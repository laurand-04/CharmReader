package com.tfg.charmreader.viewmodel.publ.misGrupos.creados;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tfg.charmreader.data.model.GrupoLectura;
import com.tfg.charmreader.data.model.Miembro;
import com.tfg.charmreader.data.repository.publ.InfoGrupoRepository;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManejoGrupoViewModel extends ViewModel {
    private final InfoGrupoRepository repository = new InfoGrupoRepository();
    private final MutableLiveData<GrupoLectura> grupo = new MutableLiveData<>();
    private final MutableLiveData<Integer> contadorMiembros = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> mensaje = new MutableLiveData<>();
    private final MutableLiveData<Boolean> votacionCerradaExito = new MutableLiveData<>(false);

    public LiveData<GrupoLectura> getGrupo() { return grupo; }
    public LiveData<Integer> getContadorMiembros() { return contadorMiembros; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getMensaje() { return mensaje; }
    public LiveData<Boolean> getVotacionCerradaExito() { return votacionCerradaExito; }

    public void setGrupoInicial(GrupoLectura g) { grupo.setValue(g); }

    public void refrescarDatosGrupo(int idGrupo) {
        isLoading.setValue(true);
        // Simplificado para el ejemplo:
        repository.obtenerMiembros(idGrupo, new Callback<List<Miembro>>() {
            @Override
            public void onResponse(Call<List<Miembro>> call, Response<List<Miembro>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    contadorMiembros.postValue(response.body().size());
                }
            }
            @Override public void onFailure(Call<List<Miembro>> call, Throwable t) { isLoading.postValue(false); }
        });
    }

    // En ManejoGrupoViewModel.java
    private final MutableLiveData<Boolean> refreshTrigger = new MutableLiveData<>(false);

    public LiveData<Boolean> getRefreshTrigger() { return refreshTrigger; }

    public void cerrarVotaciones(int idGrupo) {
        isLoading.setValue(true);
        repository.cerrarVotaciones(idGrupo, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                isLoading.postValue(false);
                if (response.isSuccessful()) {
                    // En lugar de recrear, disparamos el refresco
                    refreshTrigger.postValue(true);
                    mensaje.postValue("¡Nueva lectura establecida!");
                } else {
                    mensaje.postValue("No hay votos suficientes");
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                isLoading.postValue(false);
            }
        });
    }

    // Método para resetear el trigger una vez consumido
    public void consumeRefresh() {
        refreshTrigger.setValue(true);
        refreshTrigger.setValue(false);
    }
}