package com.tfg.charmreader.viewmodel.priv.tuslibros;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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
import java.util.List;

import okhttp3.ResponseBody;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CargarLibroViewModel extends AndroidViewModel {

    private static final String TAG = "DEBUG_CARGA";

    private final LibroRepository repository = new LibroRepository();
    private final ObrasRepository obrasRepository = new ObrasRepository();
    private final BookRepository bookRepository = new BookRepository();

    private List<LibrosDeUsuario> seguidoresTemporales;

    private final MutableLiveData<Boolean> isProcessing = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> uploadSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public CargarLibroViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Boolean> getUploadSuccess() { return uploadSuccess; }
    public LiveData<String> getError() { return error; }

    public void procesarEpub(Uri epubUri, int idUsuario, boolean grupo, int idLibro, Obras obra) {
        Log.d(TAG, "1. Iniciando procesarEpub. Usuario: " + idUsuario + ", Grupo: " + grupo + ", idLibro: " + idLibro);
        isProcessing.setValue(true);
        seguidoresTemporales = null;

        if (!grupo && obra != null && obra.getIdLibro() > 0) {
            Log.d(TAG, "1.1. Detectada obra existente con ID Libro: " + obra.getIdLibro() + ". Buscando seguidores para migrar...");
            repository.obtenerLibrosUsuarioPorIdLibro(obra.getIdLibro(), new Callback<List<LibrosDeUsuario>>() {
                @Override
                public void onResponse(Call<List<LibrosDeUsuario>> call, Response<List<LibrosDeUsuario>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        seguidoresTemporales = response.body();
                        Log.d(TAG, "1.2. Seguidores encontrados: " + seguidoresTemporales.size());
                    } else {
                        Log.w(TAG, "1.2. No se encontraron seguidores o la respuesta falló.");
                    }
                    iniciarFlujoSubida(epubUri, idUsuario, grupo, idLibro, obra);
                }

                @Override
                public void onFailure(Call<List<LibrosDeUsuario>> call, Throwable t) {
                    Log.e(TAG, "1.2. Error de red buscando seguidores: " + t.getMessage());
                    iniciarFlujoSubida(epubUri, idUsuario, grupo, idLibro, obra);
                }
            });
        } else {
            Log.d(TAG, "1.1. Obra nueva o grupo. Saltando búsqueda de seguidores.");
            iniciarFlujoSubida(epubUri, idUsuario, grupo, idLibro, obra);
        }
    }

    private void iniciarFlujoSubida(Uri epubUri, int idUsuario, boolean grupo, int idLibro, Obras obra) {
        Log.d(TAG, "2. Iniciando flujo de subida en hilo secundario...");
        new Thread(() -> {
            try {
                InputStream is = getApplication().getContentResolver().openInputStream(epubUri);
                Book book = new EpubReader().readEpub(is);

                String title = book.getTitle();
                Log.d(TAG, "2.1. EPUB Leído. Título: " + title);

                StringBuilder authors = new StringBuilder();
                for (Author a : book.getMetadata().getAuthors()) {
                    authors.append(a.getFirstname()).append(" ").append(a.getLastname()).append(", ");
                }

                String finalAuthors = authors.length() > 0
                        ? authors.substring(0, authors.length() - 2)
                        : "Autor desconocido";

                String isbn = book.getMetadata().getIdentifiers().isEmpty()
                        ? ""
                        : book.getMetadata().getIdentifiers().get(0).getValue();

                byte[] coverData = book.getCoverImage() != null
                        ? book.getCoverImage().getData()
                        : null;

                Log.d(TAG, "2.2. Portada encontrada: " + (coverData != null));

                String rutaAbsoluta = copiarEpub(epubUri);
                Log.d(TAG, "2.3. Archivo copiado localmente en: " + rutaAbsoluta);
                File archivoEpub = new File(rutaAbsoluta);

                Log.d(TAG, "3. Subiendo portada a Cloudinary...");
                CloudinaryClient.subirImagenCloudinary(coverData, new CloudinaryClient.CloudinaryCallback() {
                    @Override
                    public void onUrl(String urlPortada) {
                        Log.d(TAG, "3.1. Portada subida OK: " + urlPortada);
                        Log.d(TAG, "4. Subiendo archivo EPUB a Cloudinary...");

                        CloudinaryClient.subirArchivoRawCloudinary(archivoEpub, new CloudinaryClient.CloudinaryCallback() {
                            @Override
                            public void onUrl(String urlEpub) {
                                Log.d(TAG, "4.1. EPUB subido OK: " + urlEpub);

                                if (grupo) {
                                    Log.d(TAG, "5. Flujo GRUPO: Registrando en API...");
                                    registrarEnApi(idLibro, rutaAbsoluta, urlEpub);
                                } else {
                                    Log.d(TAG, "5. Flujo INDIVIDUAL: Creando objeto Libro...");
                                    Libro nuevo = new Libro(isbn, title, finalAuthors, book.getSpine().size());
                                    nuevo.setUrlImagen(urlPortada);
                                    nuevo.setUrlLibro(urlEpub);
                                    registrarEnApi(nuevo, rutaAbsoluta, idUsuario, obra);
                                }
                            }

                            @Override
                            public void onError(String mensajeError) {
                                Log.e(TAG, "4.Error subiendo EPUB: " + mensajeError);
                                postError("Error subiendo EPUB a la nube");
                            }
                        });
                    }

                    @Override
                    public void onError(String mensajeError) {
                        Log.w(TAG, "3.Error subiendo portada: " + mensajeError + ". Continuando sin portada.");
                        onUrl(""); // Reintenta sin portada
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "2.Error crítico en el hilo de procesamiento: ", e);
                postError("Error al procesar el archivo EPUB");
            }
        }).start();
    }

    private void registrarEnApi(Libro libro, String ruta, int idU, Obras obra) {
        Log.d(TAG, "6. Llamando a API: anadirLibro...");
        repository.anadirLibro(libro, new Callback<Libro>() {
            @Override
            public void onResponse(Call<Libro> call, Response<Libro> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    Log.e(TAG, "6.Error API anadirLibro. Code: " + resp.code());
                    postError("Error al guardar libro en base de datos");
                    return;
                }

                int nuevoIdLibro = resp.body().getId();
                Log.d(TAG, "6.1. Libro registrado con éxito. Nuevo ID: " + nuevoIdLibro);

                if (seguidoresTemporales != null && !seguidoresTemporales.isEmpty()) {
                    Log.d(TAG, "7. Iniciando migración de " + seguidoresTemporales.size() + " seguidores...");
                    migrarSeguidoresSecuencial(0, nuevoIdLibro, obra, ruta, idU);
                } else {
                    Log.d(TAG, "7. No hay seguidores. Vinculando usuario actual directamente...");
                    vincularUsuarioYFinalizar(idU, nuevoIdLibro, ruta, obra);
                }
            }

            @Override
            public void onFailure(Call<Libro> call, Throwable t) {
                Log.e(TAG, "6.Error de red en anadirLibro: " + t.getMessage());
                postError("Error de conexión al registrar libro");
            }
        });
    }

    private void migrarSeguidoresSecuencial(int index, int nuevoIdLibro, Obras obra, String ruta, int idUsuario) {
        if (seguidoresTemporales == null || index >= seguidoresTemporales.size()) {
            Log.d(TAG, "8. Migración finalizada. Eliminando libro antiguo de la BD...");

            // --- PASO CLAVE: Eliminar el libro viejo de la tabla Libro ---
            repository.eliminarLibro(obra.getIdLibro(), new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d(TAG, "Libro antiguo borrado físicamente.");
                    actualizarIdLibroEnObraSeguro(obra, nuevoIdLibro);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    // Aunque falle el borrado del viejo, actualizamos la obra para no dejar al usuario colgado
                    actualizarIdLibroEnObraSeguro(obra, nuevoIdLibro);
                }
            });
            return;
        }

        LibrosDeUsuario lduOld = seguidoresTemporales.get(index);
        int userId = lduOld.getId().getIdU();
        Log.d(TAG, "7.1. Migrando seguidor (" + (index + 1) + "/" + seguidoresTemporales.size() + ") - Usuario ID: " + userId);

        LibrosDeUsuario lduNuevo = new LibrosDeUsuario(
                new CCLibrosDeUsuario(userId, nuevoIdLibro),
                lduOld.getCapitulo(),
                ruta,
                lduOld.getScroll(),
                lduOld.getIdEstanteria(),
                lduOld.getValoracion(),
                lduOld.getDescripcion(),
                lduOld.getFechaInicio(),
                lduOld.getFechaFin()
        );

        repository.actualizarProgreso(lduNuevo, new Callback<LibrosDeUsuario>() {
            @Override
            public void onResponse(Call<LibrosDeUsuario> call, Response<LibrosDeUsuario> response) {
                Log.d(TAG, "7.2. Progreso actualizado para user " + userId + ". Eliminando vinculación antigua...");
                repository.eliminarLibrodeUsuario(userId, obra.getIdLibro(), new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        migrarSeguidoresSecuencial(index + 1, nuevoIdLibro, obra, ruta, idUsuario);
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(TAG, "7.3. Fallo al eliminar antiguo para user " + userId + ". Continuando...");
                        migrarSeguidoresSecuencial(index + 1, nuevoIdLibro, obra, ruta, idUsuario);
                    }
                });
            }

            @Override
            public void onFailure(Call<LibrosDeUsuario> call, Throwable t) {
                Log.e(TAG, "7.2. Fallo al actualizar para user " + userId + ". Continuando...");
                migrarSeguidoresSecuencial(index + 1, nuevoIdLibro, obra, ruta, idUsuario);
            }
        });
    }

    private void vincularUsuarioYFinalizar(int idU, int idL, String ruta, Obras obra) {
        Log.d(TAG, "7. Vinculando usuario " + idU + " con libro " + idL);
        LibrosDeUsuario ldu = new LibrosDeUsuario(new CCLibrosDeUsuario(idU, idL), 0, ruta);

        repository.actualizarProgreso(ldu, new Callback<LibrosDeUsuario>() {
            @Override
            public void onResponse(Call<LibrosDeUsuario> call, Response<LibrosDeUsuario> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "7.1. Vinculación exitosa. Pasando a actualizar la obra...");
                    actualizarIdLibroEnObraSeguro(obra, idL);
                } else {
                    Log.e(TAG, "7.1. Error vinculando usuario. Code: " + response.code());
                    postError("Error al vincular tu usuario con el libro");
                }
            }

            @Override
            public void onFailure(Call<LibrosDeUsuario> call, Throwable t) {
                Log.e(TAG, "7.1. Error de red vinculando usuario: " + t.getMessage());
                postError("Error de conexión en vinculación");
            }
        });
    }

    private void actualizarIdLibroEnObraSeguro(Obras obra, int nuevoIdLibro) {
        Log.d(TAG, "9. Actualizando Obra. ID Obra: " + (obra != null ? obra.getId() : "NULL") + " -> Nuevo idLibro: " + nuevoIdLibro);

        if (obra == null) {
            Log.d(TAG, "9.1. No hay objeto obra que actualizar (era una subida directa). Finalizado.");
            uploadSuccess.postValue(true);
            return;
        }

        Obras copia = new Obras(obra);
        copia.setIdLibro(nuevoIdLibro);
        copia.setPublicado(true);

        obrasRepository.guardarObra(copia, new Callback<Obras>() {
            @Override
            public void onResponse(Call<Obras> call, Response<Obras> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "10. PROCESO COMPLETADO EXITOSAMENTE.");
                    uploadSuccess.postValue(true);
                } else {
                    Log.e(TAG, "10. Error actualizando obra en API. Code: " + response.code());
                    postError("Libro creado pero no se pudo actualizar la obra");
                }
            }

            @Override
            public void onFailure(Call<Obras> call, Throwable t) {
                Log.e(TAG, "10. Error de red actualizando obra: " + t.getMessage());
                postError("Error de red al actualizar obra");
            }
        });
    }

    private String copiarEpub(Uri uri) throws Exception {
        InputStream in = getApplication().getContentResolver().openInputStream(uri);
        File file = new File(getApplication().getFilesDir(), "libro_" + System.currentTimeMillis() + ".epub");
        try (FileOutputStream out = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        }
        return file.getAbsolutePath();
    }

    private void postError(String m) {
        Log.e(TAG, ">>> ERROR FINAL: " + m);
        error.postValue(m);
        isProcessing.postValue(false);
    }

    private void registrarEnApi(int idLibro, String rutaAbsoluta, String urlEpub) {
        Log.d(TAG, "6-G. Buscando libro de grupo ID: " + idLibro);
        bookRepository.obtenerBookPorId(idLibro, new Callback<BookEn>() {
            @Override public void onResponse(Call<BookEn> call, Response<BookEn> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    BookEn libro = resp.body();
                    libro.setRuta(rutaAbsoluta);
                    libro.setUrlLibro(urlEpub);
                    Log.d(TAG, "6-G.1. Actualizando BookEn con nuevas URLs...");
                    bookRepository.anadirBook(libro, new Callback<BookEn>() {
                        @Override public void onResponse(Call<BookEn> call, Response<BookEn> r) {
                            if (r.isSuccessful()) {
                                Log.d(TAG, "7-G. Éxito en flujo de grupo.");
                                uploadSuccess.postValue(true);
                            } else {
                                Log.e(TAG, "7-G. Fallo vinculando libro de grupo.");
                                postError("Error al vincular libro de grupo");
                            }
                        }
                        @Override public void onFailure(Call<BookEn> call, Throwable t) { postError("Error de red"); }
                    });
                }
            }
            @Override public void onFailure(Call<BookEn> call, Throwable t) { postError("Error al guardar libro"); }
        });
    }
}