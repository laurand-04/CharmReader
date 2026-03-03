package com.tfg.charmreader.viewmodel.admin;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tfg.charmreader.data.model.Usuario;
import com.tfg.charmreader.data.repository.UserRepository;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GestionUsuariosViewModel extends ViewModel {
    private UserRepository repository;
    private final MutableLiveData<List<Usuario>> usuariosLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> messageLiveData = new MutableLiveData<>();

    public void setContext(Context context) {
        if (repository == null) {
            this.repository = new UserRepository(context);
        }
    }

    public LiveData<List<Usuario>> getUsuarios() { return usuariosLiveData; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getMessage() { return messageLiveData; }

    public void cargarUsuarios() {
        isLoading.setValue(true);
        repository.obtenerTodosLosUsuarios(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful()) {
                    usuariosLiveData.postValue(response.body());
                } else {
                    messageLiveData.postValue("No se pudieron cargar los usuarios");
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                isLoading.postValue(false);
                messageLiveData.postValue("Error de red");
            }
        });
    }

    public void eliminarUsuario(Usuario usuario) {
        isLoading.setValue(true);
        repository.eliminarUsuario(usuario.getId(), new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                isLoading.postValue(false);
                if (response.isSuccessful()) {
                    List<Usuario> listaActual = usuariosLiveData.getValue();
                    if (listaActual != null) {
                        listaActual.remove(usuario);
                        usuariosLiveData.postValue(listaActual);
                    }
                    messageLiveData.postValue("Usuario eliminado con éxito");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isLoading.postValue(false);
                messageLiveData.postValue("Fallo al conectar con el servidor");
            }
        });
    }
}