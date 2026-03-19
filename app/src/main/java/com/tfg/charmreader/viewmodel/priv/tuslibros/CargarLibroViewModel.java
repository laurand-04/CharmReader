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
import com.tfg.charmreader.data.model.Obras;
import com.tfg.charmreader.data.network.API.CloudinaryClient;
import com.tfg.charmreader.data.repository.priv.LibroRepository;
import com.tfg.charmreader.data.repository.priv.proximamente.BookRepository;
import com.tfg.charmreader.data.repository.priv.tusObras.ObrasRepository;

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
    private final ObrasRepository obrasRepository = new ObrasRepository();
    private final MutableLiveData<Boolean> isProcessing = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> uploadSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final BookRepository bookRepository = new BookRepository();

    public CargarLibroViewModel(@NonNull Application application) { super(application); }

    public LiveData<Boolean> getIsProcessing() { return isProcessing; }
    public LiveData<Boolean> getUploadSuccess() { return uploadSuccess; }
    public LiveData<String> getError() { return error; }

    public void procesarEpub(Uri epubUri, int idUsuario, boolean grupo, int idLibro, Obras obra) {
        Log.d("DEBUG_PUBLISH", "Actualizando 2 Obra ID: " + obra);
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

                String rutaAbsoluta = copiarEpub(epubUri);
                File archivoEpub = new File(rutaAbsoluta);

                // 1. Subir Portada usando la clase estática CloudinaryClient
                CloudinaryClient.subirImagenCloudinary(coverData, new CloudinaryClient.CloudinaryCallback() {
                    @Override
                    public void onUrl(String urlPortada) {

                        // 2. Subir el Archivo EPUB usando la clase estática CloudinaryClient
                        CloudinaryClient.subirArchivoRawCloudinary(archivoEpub, new CloudinaryClient.CloudinaryCallback() {
                            @Override
                            public void onUrl(String urlEpub) {
                                //Si es un libro de grupo trabajamos con -> bookEn
                                if(grupo){
                                    registrarEnApi(idLibro, rutaAbsoluta, urlEpub);
                                }
                                //Si es un libro de usuario trabajamos con libro
                                else{
                                    Libro nuevo = new Libro(isbn, title, finalAuthors, book.getSpine().size());
                                    nuevo.setUrlImagen(urlPortada);
                                    nuevo.setUrlLibro(urlEpub);

                                    registrarEnApi(nuevo, rutaAbsoluta, idUsuario, obra);
                                }
                            }

                            @Override
                            public void onError(String mensajeError) {
                                postError(mensajeError);
                            }
                        });
                    }

                    @Override
                    public void onError(String mensajeError) {
                        // Si falla la imagen, podrías decidir continuar sin ella o cortar aquí.
                        // En este caso, continuamos mandando "" como URL (comportamiento original)
                        onUrl("");
                    }
                });

            } catch (Exception e) {
                postError("Error al procesar el archivo EPUB");
            }
        }).start();
    }

    private void registrarEnApi(Libro libro, String ruta, int idU, Obras obra) {
        Log.d("DEBUG_PUBLISH", "Actualizando 1 Obra ID: " + obra);
        repository.anadirLibro(libro, new Callback<Libro>() {
            @Override public void onResponse(Call<Libro> call, Response<Libro> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    int nuevoIdLibro = resp.body().getId();
                    LibrosDeUsuario ldu = new LibrosDeUsuario(new CCLibrosDeUsuario(idU, resp.body().getId()), 0, ruta);
                    repository.actualizarProgreso(ldu, new Callback<LibrosDeUsuario>() {
                        @Override public void onResponse(Call<LibrosDeUsuario> c, Response<LibrosDeUsuario> r) {
                            if (r.isSuccessful()) {
                                if (obra != null) {
                                    // Seteamos el ID del libro creado en el objeto obra
                                    obra.setIdLibro(nuevoIdLibro);
                                    Log.d("DEBUG_PUBLISH", "Actualizando Obra ID: " + obra.getId() + " con idLibro: " + nuevoIdLibro);

                                    // Llamamos al repositorio de obras para actualizarla en la BD
                                    actualizarIdLibroEnObra(obra);
                                } else {
                                    Log.d("DEBUG_PUBLISH", "Obra nula");
                                    uploadSuccess.postValue(true);
                                }
                            }
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

    private void actualizarIdLibroEnObra(Obras obra) {
        // Asumo que tienes un ObrasRepository o el acceso desde este viewmodel
        obrasRepository.guardarObra(obra, new Callback<Obras>() {
            @Override
            public void onResponse(Call<Obras> call, Response<Obras> response) {
                if (response.isSuccessful()) {
                    uploadSuccess.postValue(true);
                } else {
                    postError("Libro creado pero no se pudo vincular a la obra");
                }
            }
            @Override
            public void onFailure(Call<Obras> call, Throwable t) {
                postError("Error de red al vincular obra");
            }
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
}