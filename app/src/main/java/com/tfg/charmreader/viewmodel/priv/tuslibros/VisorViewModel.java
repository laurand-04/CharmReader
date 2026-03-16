package com.tfg.charmreader.viewmodel.priv.tuslibros;

import android.app.Application;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tfg.charmreader.data.model.BookEn;
import com.tfg.charmreader.data.model.Libro;
import com.tfg.charmreader.data.model.LibrosDeUsuario;
import com.tfg.charmreader.data.model.Sesion;
import com.tfg.charmreader.data.repository.priv.LibroRepository;
import com.tfg.charmreader.data.repository.publ.SesionRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.epub.EpubReader;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VisorViewModel extends AndroidViewModel {
    private final LibroRepository libroRepository = new LibroRepository();
    private final SesionRepository sesionRepository = new SesionRepository();


    // Pool de un solo hilo para descarga y procesamiento secuencial
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<Book> epubBook = new MutableLiveData<>();
    private final MutableLiveData<List<Resource>> chapters = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<LibrosDeUsuario> libroUsuario = new MutableLiveData<>();
    private final MutableLiveData<BookEn> bookEn = new MutableLiveData<>();
    private final MutableLiveData<String> chapterHtml = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> isFinished = new MutableLiveData<>(false);

    private int currentChapter = 0;
    private boolean grupo = false;

    public VisorViewModel(@NonNull Application application) {
        super(application);
    }

    // --- GETTERS ---
    public LiveData<String> getChapterHtml() { return chapterHtml; }
    public LiveData<LibrosDeUsuario> getLibroUsuario() { return libroUsuario; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getIsFinished() { return isFinished; }
    public int getCurrentChapterIndex() { return currentChapter; }
    public int getTotalChapters() {
        return chapters.getValue() != null ? chapters.getValue().size() : 0;
    }

    // --- LÓGICA DE APERTURA ---
    public void verificarYAbrirLibro(LibrosDeUsuario ldu) {
        isLoading.postValue(true);
        this.grupo = false;
        String rutaEpub = ldu.getRuta();

        if (rutaEpub == null || rutaEpub.isEmpty()) {
            Log.e("VisorVM", "Ruta no definida");
            return;
        }

        File file = rutaEpub.startsWith("file://")
                ? new File(java.net.URI.create(rutaEpub))
                : new File(rutaEpub);

        if (file.exists()) {
            libroUsuario.setValue(ldu);
            currentChapter = ldu.getCapitulo();
            // Ejecutamos en el executor para no bloquear UI
            executor.execute(() -> leerEpub(Uri.fromFile(file), ldu));
        } else {
            descargarYProcesar(ldu, file);
        }
    }

    public void verificarYAbrirLibro(BookEn libro, int idGrupo) {
        isLoading.postValue(true);
        this.grupo = true;
        String rutaEpub = libro.getRuta();

        if (rutaEpub == null || rutaEpub.isEmpty()) {
            Log.e("VisorVM", "Ruta no definida");
            isLoading.postValue(false); // Importante para no bloquear la UI
            return;
        }

        File file = new File(rutaEpub);

        // LLAMADA ASÍNCRONA A LA BASE DE DATOS
        sesionRepository.obtenerProximaSesion(idGrupo, new Callback<Sesion>() {
            @Override
            public void onResponse(Call<Sesion> call, Response<Sesion> response) {
                // 1. Primero asignamos el valor del capítulo
                if (response.isSuccessful() && response.body() != null) {
                    currentChapter = response.body().getCapituloInicio();
                    Log.d("VisorVM", "Capitulo inicio recibido: " + currentChapter);
                }

                // 2. AHORA ejecutamos tu lógica de archivos, AQUÍ DENTRO
                // Solo llegamos aquí cuando el servidor ya respondió
                if (file.exists()) {
                    bookEn.setValue(libro);
                    Log.d("VisorVM", "Capitulo --: " + currentChapter); // Ahora sí será 5
                    executor.execute(() -> leerEpub(Uri.fromFile(file)));
                } else {
                    Log.d("VisorVM", "Capitulo para descarga: " + currentChapter);
                    descargarYProcesar(libro, file);
                }
            }

            @Override
            public void onFailure(Call<Sesion> call, Throwable t) {
                Log.e("VisorVM", "Error en la petición", t);
                isLoading.postValue(false);

                // Opcional: ¿Quieres abrir el libro aunque falle la sesión?
                // Si es así, copia el bloque del if(file.exists) aquí también.
            }
        });
    }

    private void descargarYProcesar(LibrosDeUsuario ldu, File destino) {
        List<Integer> ids = new ArrayList<>();
        ids.add(ldu.getId().getIdL());

        libroRepository.obtenerLibrosPorIds(ids, new Callback<List<Libro>>() {
            @Override
            public void onResponse(Call<List<Libro>> call, Response<List<Libro>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    descargarLibroAStorage(response.body().get(0).getUrlLibro(), destino, ldu);
                } else {
                    isLoading.postValue(false);
                }
            }
            @Override
            public void onFailure(Call<List<Libro>> call, Throwable t) {
                isLoading.postValue(false);
            }
        });
    }

    private void descargarYProcesar(BookEn libro, File destino) {
        if (libro != null) {
                    descargarLibroAStorage(libro.getUrlLibro(), destino, libro);
        } else {
            isLoading.postValue(false);
        }
    }

    private void descargarLibroAStorage(String urlCloudinary, File destino, LibrosDeUsuario ldu) {
        // Tarea 1: Descarga
        executor.execute(() -> {
            try {
                File parent = destino.getParentFile();
                if (parent != null && !parent.exists()) parent.mkdirs();

                try (InputStream is = new URL(urlCloudinary).openStream();
                     FileOutputStream os = new FileOutputStream(destino)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
            } catch (Exception e) {
                Log.e("VisorVM", "Error descarga", e);
                isLoading.postValue(false);
            }
        });

        // Tarea 2: Leer y Renderizar (Esperará automáticamente a la Tarea 1)
        executor.execute(() -> {
            if (destino.exists()) {
                libroUsuario.postValue(ldu);
                currentChapter = ldu.getCapitulo();
                leerEpub(Uri.fromFile(destino), ldu);
            }
        });
    }

    private void descargarLibroAStorage(String urlCloudinary, File destino, BookEn libro) {
        // Tarea 1: Descarga
        executor.execute(() -> {
            try {
                File parent = destino.getParentFile();
                if (parent != null && !parent.exists()) parent.mkdirs();

                try (InputStream is = new URL(urlCloudinary).openStream();
                    FileOutputStream os = new FileOutputStream(destino)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
            } catch (Exception e) {
                Log.e("VisorVM", "Error descarga", e);
                isLoading.postValue(false);
            }
        });
        // Tarea 2: Leer y Renderizar (Esperará automáticamente a la Tarea 1)
        executor.execute(() -> {
            if (destino.exists()) {
                bookEn.postValue(libro);
                leerEpub(Uri.fromFile(destino));
            }
        });
    }

    private void leerEpub(Uri uri, LibrosDeUsuario ldu) {
        try {
            InputStream is = uri.getScheme().startsWith("http") ?
                    new URL(uri.toString()).openStream() :
                    getApplication().getContentResolver().openInputStream(uri);

            Book book = new EpubReader().readEpub(is);
            List<Resource> tempChapters = new ArrayList<>();
            for (SpineReference sr : book.getSpine().getSpineReferences()) {
                tempChapters.add(sr.getResource());
            }

            // Actualizamos LiveData para la posteridad
            epubBook.postValue(book);
            chapters.postValue(tempChapters);

            // Renderizamos directamente con los datos locales recien creados
            renderizarCapitulo(ldu.getCapitulo(), book, tempChapters);

        } catch (Exception e) {
            Log.e("VisorVM", "Error lectura EPUB", e);
            isLoading.postValue(false);
        }
    }

    private void leerEpub(Uri uri) {
        try {
            InputStream is = uri.getScheme().startsWith("http") ?
                new URL(uri.toString()).openStream() :
                getApplication().getContentResolver().openInputStream(uri);

            Book book = new EpubReader().readEpub(is);
            List<Resource> tempChapters = new ArrayList<>();
            for (SpineReference sr : book.getSpine().getSpineReferences()) {
                tempChapters.add(sr.getResource());
            }

            // Actualizamos LiveData para la posteridad
            epubBook.postValue(book);
            chapters.postValue(tempChapters);

            // Renderizamos directamente con los datos locales recien creados
            Log.d("VisorVM", "Renderizando leer capítulo " + currentChapter);
            renderizarCapitulo(currentChapter, book, tempChapters);

        } catch (Exception e) {
            Log.e("VisorVM", "Error lectura EPUB", e);
            isLoading.postValue(false);
        }
    }

    public void navegarCapitulo(int direccion) {
        int nuevoIndex = currentChapter + direccion;
        List<Resource> lista = chapters.getValue();
        Book book = epubBook.getValue();

        if (lista != null && book != null && nuevoIndex >= 0 && nuevoIndex < lista.size()) {
            currentChapter = nuevoIndex;
            // Ejecutamos renderizado en hilo de fondo
            executor.execute(() -> renderizarCapitulo(currentChapter, book, lista));
        } else if (lista != null && nuevoIndex >= lista.size() && this.grupo == false) {
            marcarFinalizado();
        }
    }

    private void renderizarCapitulo(int index, Book book, List<Resource> listaCapitulos) {
        isLoading.postValue(true);
        Log.d("VisorVM", "Renderizando capítulo " + index);
        try {
            String styledHtml;
            if (index == 0 && book.getCoverImage() != null) {
                byte[] data = book.getCoverImage().getData();
                String base64 = Base64.encodeToString(data, Base64.NO_WRAP);
                String dataUri = "data:" + book.getCoverImage().getMediaType() + ";base64," + base64;
                styledHtml = "<html><head><style>body{display:flex;justify-content:center;align-items:center;height:100vh;margin:0;background:#f5f5f5;}img{max-width:100%;max-height:100%;object-fit:contain;}</style></head><body><img src=\"" + dataUri + "\"></body></html>";
            } else {
                Resource res = listaCapitulos.get(index);
                String rawHtml = new String(res.getData(), "UTF-8");
                String processedHtml = procesarImagenesHtml(rawHtml, book);
                styledHtml = "<html><head><style>body{font-family:sans-serif; font-size:18px; line-height:1.6; padding:20px; color:#333;} img{max-width:100% !important; height:auto !important; display:block; margin: 20px auto;}</style></head><body>" + processedHtml + "</body></html>";
            }

            chapterHtml.postValue(styledHtml);
            isLoading.postValue(false);
        } catch (Exception e) {
            Log.e("VisorVM", "Error render", e);
            isLoading.postValue(false);
        }
    }

    private String procesarImagenesHtml(String html, Book book) throws IOException {
        if (book == null) return html;
        Pattern p = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");
        Matcher m = p.matcher(html);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String src = m.group(1).replace("../", "");
            Resource imgRes = book.getResources().getByHref(src);
            if (imgRes != null) {
                String base64 = Base64.encodeToString(imgRes.getData(), Base64.NO_WRAP);
                String dataUri = "data:" + imgRes.getMediaType() + ";base64," + base64;
                m.appendReplacement(sb, m.group(0).replace(m.group(1), dataUri));
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    // --- OTROS MÉTODOS ---
    public void marcarFinalizado() {
        LibrosDeUsuario ldu = libroUsuario.getValue();
        if (ldu != null && ldu.getFechaFin() == null) {
            ldu.setFechaFin(new Date());
            ldu.setCapitulo(getTotalChapters() - 1);
            ldu.setScroll(1f);
            libroRepository.actualizarProgreso(ldu, new Callback<LibrosDeUsuario>() {
                @Override public void onResponse(Call<LibrosDeUsuario> c, Response<LibrosDeUsuario> r) { isFinished.postValue(true); }
                @Override public void onFailure(Call<LibrosDeUsuario> c, Throwable t) { isFinished.postValue(true); }
            });
        } else {
            isFinished.postValue(true);
        }
    }

    public void guardarEstadoActual(float scroll) {
        LibrosDeUsuario ldu = libroUsuario.getValue();
        if (ldu != null && ldu.getFechaFin() == null) {
            if (ldu.getFechaInicio() == null) {
                ldu.setFechaInicio(new Date());
            }
            ldu.setCapitulo(currentChapter);
            ldu.setScroll(scroll);
            libroRepository.actualizarProgreso(ldu, new Callback<LibrosDeUsuario>() {
                @Override public void onResponse(Call<LibrosDeUsuario> c, Response<LibrosDeUsuario> r) {}
                @Override public void onFailure(Call<LibrosDeUsuario> c, Throwable t) {}
            });
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown(); // Limpieza del pool
    }
}