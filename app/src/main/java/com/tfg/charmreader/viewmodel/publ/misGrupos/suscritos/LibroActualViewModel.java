package com.tfg.charmreader.viewmodel.publ.misGrupos.suscritos;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.tfg.charmreader.data.model.BookEn;
import com.tfg.charmreader.data.model.Valoracion;
import com.tfg.charmreader.data.network.API.GeminiService;
import com.tfg.charmreader.data.repository.publ.ValoracionRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibroActualViewModel extends ViewModel {

    private final MutableLiveData<BookEn> libro = new MutableLiveData<>();
    private final MutableLiveData<String> coverUrl = new MutableLiveData<>();
    private final MutableLiveData<String> mensaje = new MutableLiveData<>();

    // NUEVOS LIVEDATA PARA LA IA
    private final MutableLiveData<String> resumenIA = new MutableLiveData<>();
    private final MutableLiveData<String> mediaValoracion = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cargandoIA = new MutableLiveData<>();

    private final ValoracionRepository valoracionRepository = new ValoracionRepository();
    private final GeminiService geminiService = new GeminiService();
    private boolean yaCargado = false;

    // Getters para la UI
    public LiveData<BookEn> getLibro() { return libro; }
    public LiveData<String> getCoverUrl() { return coverUrl; }
    public LiveData<String> getMensaje() { return mensaje; }
    public LiveData<String> getResumenIA() { return resumenIA; }
    public LiveData<String> getMediaValoracion() { return mediaValoracion; }
    public LiveData<Boolean> getCargandoIA() { return cargandoIA; }

    public void setLibro(BookEn book) {
        libro.setValue(book);
        if (book != null && book.getCoverId() != null && !book.getCoverId().isEmpty()) {
            coverUrl.setValue("https://covers.openlibrary.org/b/id/" + book.getCoverId() + "-L.jpg");
        }
    }

    public void cargarComentariosYGenerarResumen(String titulo) {
        if (yaCargado) return;
        yaCargado = true;

        cargandoIA.setValue(true);
        resumenIA.setValue("Analizando reseñas de la comunidad...");
        Log.d("DEBUG_VALORACIONES", "Entrando en cargarComentariosYGenerarResumen");

        valoracionRepository.verValoracionesTitulo(titulo, new Callback<List<Valoracion>>() {
            @Override
            public void onResponse(Call<List<Valoracion>> call, Response<List<Valoracion>> response) {
                Log.d("DEBUG_VALORACIONES", "onResponse llamado");

                if (response.isSuccessful() && response.body() != null) {
                    List<Valoracion> lista = response.body();

                    if (lista.isEmpty()) {
                        mediaValoracion.postValue("0.0");
                        resumenIA.postValue("Aún no hay reseñas para este libro.");
                        cargandoIA.postValue(false);
                        return;
                    }

                    // 1. Calculamos la media matemática
                    double media = valoracionRepository.calcularMedia(lista);
                    mediaValoracion.postValue(String.format("%.1f", media));

                    // 2. Preparamos el prompt y llamamos a Gemini con Retrofit
                    String prompt = geminiService.generarPrompt(lista);
                    Log.d("DEBUG_VALORACIONES", "Antes de llamar a GeminiService");

                    geminiService.generarTexto(prompt, new GeminiService.GeminiCallback() {
                        @Override
                        public void onSuccess(String respuestaIA) {
                            resumenIA.postValue(respuestaIA);
                            cargandoIA.postValue(false);
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("GeminiError", "Error llamando a Gemini: " + e.getMessage());
                            resumenIA.postValue("No se pudo generar el resumen.");
                            cargandoIA.postValue(false);
                        }
                    });

                } else {
                    resumenIA.postValue("Error al obtener las valoraciones.");
                    cargandoIA.postValue(false);
                }
            }

            @Override
            public void onFailure(Call<List<Valoracion>> call, Throwable t) {
                Log.e("DEBUG_VALORACIONES", "onFailure: " + t.getMessage());
                resumenIA.postValue("Error de conexión con el servidor.");
                cargandoIA.postValue(false);
            }
        });
    }

    public void descargarLibro(BookEn book) {
        if (book.getUrlLibro() == null || book.getUrlLibro().isEmpty()) {
            mensaje.setValue("Este libro no tiene EPUB disponible");
            return;
        }
        // aquí se lanzará la descarga real
        mensaje.setValue("Iniciando descarga...");
    }
}