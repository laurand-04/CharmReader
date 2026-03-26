package com.tfg.charmreader.viewmodel.publ.fragmentView;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tfg.charmreader.data.model.BookEn;
import com.tfg.charmreader.data.model.CatalogoLectura;
import com.tfg.charmreader.data.model.Sesion;
import com.tfg.charmreader.data.pojo.LibroHistorialUI;
import com.tfg.charmreader.data.repository.GrupoRepository;
import com.tfg.charmreader.data.repository.publ.SesionRepository;
import com.tfg.charmreader.data.repository.publ.ValoracionRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistorialFragmentViewModel extends ViewModel {
    private final GrupoRepository grupoRepository = new GrupoRepository();
    private final ValoracionRepository valoracionRepository = new ValoracionRepository();
    private final SesionRepository sesionRepository = new SesionRepository();

    // Ahora solo necesitamos una lista del nuevo objeto Wrapper
    private final MutableLiveData<List<LibroHistorialUI>> librosHistorial = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isEmpty = new MutableLiveData<>(false);

    public LiveData<List<LibroHistorialUI>> getLibrosHistorial() { return librosHistorial; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getIsEmpty() { return isEmpty; }

    public void cargarHistorial(int idGrupo) {
        isLoading.setValue(true);

        grupoRepository.obtenerHistorialCatalogo(idGrupo, new Callback<List<CatalogoLectura>>() {
            @Override
            public void onResponse(Call<List<CatalogoLectura>> call, Response<List<CatalogoLectura>> responseCata) {
                if (responseCata.isSuccessful() && responseCata.body() != null && !responseCata.body().isEmpty()) {
                    List<CatalogoLectura> listaCata = responseCata.body();

                    grupoRepository.obtenerHistorialLibros(idGrupo, new Callback<List<BookEn>>() {
                        @Override
                        public void onResponse(Call<List<BookEn>> c, Response<List<BookEn>> responseLibros) {
                            if (responseLibros.isSuccessful() && responseLibros.body() != null) {
                                List<BookEn> listaLibros = responseLibros.body();

                                // PASO EXTRA: Obtener sesiones antes de combinar
                                sesionRepository.obtenerSesiones(idGrupo, new Callback<List<Sesion>>() {
                                    @Override
                                    public void onResponse(Call<List<Sesion>> call, Response<List<Sesion>> responseSes) {
                                        List<Sesion> todasLasSesiones = responseSes.isSuccessful() ? responseSes.body() : new ArrayList<>();
                                        Log.d("HistorialFragmentViewModel", "Sesiones: " + todasLasSesiones.size());
                                        cargarMediasYCombinar(idGrupo, listaLibros, listaCata, todasLasSesiones);
                                    }
                                    @Override public void onFailure(Call<List<Sesion>> call, Throwable t) {
                                        cargarMediasYCombinar(idGrupo, listaLibros, listaCata, new ArrayList<>());
                                    }
                                });
                            } else { finalizarConError(); }
                        }
                        @Override public void onFailure(Call<List<BookEn>> c, Throwable t) { finalizarConError(); }
                    });
                } else { finalizarConError(); }
            }
            @Override public void onFailure(Call<List<CatalogoLectura>> call, Throwable t) { finalizarConError(); }
        });
    }

    private void cargarMediasYCombinar(int idGrupo, List<BookEn> listaLibros, List<CatalogoLectura> listaCata, List<Sesion> sesionesGrupo) {
        int total = listaLibros.size();
        double[] medias = new double[total];
        int[] finalizadas = {0};
        Log.d("HistorialFragmentViewModel", "Sesiones: " + sesionesGrupo.size());


        for (int i = 0; i < total; i++) {
            final int index = i;
            valoracionRepository.obtenerMediaLibro(idGrupo, listaLibros.get(i).getId(), new Callback<Double>() {
                @Override
                public void onResponse(Call<Double> call, Response<Double> response) {
                    medias[index] = (response.isSuccessful() && response.body() != null) ? response.body() : 0.0;
                    verificar();
                }
                @Override public void onFailure(Call<Double> call, Throwable t) {
                    medias[index] = 0.0;
                    verificar();
                }

                private void verificar() {
                    finalizadas[0]++;
                    if (finalizadas[0] == total) {
                        List<LibroHistorialUI> resultado = new ArrayList<>();
                        for (int j = 0; j < total; j++) {
                            CatalogoLectura cata = listaCata.get(j);

                            // Lógica de conteo de sesiones
                            int contadorSesiones = contarSesionesEnRango(sesionesGrupo, cata.getFechaComienzo(), cata.getFechaFinalizacion());
                            Log.d("HistorialFragmentViewModel", "Sesiones encontradas: " + contadorSesiones);
                            resultado.add(new LibroHistorialUI(listaLibros.get(j), cata, medias[j], contadorSesiones));
                        }
                        librosHistorial.postValue(resultado);
                        isEmpty.postValue(resultado.isEmpty());
                        isLoading.postValue(false);
                    }
                }
            });
        }
    }

    private int contarSesionesEnRango(List<Sesion> sesiones, Date inicio, Date fin) {
        if (sesiones == null || inicio == null || fin == null) return 0;

        int count = 0;

        java.time.ZoneId zone = java.time.ZoneId.systemDefault();

        java.time.LocalDate inicioDate = inicio.toInstant().atZone(zone).toLocalDate();
        java.time.LocalDate finDate = fin.toInstant().atZone(zone).toLocalDate();

        for (Sesion s : sesiones) {
            if (s.getFecha() != null) {
                java.time.LocalDate sesionDate = s.getFecha().toInstant().atZone(zone).toLocalDate();

                if ((sesionDate.isEqual(inicioDate) || sesionDate.isAfter(inicioDate)) &&
                        (sesionDate.isEqual(finDate) || sesionDate.isBefore(finDate))) {
                    count++;
                    Log.d("HistorialFragmentViewModel", "Sesión encontrada: " + sesionDate);
                }
            }
        }

        Log.d("HistorialFragmentViewModel", "Sesiones encontradas: " + count);

        return count;
    }

    private void finalizarConError() {
        isLoading.postValue(false);
        isEmpty.postValue(true);
    }
}