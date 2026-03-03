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

public class LibrosEstanteriaViewModel extends ViewModel {
    private final LibroRepository repository = new LibroRepository();

    private final MutableLiveData<List<Libro>> librosLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<LibrosDeUsuario>> relacionesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> mensaje = new MutableLiveData<>();

    public LiveData<List<Libro>> getLibros() { return librosLiveData; }
    public LiveData<List<LibrosDeUsuario>> getRelaciones() { return relacionesLiveData; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getMensaje() { return mensaje; }

    public void cargarLibrosPorEstanteria(Context context, int idEstanteria) {
        SharedPreferences prefs = context.getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        int idU = prefs.getInt("idUsuario", -1);
        if (idU == -1) return;

        isLoading.setValue(true);
        // Usamos el repositorio que ya maneja las interfaces API
        repository.obtenerRelacionesPorEstanteria(idEstanteria, idU, new Callback<List<LibrosDeUsuario>>() {
            @Override
            public void onResponse(Call<List<LibrosDeUsuario>> call, Response<List<LibrosDeUsuario>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<LibrosDeUsuario> relaciones = response.body();
                    relacionesLiveData.postValue(relaciones);

                    if (relaciones.isEmpty()) {
                        librosLiveData.postValue(new ArrayList<>());
                        isLoading.postValue(false);
                        return;
                    }

                    List<Integer> ids = new ArrayList<>();
                    for (LibrosDeUsuario ldu : relaciones) ids.add(ldu.getId().getIdL());
                    cargarDetallesLibros(ids);
                } else {
                    isLoading.postValue(false);
                    librosLiveData.postValue(new ArrayList<>());
                }
            }
            @Override public void onFailure(Call<List<LibrosDeUsuario>> call, Throwable t) {
                isLoading.postValue(false);
            }
        });
    }

    private void cargarDetallesLibros(List<Integer> ids) {
        repository.obtenerDetallesLibros(ids, new Callback<List<Libro>>() {
            @Override
            public void onResponse(Call<List<Libro>> call, Response<List<Libro>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful()) librosLiveData.postValue(response.body());
            }
            @Override public void onFailure(Call<List<Libro>> call, Throwable t) {
                isLoading.postValue(false);
            }
        });
    }

    public void desvincularLibro(Context context, int idLibro, int idEstanteriaActual) {
        int idU = AuthRepository.getInstance(context.getApplicationContext()).getIdUsuario();

        isLoading.setValue(true);
        repository.desvincularLibro(idU, idLibro, new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && Boolean.TRUE.equals(response.body())) {
                    mensaje.postValue("Libro quitado de la estantería");
                    cargarLibrosPorEstanteria(context, idEstanteriaActual);
                } else {
                    isLoading.postValue(false);
                }
            }
            @Override public void onFailure(Call<Boolean> call, Throwable t) {
                isLoading.postValue(false);
            }
        });
    }
}