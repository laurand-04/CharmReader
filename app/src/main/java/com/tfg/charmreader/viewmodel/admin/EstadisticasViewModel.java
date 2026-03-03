package com.tfg.charmreader.viewmodel.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.tfg.charmreader.data.repository.admin.AdminRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EstadisticasViewModel extends ViewModel {
    private final AdminRepository repository = new AdminRepository();
    private final MutableLiveData<EstadisticasState> statsState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public LiveData<EstadisticasState> getStatsState() {
        return statsState;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void cargarEstadisticas() {
        isLoading.setValue(true);
        EstadisticasState state = new EstadisticasState();

        // Iniciamos la cadena de peticiones (1/6)
        repository.getTotalUsuarios(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful()) state.totalUsuarios = response.body();
                fetchLecturasActivas(state);
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                fetchLecturasActivas(state);
            }
        });
    }

    private void fetchLecturasActivas(EstadisticasState state) { // (2/6)
        repository.getLecturasActivas(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful()) state.lecturasActivas = response.body();
                fetchGrupoTop(state);
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                fetchGrupoTop(state);
            }
        });
    }

    private void fetchGrupoTop(EstadisticasState state) { // (3/6)
        repository.getNombreGrupoTop(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) state.grupoTop = response.body();
                fetchDensidad(state);
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                fetchDensidad(state);
            }
        });
    }

    private void fetchDensidad(EstadisticasState state) { // (4/6)
        repository.getDensidad(new Callback<Double>() {
            @Override
            public void onResponse(Call<Double> call, Response<Double> response) {
                if (response.isSuccessful()) state.densidad = response.body();
                fetchTiempoMedio(state);
            }

            @Override
            public void onFailure(Call<Double> call, Throwable t) {
                fetchTiempoMedio(state);
            }
        });
    }

    private void fetchTiempoMedio(EstadisticasState state) { // (5/6)
        repository.getTiempoMedio(new Callback<Double>() {
            @Override
            public void onResponse(Call<Double> call, Response<Double> response) {
                if (response.isSuccessful()) state.tiempoMedio = response.body();
                fetchFinalizadosMes(state);
            }

            @Override
            public void onFailure(Call<Double> call, Throwable t) {
                fetchFinalizadosMes(state);
            }
        });
    }

    private void fetchFinalizadosMes(EstadisticasState state) { // (6/6)
        repository.getFinalizadosMes(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful()) state.finalizadosMes = response.body();
                // FINAL DE LA CADENA
                statsState.postValue(state);
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                statsState.postValue(state);
                isLoading.postValue(false);
            }
        });
    }

    public static class EstadisticasState {
        public Long totalUsuarios = 0L;
        public Long lecturasActivas = 0L;
        public String grupoTop = "Sin datos";
        public Double densidad = 0.0;
        public Double tiempoMedio = 0.0;
        public Long finalizadosMes = 0L;
    }
}