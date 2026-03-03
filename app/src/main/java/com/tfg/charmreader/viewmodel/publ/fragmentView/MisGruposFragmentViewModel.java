package com.tfg.charmreader.viewmodel.publ.fragmentView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tfg.charmreader.data.model.GrupoLectura;
import com.tfg.charmreader.data.repository.GrupoRepository;
import com.tfg.charmreader.data.repository.publ.InfoGrupoRepository;

import java.util.ArrayList;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MisGruposFragmentViewModel extends ViewModel {

    private final MutableLiveData<List<GrupoLectura>> listaSuscritos = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<GrupoLectura>> listaCreados = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private final InfoGrupoRepository infoGrupoRepository = new InfoGrupoRepository();
    private final GrupoRepository grupoRepository = new GrupoRepository();

    private int peticionesEnCurso = 0;

    public LiveData<List<GrupoLectura>> getListaSuscritos() { return listaSuscritos; }
    public LiveData<List<GrupoLectura>> getListaCreados() { return listaCreados; }
    public LiveData<String> getToastMessage() { return toastMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void cargarDatos(int idUsuario) {
        if (idUsuario <= 0) return;

        isLoading.setValue(true);
        peticionesEnCurso = 2;

        // 1. Cargar Grupos donde el usuario es miembro
        infoGrupoRepository.obtenerGruposDondeEsMiembro(idUsuario, new Callback<List<GrupoLectura>>() {
            @Override
            public void onResponse(Call<List<GrupoLectura>> call, Response<List<GrupoLectura>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaSuscritos.postValue(response.body());
                }
                decrementarCarga();
            }
            @Override
            public void onFailure(Call<List<GrupoLectura>> call, Throwable t) {
                decrementarCarga();
            }
        });

        // 2. Cargar Grupos creados por el usuario
        grupoRepository.obtenerGruposPorAdmin(idUsuario, new Callback<List<GrupoLectura>>() {
            @Override
            public void onResponse(Call<List<GrupoLectura>> call, Response<List<GrupoLectura>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaCreados.postValue(response.body());
                }
                decrementarCarga();
            }
            @Override
            public void onFailure(Call<List<GrupoLectura>> call, Throwable t) {
                decrementarCarga();
            }
        });
    }

    public void gestionarSalidaAdmin(int idGrupo, int idUsuario) {
        grupoRepository.gestionarSalidaAdmin(idGrupo, idUsuario, new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String res = response.body().string().replace("\"", "");
                        if (res.equalsIgnoreCase("CEDIDO")) {
                            toastMessage.postValue("Control cedido correctamente");
                        } else if (res.equalsIgnoreCase("ELIMINADO")) {
                            toastMessage.postValue("Grupo eliminado permanentemente");
                        }
                        cargarDatos(idUsuario);
                    }
                } catch (Exception e) {
                    toastMessage.postValue("Error al procesar respuesta");
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                toastMessage.postValue("Error de red");
            }
        });
    }

    private void decrementarCarga() {
        peticionesEnCurso--;
        if (peticionesEnCurso <= 0) {
            isLoading.postValue(false);
        }
    }
}