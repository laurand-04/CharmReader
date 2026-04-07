package com.tfg.charmreader.viewmodel.priv.tusobras;

import android.app.Application;
import android.text.Html;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tfg.charmreader.data.model.Obras;
import com.tfg.charmreader.data.network.API.CloudinaryClient;
import com.tfg.charmreader.data.repository.priv.tusObras.ObrasRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.epub.EpubWriter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditorObraViewModel extends AndroidViewModel {

    private final ObrasRepository obrasRepository = new ObrasRepository();
    private int idObraActual = -1;
    private Obras obra;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> mensaje = new MutableLiveData<>();
    private final MutableLiveData<String> tituloCapitulo = new MutableLiveData<>();
    private final MutableLiveData<String> contenidoCapitulo = new MutableLiveData<>();
    private final MutableLiveData<Boolean> esPrimerCapitulo = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> esUltimoCapitulo = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> puedeEliminar = new MutableLiveData<>(false);

    private Book miLibro;
    private String rutaArchivo;
    private int indiceCapituloActual = 1;

    public EditorObraViewModel(@NonNull Application application) { super(application); }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getMensaje() { return mensaje; }
    public LiveData<String> getTituloCapitulo() { return tituloCapitulo; }
    public LiveData<String> getContenidoCapitulo() { return contenidoCapitulo; }
    public LiveData<Boolean> getEsPrimerCapitulo() { return esPrimerCapitulo; }
    public LiveData<Boolean> getEsUltimoCapitulo() { return esUltimoCapitulo; }
    public LiveData<Boolean> getPuedeEliminar() { return puedeEliminar; }

    // --- 1. CARGAR LA OBRA ---
    public void cargarObra(String ruta, Obras obra) {
        this.rutaArchivo = ruta;
        this.idObraActual = (obra != null) ? obra.getId() : -1;
        this.obra = obra;
        isLoading.setValue(true);

        new Thread(() -> {
            try {
                File archivo = new File(ruta);
                if (!archivo.exists()) {
                    mensaje.postValue("El archivo no existe localmente.");
                    isLoading.postValue(false);
                    return;
                }
                miLibro = new EpubReader().readEpub(new FileInputStream(archivo));

                int totalElementos = miLibro.getSpine().size();
                // Abrir en el último capítulo por defecto
                indiceCapituloActual = (totalElementos > 1) ? totalElementos - 1 : 0;

                cargarCapituloEnUI();
            } catch (Exception e) {
                Log.e("EditorVM", "Error leyendo EPUB", e);
                mensaje.postValue("Error al abrir la obra.");
            } finally {
                isLoading.postValue(false);
            }
        }).start();
    }

    // --- 2. NAVEGACIÓN Y EDICIÓN ---
    public void cambiarCapitulo(int direccion, String textoActualDelEditor) {
        guardarCapituloEnMemoria(textoActualDelEditor);
        int nuevoIndice = indiceCapituloActual + direccion;

        if (nuevoIndice >= 1 && nuevoIndice < miLibro.getSpine().size()) {
            indiceCapituloActual = nuevoIndice;
            cargarCapituloEnUI();
        }
    }

    public void anadirNuevoCapitulo(String textoActualDelEditor) {
        guardarCapituloEnMemoria(textoActualDelEditor);

        int nuevoNumero = miLibro.getSpine().size();
        String tituloNuevo = "Capítulo " + nuevoNumero;
        String htmlVacio = "<html><head><title>Cap</title></head><body><div></div></body></html>";

        miLibro.addSection(tituloNuevo,
                new Resource(htmlVacio.getBytes(StandardCharsets.UTF_8), "capitulo_" + nuevoNumero + ".html"));

        indiceCapituloActual = miLibro.getSpine().size() - 1;
        cargarCapituloEnUI();
        mensaje.postValue("Nuevo capítulo creado");
    }

    public void eliminarUltimoCapitulo() {
        int ultimoIndex = miLibro.getSpine().size() - 1;

        if (ultimoIndex > 1 && indiceCapituloActual == ultimoIndex) {
            try {
                miLibro.getSpine().getSpineReferences().remove(ultimoIndex);
                indiceCapituloActual = ultimoIndex - 1;
                cargarCapituloEnUI();
                mensaje.postValue("Capítulo eliminado correctamente");
            } catch (Exception e) {
                mensaje.postValue("Error al eliminar capítulo");
            }
        } else if (ultimoIndex <= 1) {
            mensaje.postValue("No puedes eliminar el primer capítulo");
        } else {
            mensaje.postValue("Navega al último capítulo para eliminarlo");
        }
    }

    // --- 3. GUARDADO FÍSICO + SINCRONIZACIÓN NUBE ---
    public void guardarObraFisicamente(String textoActualDelEditor) {
        isLoading.setValue(true);
        new Thread(() -> {
            try {
                // Primero: Guardar el texto actual en el objeto Book en memoria
                guardarCapituloEnMemoria(textoActualDelEditor);

                // Segundo: Escribir el archivo .epub en el almacenamiento del móvil
                EpubWriter epubWriter = new EpubWriter();
                File archivoLocal = new File(rutaArchivo);
                try (FileOutputStream out = new FileOutputStream(archivoLocal)) {
                    epubWriter.write(miLibro, out);
                }

                // Tercero: Si tenemos ID de obra, subimos a Cloudinary y actualizamos servidor
                if (idObraActual != -1) {
                    subirACloudinaryYSincronizar(archivoLocal);
                } else {
                    mensaje.postValue("Guardado localmente (Sin ID para sincronizar)");
                    isLoading.postValue(false);
                }

            } catch (Exception e) {
                Log.e("EditorVM", "Error guardando", e);
                mensaje.postValue("Error al guardar la obra");
                isLoading.postValue(false);
            }
        }).start();
    }

    private void subirACloudinaryYSincronizar(File archivo) {
        mensaje.postValue("Sincronizando con la nube...");

        CloudinaryClient.subirArchivoRawCloudinary(archivo, new CloudinaryClient.CloudinaryCallback() {
            @Override
            public void onUrl(String urlEpubCloudinary) {
                // Actualizamos el objeto obra con la nueva URL y fecha
                obra.setUrl_obra(urlEpubCloudinary);
                obra.setFecha_ultima_modificacion(new Date());
                obra.setPaginas(miLibro.getContents().size());
                // Llamamos a la API para guardar los cambios en la base de datos
                actualizarObraEnServidor();
            }

            @Override
            public void onError(String mensajeError) {
                Log.e("EditorVM", "Error Cloudinary: " + mensajeError);
                mensaje.postValue("Guardado local. Error al subir a la nube.");
                isLoading.postValue(false);
            }
        });
    }

    private void actualizarObraEnServidor() {
        obrasRepository.guardarObra(obra, new Callback<Obras>() {
            @Override
            public void onResponse(Call<Obras> call, Response<Obras> response) {
                if (response.isSuccessful()) {
                    mensaje.postValue("¡Obra guardada y sincronizada!");
                } else {
                    mensaje.postValue("Guardado local. Error al actualizar servidor.");
                }
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(Call<Obras> call, Throwable t) {
                mensaje.postValue("Guardado local. Sin conexión para sincronizar.");
                isLoading.postValue(false);
            }
        });
    }

    // --- UTILIDADES INTERNAS ---
    private void cargarCapituloEnUI() {
        try {
            Resource recurso = miLibro.getSpine().getResource(indiceCapituloActual);
            String htmlBruto = new String(recurso.getData(), StandardCharsets.UTF_8);

            htmlBruto = htmlBruto.replaceAll("(?is)<h[1-6][^>]*>.*?</h[1-6]>", "");
            htmlBruto = htmlBruto.replaceAll("(?is)<title>.*?</title>", "");

            CharSequence textoFormateado = Html.fromHtml(htmlBruto, Html.FROM_HTML_MODE_LEGACY);

            String textoFinal = textoFormateado.toString().trim();

            //String textoPlano = Html.fromHtml(htmlBruto, Html.FROM_HTML_MODE_COMPACT).toString().trim();
            //textoPlano = textoPlano.replaceAll("(?i)^Capítulo\\s*\\d+\\s*", "");

            tituloCapitulo.postValue("Capítulo " + indiceCapituloActual);
            contenidoCapitulo.postValue(textoFinal);

            esPrimerCapitulo.postValue(indiceCapituloActual == 1);
            esUltimoCapitulo.postValue(indiceCapituloActual == miLibro.getSpine().size() - 1);
            puedeEliminar.postValue(indiceCapituloActual == miLibro.getSpine().size() - 1 && miLibro.getSpine().size() > 2);

        } catch (Exception e) {
            Log.e("EditorVM", "Error cargando capítulo", e);
        }
    }

    private void guardarCapituloEnMemoria(String textoPlano) {
        try {
            String titulo = "Capítulo " + indiceCapituloActual;
            StringBuilder cuerpoHtml = new StringBuilder();

            // Dividimos el texto por saltos de línea para crear párrafos <p>
            String[] parrafos = textoPlano.split("\n");
            for (String p : parrafos) {
                if (!p.trim().isEmpty()) {
                    // Escapamos caracteres básicos manualmente para no romper el HTML
                    String textoEscapado = p.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                    cuerpoHtml.append("<p>").append(textoEscapado).append("</p>");
                } else {
                    // Si es un salto de línea vacío, añadimos un párrafo vacío o un break
                    cuerpoHtml.append("<br/>");
                }
            }

            String nuevoHtml =
                    "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                            "<html><head><title>" + titulo + "</title></head>" +
                            "<body><h1>" + titulo + "</h1>" +
                            cuerpoHtml.toString() +
                            "</body></html>";

            Resource recursoExistente = miLibro.getSpine().getResource(indiceCapituloActual);
            recursoExistente.setData(nuevoHtml.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            Log.e("EditorVM", "Error actualizando memoria", e);
        }
    }
}