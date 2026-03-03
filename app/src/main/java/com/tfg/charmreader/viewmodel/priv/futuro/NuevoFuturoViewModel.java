package com.tfg.charmreader.viewmodel.priv.futuro;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tfg.charmreader.data.model.CCLibrosSinEstrenar;
import com.tfg.charmreader.data.model.LibrosSinEstrenar;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import com.tfg.charmreader.data.repository.priv.futuro.FuturoRepository;
import java.util.Date;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NuevoFuturoViewModel extends ViewModel {
    private final FuturoRepository repository = new FuturoRepository();

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Date> fechaSeleccionada = new MutableLiveData<>();

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getIsSuccess() { return isSuccess; }
    public LiveData<String> getError() { return error; }
    public LiveData<Date> getFechaSeleccionada() { return fechaSeleccionada; }

    public void setFecha(Date fecha) { fechaSeleccionada.setValue(fecha); }

    public void guardarNuevoLibro(Context context, String titulo, String autor) {
        if (titulo.isEmpty() || autor.isEmpty() || fechaSeleccionada.getValue() == null) {
            error.setValue("Por favor, rellena todos los datos");
            return;
        }

        int idUsuario = AuthRepository.getInstance(context.getApplicationContext()).getIdUsuario();

        if (idUsuario == -1) {
            error.setValue("Error de sesión");
            return;
        }

        isLoading.setValue(true);

        CCLibrosSinEstrenar clave = new CCLibrosSinEstrenar(idUsuario, titulo);
        LibrosSinEstrenar nuevo = new LibrosSinEstrenar();
        nuevo.setId(clave);
        nuevo.setAutor(autor);
        nuevo.setFechaPublicacion(fechaSeleccionada.getValue());

        repository.actualizarLibro(nuevo, new Callback<LibrosSinEstrenar>() {
            @Override
            public void onResponse(Call<LibrosSinEstrenar> call, Response<LibrosSinEstrenar> response) {
                isLoading.postValue(false);
                if (response.isSuccessful()) isSuccess.postValue(true);
                else error.postValue("Error al guardar el lanzamiento");
            }

            @Override
            public void onFailure(Call<LibrosSinEstrenar> call, Throwable t) {
                isLoading.postValue(false);
                error.postValue("Fallo de conexión");
            }
        });
    }
}