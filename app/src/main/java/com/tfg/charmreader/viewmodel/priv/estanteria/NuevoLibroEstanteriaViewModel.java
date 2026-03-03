package com.tfg.charmreader.viewmodel.priv.estanteria;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tfg.charmreader.data.model.Libro;
import com.tfg.charmreader.data.model.LibrosDeUsuario;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import com.tfg.charmreader.data.repository.priv.LibroRepository;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NuevoLibroEstanteriaViewModel extends ViewModel {
    private final LibroRepository repository = new LibroRepository();

    private final MutableLiveData<List<Libro>> librosDetalles = new MutableLiveData<>();
    private final MutableLiveData<List<LibrosDeUsuario>> relaciones = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> operacionExitosa = new MutableLiveData<>(false);

    public LiveData<List<Libro>> getLibrosDetalles() { return librosDetalles; }
    public LiveData<List<LibrosDeUsuario>> getRelaciones() { return relaciones; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getOperacionExitosa() { return operacionExitosa; }

    public void cargarLibrosDisponibles(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);
        if (idUsuario == -1) return;

        isLoading.setValue(true);
        repository.obtenerLibrosSinEstanteria(idUsuario, new Callback<List<LibrosDeUsuario>>() {
            @Override
            public void onResponse(Call<List<LibrosDeUsuario>> call, Response<List<LibrosDeUsuario>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<LibrosDeUsuario> relacionesObtenidas = response.body();
                    relaciones.postValue(relacionesObtenidas);

                    // Extraer IDs para la segunda llamada
                    List<Integer> ids = new ArrayList<>();
                    for (LibrosDeUsuario r : relacionesObtenidas) ids.add(r.getId().getIdL());
                    cargarDetalles(ids);
                } else {
                    isLoading.postValue(false);
                    librosDetalles.postValue(new ArrayList<>()); // Lista vacía para mostrar layoutEmpty
                }
            }
            @Override
            public void onFailure(Call<List<LibrosDeUsuario>> call, Throwable t) {
                isLoading.postValue(false);
            }
        });
    }

    private void cargarDetalles(List<Integer> ids) {
        repository.obtenerDetallesLibros(ids, new Callback<List<Libro>>() {
            @Override
            public void onResponse(Call<List<Libro>> call, Response<List<Libro>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful()) librosDetalles.postValue(response.body());
            }
            @Override
            public void onFailure(Call<List<Libro>> call, Throwable t) {
                isLoading.postValue(false);
            }
        });
    }

    public void asignarLibro(Context context, int idLibro, int idEstanteria) {
        int idUsuario = AuthRepository.getInstance(context.getApplicationContext()).getIdUsuario();

        isLoading.setValue(true);
        repository.asignarAEstanteria(idUsuario, idLibro, idEstanteria, new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) operacionExitosa.postValue(response.body());
            }
            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                isLoading.postValue(false);
            }
        });
    }
}