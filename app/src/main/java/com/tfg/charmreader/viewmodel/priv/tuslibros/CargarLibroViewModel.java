package com.tfg.charmreader.viewmodel.priv.tuslibros;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.tfg.charmreader.data.model.BookEn;
import com.tfg.charmreader.data.model.CCLibrosDeUsuario;
import com.tfg.charmreader.data.model.Libro;
import com.tfg.charmreader.data.model.LibrosDeUsuario;
import com.tfg.charmreader.data.network.API.CloudinaryClient;
import com.tfg.charmreader.data.repository.priv.LibroRepository;
import com.tfg.charmreader.data.repository.priv.proximamente.BookRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CargarLibroViewModel extends AndroidViewModel {
    private final LibroRepository repository = new LibroRepository();
    private final MutableLiveData<Boolean> isProcessing = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> uploadSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final BookRepository bookRepository = new BookRepository();

    public CargarLibroViewModel(@NonNull Application application) { super(application); }

    public LiveData<Boolean> getIsProcessing() { return isProcessing; }
    public LiveData<Boolean> getUploadSuccess() { return uploadSuccess; }
    public LiveData<String> getError() { return error; }

    public void procesarEpub(Uri epubUri, int idUsuario, boolean grupo, int idLibro) {
        isProcessing.setValue(true);
        new Thread(() -> {
            try {
                InputStream is = getApplication().getContentResolver().openInputStream(epubUri);
                Book book = new EpubReader().readEpub(is);

                // Extraer metadatos
                String title = book.getTitle();
                StringBuilder authors = new StringBuilder();
                for (Author a : book.getMetadata().getAuthors()) {
                    authors.append(a.getFirstname()).append(" ").append(a.getLastname()).append(", ");
                }
                String finalAuthors = authors.length() > 0 ? authors.substring(0, authors.length() - 2) : "Autor desconocido";
                String isbn = book.getMetadata().getIdentifiers().isEmpty() ? "" : book.getMetadata().getIdentifiers().get(0).getValue();
                byte[] coverData = (book.getCoverImage() != null) ? book.getCoverImage().getData() : null;

                String rutaAbsoluta = copiarEpub(epubUri); // Ya tienes el archivo en el almacenamiento interno
                File archivoEpub = new File(rutaAbsoluta); // Convertimos el String/Uri a File

                // 1. Subir Portada
                subirImagenCloudinary(coverData, urlPortada -> {

                    // 2. Subir el Archivo EPUB (nuevo paso)
                    subirArchivoRawCloudinary(archivoEpub, urlEpub -> {
                        //Si es un libro de grupo trabajamos con -> bookEn
                        if(grupo){
                            registrarEnApi(idLibro, rutaAbsoluta, urlEpub);
                        }
                        //Si es un libro de usuario trabajamos con libro
                        else{
                            Libro nuevo = new Libro(isbn, title, finalAuthors, book.getSpine().size());
                            nuevo.setUrlImagen(urlPortada); // Imagen
                            nuevo.setUrlLibro(urlEpub);    // PDF/EPUB en Cloudinary

                            registrarEnApi(nuevo, rutaAbsoluta, idUsuario);
                        }
                    });
                });

            } catch (Exception e) {
                postError("Error al procesar el archivo EPUB");
            }
        }).start();
    }

    private void subirImagenCloudinary(byte[] data, CloudinaryCallback cb) {
        if (data == null) { cb.onUrl(""); return; }
        CloudinaryClient.nuevoUpload(data).callback(new UploadCallback() {
            @Override public void onSuccess(String r, Map res) { cb.onUrl((String) res.get("secure_url")); }
            @Override public void onError(String r, ErrorInfo e) { cb.onUrl(""); }
            @Override public void onStart(String r) {}
            @Override public void onProgress(String r, long b, long t) {}
            @Override public void onReschedule(String r, ErrorInfo e) {}
        }).dispatch();
    }

    private void subirArchivoRawCloudinary(File file, CloudinaryCallback cb) {
        if (file == null || !file.exists()) { cb.onUrl(""); return; }

        CloudinaryClient.nuevoUpload(file.getAbsolutePath())
                .option("resource_type", "raw") // IMPRESCINDIBLE para .epub o .pdf
                .option("use_filename", true)   // Opcional: mantiene el nombre original
                .callback(new UploadCallback() {
                    @Override public void onSuccess(String r, Map res) { cb.onUrl((String) res.get("secure_url")); }
                    @Override public void onError(String r, ErrorInfo e) {
                        postError("Error al subir el archivo a la nube");
                        cb.onUrl("");
                    }
                    @Override public void onStart(String r) {}
                    @Override public void onProgress(String r, long b, long t) {}
                    @Override public void onReschedule(String r, ErrorInfo e) {}
                }).dispatch();
    }

    private void registrarEnApi(Libro libro, String ruta, int idU) {
        repository.anadirLibro(libro, new Callback<Libro>() {
            @Override public void onResponse(Call<Libro> call, Response<Libro> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    LibrosDeUsuario ldu = new LibrosDeUsuario(new CCLibrosDeUsuario(idU, resp.body().getId()), 0, ruta);
                    repository.actualizarProgreso(ldu, new Callback<LibrosDeUsuario>() {
                        @Override public void onResponse(Call<LibrosDeUsuario> c, Response<LibrosDeUsuario> r) {
                            if (r.isSuccessful()) uploadSuccess.postValue(true);
                            else postError("Error al vincular con usuario");
                        }
                        @Override public void onFailure(Call<LibrosDeUsuario> c, Throwable t) { postError("Error de red"); }
                    });
                }
            }
            @Override public void onFailure(Call<Libro> call, Throwable t) { postError("Error al guardar libro"); }
        });
    }

    private void registrarEnApi(int idLibro, String rutaAbsoluta, String urlEpub) {
        bookRepository.obtenerBookPorId(idLibro, new Callback<BookEn>() {
            @Override public void onResponse(Call<BookEn> call, Response<BookEn> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    BookEn libro = resp.body();
                    libro.setRuta(rutaAbsoluta);
                    libro.setUrlLibro(urlEpub);

                    bookRepository.anadirBook(libro, new Callback<BookEn>() {
                        @Override public void onResponse(Call<BookEn> call, Response<BookEn> r) {
                            if (r.isSuccessful()) {
                                Log.d("registrarEnAPi de CargarLibroViewModel", "Book cargado de forma correcta");
                                uploadSuccess.postValue(true);
                            }
                            else postError("Error al vincular con usuario");
                        }
                        @Override public void onFailure(Call<BookEn> call, Throwable t) { postError("Error de red"); }
                    });
                }
            }
            @Override public void onFailure(Call<BookEn> call, Throwable t) { postError("Error al guardar libro"); }
        });
    }

    private String copiarEpub(Uri uri) throws Exception {
        InputStream in = getApplication().getContentResolver().openInputStream(uri);
        File file = new File(getApplication().getFilesDir(), "libro_" + System.currentTimeMillis() + ".epub");
        try (FileOutputStream out = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) out.write(buffer, 0, len);
        }
        return file.getAbsolutePath(); // Devuelve la ruta directa, no el Uri string
    }

    private void postError(String m) {
        error.postValue(m);
        isProcessing.postValue(false);
    }

    interface CloudinaryCallback { void onUrl(String url); }
}