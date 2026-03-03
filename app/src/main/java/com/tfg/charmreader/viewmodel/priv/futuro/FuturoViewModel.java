package com.tfg.charmreader.viewmodel.priv.futuro;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tfg.charmreader.data.model.LibrosSinEstrenar;
import com.tfg.charmreader.data.repository.priv.futuro.FuturoRepository;
import java.util.Date;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FuturoViewModel extends ViewModel {
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

    public void guardarCambios(LibrosSinEstrenar libro, String titulo, String autor) {
        if (titulo.isEmpty() || autor.isEmpty() || fechaSeleccionada.getValue() == null) {
            error.setValue("Completa todos los campos");
            return;
        }

        isLoading.setValue(true);
        libro.setAutor(autor);
        libro.setFechaPublicacion(fechaSeleccionada.getValue());
        libro.getId().setNombre(titulo);

        repository.actualizarLibro(libro, new Callback<LibrosSinEstrenar>() {
            @Override
            public void onResponse(Call<LibrosSinEstrenar> call, Response<LibrosSinEstrenar> response) {
                isLoading.postValue(false);
                if (response.isSuccessful()) isSuccess.postValue(true);
                else error.postValue("Error al actualizar");
            }

            @Override
            public void onFailure(Call<LibrosSinEstrenar> call, Throwable t) {
                isLoading.postValue(false);
                error.postValue("Error de conexión");
            }
        });
    }

    public boolean haHabidoCambios(LibrosSinEstrenar original, String t, String a) {
        if (original == null) return false;
        boolean tituloIgual = t.equals(original.getId().getNombre());
        boolean autorIgual = a.equals(original.getAutor());
        boolean fechaIgual = fechaSeleccionada.getValue() != null &&
                fechaSeleccionada.getValue().equals(original.getFechaPublicacion());
        return !tituloIgual || !autorIgual || !fechaIgual;
    }
}