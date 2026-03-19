package com.tfg.charmreader.viewmodel.priv.tusobras;

import android.app.Application;
import android.text.Html;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tfg.charmreader.data.model.Obras;
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
import okhttp3.ResponseBody;
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
    private final MutableLiveData<Boolean> puedeEliminar = new MutableLiveData<>(false); // NUEVO

    private Book miLibro;
    private String rutaArchivo;
    private int indiceCapituloActual = 0;

    public EditorObraViewModel(@NonNull Application application) { super(application); }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getMensaje() { return mensaje; }
    public LiveData<String> getTituloCapitulo() { return tituloCapitulo; }
    public LiveData<String> getContenidoCapitulo() { return contenidoCapitulo; }
    public LiveData<Boolean> getEsPrimerCapitulo() { return esPrimerCapitulo; }
    public LiveData<Boolean> getEsUltimoCapitulo() { return esUltimoCapitulo; }
    public LiveData<Boolean> getPuedeEliminar() { return puedeEliminar; } // NUEVO

    // --- 1. CARGAR LA OBRA ---
    public void cargarObra(String ruta, Obras obra) {
        this.rutaArchivo = ruta;
        this.idObraActual = obra.getId();
        this.obra = obra;
        isLoading.setValue(true);

        new Thread(() -> {
            try {
                File archivo = new File(ruta);
                if (!archivo.exists()) {
                    mensaje.postValue("El archivo de la obra no existe.");
                    isLoading.postValue(false);
                    return;
                }

                miLibro = new EpubReader().readEpub(new FileInputStream(archivo));

                int totalCapitulos = miLibro.getSpine().size();
                if (totalCapitulos > 0) {
                    indiceCapituloActual = totalCapitulos - 1;
                } else {
                    indiceCapituloActual = 0;
                }

                cargarCapituloEnUI();

            } catch (Exception e) {
                Log.e("EditorVM", "Error leyendo EPUB", e);
                mensaje.postValue("Error al abrir la obra.");
            } finally {
                isLoading.postValue(false);
            }
        }).start();
    }

    // --- 2. NAVEGACIÓN Y EDICIÓN DE CAPÍTULOS ---
    public void cambiarCapitulo(int direccion, String textoActualDelEditor) {
        guardarCapituloEnMemoria(textoActualDelEditor);
        int nuevoIndice = indiceCapituloActual + direccion;
        if (nuevoIndice >= 0 && nuevoIndice < miLibro.getSpine().size()) {
            indiceCapituloActual = nuevoIndice;
            cargarCapituloEnUI();
        }
    }

    public void anadirNuevoCapitulo(String textoActualDelEditor) {
        guardarCapituloEnMemoria(textoActualDelEditor);
        int numNuevoCap = miLibro.getSpine().size() + 1;
        String tituloNuevo = "Capítulo " + numNuevoCap;
        String htmlVacio = "<html><head><title>" + tituloNuevo + "</title></head><body><h1>" + tituloNuevo + "</h1><p></p></body></html>";

        miLibro.addSection(tituloNuevo, new Resource(htmlVacio.getBytes(StandardCharsets.UTF_8), "capitulo_" + numNuevoCap + ".html"));

        indiceCapituloActual = miLibro.getSpine().size() - 1;
        cargarCapituloEnUI();
        mensaje.postValue("Nuevo capítulo creado");
    }

    // NUEVO: Método para eliminar el último capítulo
    public void eliminarUltimoCapitulo() {
        int ultimoIndex = miLibro.getSpine().size() - 1;

        // Solo permitimos eliminar si estamos visualizando el último y si no es el único capítulo que queda
        if (ultimoIndex > 0 && indiceCapituloActual == ultimoIndex) {
            try {
                // Eliminamos el recurso del lomo (Spine)
                miLibro.getSpine().getSpineReferences().remove(ultimoIndex);

                // Retrocedemos al capítulo que ahora es el último
                indiceCapituloActual = ultimoIndex - 1;
                cargarCapituloEnUI();

                mensaje.postValue("Capítulo eliminado");
                // Importante: No guardamos físicamente aún, para que el usuario
                // confirme los cambios pulsando el botón Guardar cuando quiera.

            } catch (Exception e) {
                Log.e("EditorVM", "Error eliminando capítulo", e);
                mensaje.postValue("Error al eliminar el capítulo");
            }
        }
    }

    // --- 3. GUARDADO FÍSICO (PISAR ARCHIVO) ---
    public void guardarObraFisicamente(String textoActualDelEditor) {
        isLoading.setValue(true);
        new Thread(() -> {
            try {
                guardarCapituloEnMemoria(textoActualDelEditor);

                EpubWriter epubWriter = new EpubWriter();
                try (FileOutputStream out = new FileOutputStream(rutaArchivo)) {
                    epubWriter.write(miLibro, out);
                }

                if (idObraActual != -1) {
                    actualizarFechaEnServidor();
                } else {
                    mensaje.postValue("Guardado en el móvil (Error de ID al sincronizar)");
                    isLoading.postValue(false);
                }

            } catch (Exception e) {
                Log.e("EditorVM", "Error guardando", e);
                mensaje.postValue("Error al guardar la obra");
                isLoading.postValue(false);
            }
        }).start();
    }

    private void actualizarFechaEnServidor() {
        obra.setFecha_ultima_modificacion(new Date());
        obrasRepository.guardarObra(obra, new Callback<Obras>() {
            @Override
            public void onResponse(Call<Obras> call, Response<Obras> response) {
                if (response.isSuccessful()) {
                    mensaje.postValue("Obra guardada y sincronizada");
                } else {
                    mensaje.postValue("Guardado localmente. Error al sincronizar con el servidor.");
                }
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(Call<Obras> call, Throwable t) {
                mensaje.postValue("Guardado local. Sin conexión al servidor.");
                isLoading.postValue(false);
            }
        });
    }

    // --- UTILIDADES INTERNAS ---

    private void cargarCapituloEnUI() {
        try {
            Resource recurso = miLibro.getSpine().getResource(indiceCapituloActual);
            String htmlBruto = new String(recurso.getData(), StandardCharsets.UTF_8);

            htmlBruto = htmlBruto.replaceAll("(?is)<title>.*?</title>", "");
            htmlBruto = htmlBruto.replaceAll("(?is)<h[1-6][^>]*>.*?</h[1-6]>", "");

            String textoPlano = Html.fromHtml(htmlBruto, Html.FROM_HTML_MODE_COMPACT).toString().trim();

            String nombreEsperado = "Capítulo " + (indiceCapituloActual + 1);
            tituloCapitulo.postValue(nombreEsperado);
            contenidoCapitulo.postValue(textoPlano);

            boolean isPrimero = (indiceCapituloActual == 0);
            boolean isUltimo = (indiceCapituloActual == miLibro.getSpine().size() - 1);

            esPrimerCapitulo.postValue(isPrimero);
            esUltimoCapitulo.postValue(isUltimo);

            // Evaluamos si puede eliminar: Solo si es el último y hay más de 1 capítulo
            puedeEliminar.postValue(isUltimo && miLibro.getSpine().size() > 1);

        } catch (Exception e) {
            Log.e("EditorVM", "Error cargando capítulo", e);
        }
    }

    private void guardarCapituloEnMemoria(String textoPlano) {
        try {
            String titulo = "Capítulo " + (indiceCapituloActual + 1);

            String textoSeguro = Html.escapeHtml(textoPlano);
            String textoFormateado = textoSeguro.replace("\n", "<br>");

            String nuevoHtml = "<html><head><title>" + titulo + "</title></head><body><h2>" + titulo + "</h2><div>" + textoFormateado + "</div></body></html>";

            Resource recursoExistente = miLibro.getSpine().getResource(indiceCapituloActual);
            recursoExistente.setData(nuevoHtml.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            Log.e("EditorVM", "Error actualizando memoria", e);
        }
    }
}