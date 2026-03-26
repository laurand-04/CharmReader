package com.tfg.charmreader.viewmodel.publ.fragmentView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.tfg.charmreader.data.model.BookEn;
import com.tfg.charmreader.data.model.Votacion;
import com.tfg.charmreader.data.network.API.API;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiMiembro;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiVotacion;
import com.tfg.charmreader.data.repository.GrupoRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PropuestasFragmentViewModel extends ViewModel {

    private final MutableLiveData<List<BookEn>> listaLibros = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> totalMiembros = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final GrupoRepository grupoRepository = new GrupoRepository();
    //No se peude quitar I_ApiMiembro
    private final I_ApiMiembro apiMiembro = API.getInstancia().create(I_ApiMiembro.class);
    //No se peude quitar I_ApiVotacion
    private final I_ApiVotacion apiVotacion = API.getInstancia().create(I_ApiVotacion.class);

    public LiveData<List<BookEn>> getListaLibros() { return listaLibros; }
    public LiveData<Integer> getTotalMiembros() { return totalMiembros; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getToastMessage() { return toastMessage; }

    public I_ApiVotacion getApiVotacion() { return apiVotacion; }

    public void cargarDatos(int idGrupo) {
        isLoading.setValue(true);

        // Primero obtenemos el total de miembros
        apiMiembro.contarMiembros(idGrupo).enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful() && response.body() != null) {
                    totalMiembros.setValue(response.body().intValue());
                    cargarLibros(idGrupo);
                } else {
                    isLoading.setValue(false);
                }
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                isLoading.setValue(false);
            }
        });
    }

    private void cargarLibros(int idGrupo) {
        grupoRepository.obtenerLibroPropuestas(idGrupo, new Callback<List<BookEn>>() {
            @Override
            public void onResponse(Call<List<BookEn>> call, Response<List<BookEn>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaLibros.setValue(response.body());
                } else {
                    listaLibros.setValue(new ArrayList<>());
                }
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(Call<List<BookEn>> call, Throwable t) {
                listaLibros.setValue(new ArrayList<>());
                isLoading.setValue(false);
            }
        });
    }

    public void ejecutarVoto(int idUsuario, int idGrupo, int idLibro) {
        Votacion v = new Votacion(idUsuario, idGrupo, idLibro);
        apiVotacion.alternarVoto(v).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    toastMessage.setValue("Voto actualizado");
                    cargarDatos(idGrupo); // Refrescar datos tras votar
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                toastMessage.setValue("Error al votar");
            }
        });
    }
}