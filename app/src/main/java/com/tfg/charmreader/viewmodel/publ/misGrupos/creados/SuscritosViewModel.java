package com.tfg.charmreader.viewmodel.publ.misGrupos.creados;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tfg.charmreader.data.model.Miembro;
import com.tfg.charmreader.data.repository.publ.InfoGrupoRepository;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SuscritosViewModel extends ViewModel {
    private final InfoGrupoRepository repository = new InfoGrupoRepository();

    private final MutableLiveData<List<Miembro>> miembros = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> mensaje = new MutableLiveData<>();

    public LiveData<List<Miembro>> getMiembros() { return miembros; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getMensaje() { return mensaje; }

    public void cargarMiembros(int idGrupo) {
        isLoading.setValue(true);
        repository.obtenerMiembros(idGrupo, new Callback<List<Miembro>>() {
            @Override
            public void onResponse(Call<List<Miembro>> call, Response<List<Miembro>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    miembros.postValue(response.body());
                }
            }
            @Override
            public void onFailure(Call<List<Miembro>> call, Throwable t) {
                isLoading.postValue(false);
                mensaje.postValue("Error al cargar la lista de miembros");
            }
        });
    }

    public void expulsarUsuario(int idGrupo, int idUsuario) {
        isLoading.setValue(true);
        repository.salir(idGrupo, idUsuario, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful() || response.code() == 204) {
                    mensaje.postValue("Usuario expulsado con éxito");
                    cargarMiembros(idGrupo); // Refrescar lista automáticamente
                } else {
                    isLoading.postValue(false);
                    mensaje.postValue("Error al intentar expulsar");
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isLoading.postValue(false);
                mensaje.postValue("Fallo de red");
            }
        });
    }
}