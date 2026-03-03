package com.tfg.charmreader.viewmodel.priv.estanteria;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tfg.charmreader.data.model.Estanteria;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import com.tfg.charmreader.data.repository.priv.estanteria.EstanteriaRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EstanteriaViewModel extends ViewModel {
    private EstanteriaRepository repository;
    private final MutableLiveData<Boolean> isSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public void setContext(Context context) {
        if (repository == null) {
            repository = new EstanteriaRepository();
        }
    }

    public LiveData<Boolean> getIsSuccess() { return isSuccess; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void crearEstanteria(Context context, String titulo, String color) {
        if (titulo.isEmpty()) {
            errorMessage.setValue("El título no puede estar vacío");
            return;
        }

        int idUsuario = AuthRepository.getInstance(context.getApplicationContext()).getIdUsuario();

        if (idUsuario == -1) {
            errorMessage.setValue("Error de sesión: ID no encontrado");
            return;
        }

        isLoading.setValue(true);
        Estanteria nueva = new Estanteria(idUsuario, titulo, color);

        repository.guardarEstanteria(nueva, new Callback<Estanteria>() {
            @Override
            public void onResponse(Call<Estanteria> call, Response<Estanteria> response) {
                isLoading.postValue(false);
                if (response.isSuccessful()) {
                    isSuccess.postValue(true);
                } else {
                    errorMessage.postValue("Error al guardar en el servidor");
                }
            }

            @Override
            public void onFailure(Call<Estanteria> call, Throwable t) {
                isLoading.postValue(false);
                errorMessage.postValue("Fallo de red: " + t.getMessage());
            }
        });
    }
}