package com.tfg.charmreader.viewmodel.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tfg.charmreader.data.model.GrupoLectura;
import com.tfg.charmreader.data.repository.GrupoRepository;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GestionGruposViewModel extends ViewModel {
    private final GrupoRepository repository = new GrupoRepository();

    private final MutableLiveData<List<GrupoLectura>> gruposLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> messageLiveData = new MutableLiveData<>();

    public LiveData<List<GrupoLectura>> getGrupos() { return gruposLiveData; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getMessage() { return messageLiveData; }

    public void cargarGrupos() {
        isLoading.setValue(true);
        repository.obtenerGrupos(new Callback<List<GrupoLectura>>() {
            @Override
            public void onResponse(Call<List<GrupoLectura>> call, Response<List<GrupoLectura>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful()) {
                    gruposLiveData.postValue(response.body());
                } else {
                    messageLiveData.postValue("No se encontraron grupos");
                }
            }

            @Override
            public void onFailure(Call<List<GrupoLectura>> call, Throwable t) {
                isLoading.postValue(false);
                messageLiveData.postValue("Error de conexión");
            }
        });
    }

    public void eliminarGrupo(GrupoLectura grupo) {
        isLoading.setValue(true);
        repository.eliminarGrupo(grupo.getIdGrupo(), new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                isLoading.postValue(false);
                if (response.isSuccessful()) {
                    List<GrupoLectura> actual = gruposLiveData.getValue();
                    if (actual != null) {
                        actual.remove(grupo);
                        gruposLiveData.postValue(actual);
                    }
                    messageLiveData.postValue("Grupo eliminado correctamente");
                } else {
                    messageLiveData.postValue("Error al eliminar el grupo");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isLoading.postValue(false);
                messageLiveData.postValue("Error de red");
            }
        });
    }
}