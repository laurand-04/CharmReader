package com.tfg.charmreader.viewmodel.priv.estanteria;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tfg.charmreader.data.model.LibrosDeUsuario;
import com.tfg.charmreader.data.repository.priv.LibroRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ValoracionViewModel extends ViewModel {
    private final LibroRepository repository = new LibroRepository();

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getIsSuccess() { return isSuccess; }
    public LiveData<String> getError() { return error; }

    public void guardarValoracion(LibrosDeUsuario libro, float estrellas, String descripcion) {
        isLoading.setValue(true);

        libro.setValoracion((double) estrellas);
        libro.setDescripcion(descripcion);

        repository.actualizarProgreso(libro, new Callback<LibrosDeUsuario>() {
            @Override
            public void onResponse(Call<LibrosDeUsuario> call, Response<LibrosDeUsuario> response) {
                isLoading.postValue(false);
                if (response.isSuccessful()) {
                    isSuccess.postValue(true);
                } else {
                    error.postValue("Error al guardar la valoración");
                }
            }

            @Override
            public void onFailure(Call<LibrosDeUsuario> call, Throwable t) {
                isLoading.postValue(false);
                error.postValue("Error de conexión con el servidor");
            }
        });
    }

    public boolean hayCambios(LibrosDeUsuario original, float nuevasEstrellas, String nuevaDesc) {
        if (original == null) return false;
        return original.getValoracion() != (double) nuevasEstrellas ||
                !original.getDescripcion().equals(nuevaDesc);
    }
}