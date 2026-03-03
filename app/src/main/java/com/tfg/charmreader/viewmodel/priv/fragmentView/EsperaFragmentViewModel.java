package com.tfg.charmreader.viewmodel.priv.fragmentView;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.tfg.charmreader.data.model.LibrosSinEstrenar;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import com.tfg.charmreader.data.repository.priv.futuro.FuturoRepository;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EsperaFragmentViewModel extends AndroidViewModel {
    private final FuturoRepository repository = new FuturoRepository();

    private final MutableLiveData<List<LibrosSinEstrenar>> libros = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> mensaje = new MutableLiveData<>();

    public EsperaFragmentViewModel(@NonNull Application application) { super(application); }

    public LiveData<List<LibrosSinEstrenar>> getLibros() { return libros; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getMensaje() { return mensaje; }

    public void cargarLibros() {
        int idUsuario = AuthRepository.getInstance(getApplication()).getIdUsuario();
        if (idUsuario <= 0) return;

        isLoading.setValue(true);
        repository.obtenerLibrosPorUsuario(idUsuario, new Callback<ArrayList<LibrosSinEstrenar>>() {
            @Override
            public void onResponse(Call<ArrayList<LibrosSinEstrenar>> call, Response<ArrayList<LibrosSinEstrenar>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    libros.postValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<ArrayList<LibrosSinEstrenar>> call, Throwable t) {
                isLoading.postValue(false);
                mensaje.postValue("Error al cargar lanzamientos");
            }
        });
    }

    public void eliminarLibro(LibrosSinEstrenar libro) {
        // No ponemos isLoading a true aquí para no parpadear la lista entera,
        // pero podrías hacerlo si prefieres bloquear la UI.
        repository.eliminarLibro(libro.getId(), new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null && response.body().equalsIgnoreCase("Bien")) {
                    mensaje.postValue("Lanzamiento eliminado con éxito");
                    cargarLibros(); // Refrescamos automáticamente
                } else {
                    mensaje.postValue("No se pudo eliminar el registro");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                mensaje.postValue("Error de red");
            }
        });
    }
}