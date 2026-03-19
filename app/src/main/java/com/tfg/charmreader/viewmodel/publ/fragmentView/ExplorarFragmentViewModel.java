package com.tfg.charmreader.viewmodel.publ.fragmentView;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel; // Cambiado de ViewModel a AndroidViewModel
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.tfg.charmreader.data.model.GrupoLectura;
import com.tfg.charmreader.data.model.Usuario;
import com.tfg.charmreader.data.repository.GrupoRepository;
import com.tfg.charmreader.data.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExplorarFragmentViewModel extends AndroidViewModel { // Hereda de AndroidViewModel

    private final GrupoRepository repository;
    private final UserRepository repositoryUser;

    private final MutableLiveData<List<GrupoLectura>> grupos = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Usuario>> usuariosPublicos = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<GrupoLectura> grupoEncontrado = new MutableLiveData<>();
    private final MutableLiveData<String> mensajeError = new MutableLiveData<>();

    public ExplorarFragmentViewModel(@NonNull Application application) {
        super(application);
        // Inicializamos los repositorios pasando el contexto de la aplicación
        this.repository = new GrupoRepository();
        this.repositoryUser = new UserRepository(application.getApplicationContext());
    }

    public LiveData<List<GrupoLectura>> getGrupos() { return grupos; }
    public LiveData<List<Usuario>> getUsuariosPublicos() { return usuariosPublicos; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<GrupoLectura> getGrupoEncontrado() { return grupoEncontrado; }
    public LiveData<String> getMensajeError() { return mensajeError; }

    public void cargarDatosExplorar() {
        cargarGrupos();
        cargarUsuarios();
    }

    public void cargarGrupos() {
        isLoading.setValue(true);
        repository.obtenerGrupos(new Callback<List<GrupoLectura>>() {
            @Override
            public void onResponse(Call<List<GrupoLectura>> call, Response<List<GrupoLectura>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    grupos.postValue(response.body());
                }
            }
            @Override public void onFailure(Call<List<GrupoLectura>> call, Throwable t) {
                isLoading.postValue(false);
            }
        });
    }

    public void cargarUsuarios() {
        isLoading.setValue(true);
        repositoryUser.obtenerTodosLosUsuarios(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    usuariosPublicos.postValue(response.body());
                }
            }
            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                isLoading.postValue(false);
            }
        });
    }

    public void buscarGrupoPorNombre(String query) {
        isLoading.setValue(true);
        repository.buscarPorNombre(query, new Callback<GrupoLectura>() {
            @Override
            public void onResponse(Call<GrupoLectura> call, Response<GrupoLectura> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    grupoEncontrado.postValue(response.body());
                } else {
                    mensajeError.postValue("No se ha encontrado el grupo");
                }
            }
            @Override public void onFailure(Call<GrupoLectura> call, Throwable t) {
                isLoading.postValue(false);
                mensajeError.postValue("Error de conexión");
            }
        });
    }
}