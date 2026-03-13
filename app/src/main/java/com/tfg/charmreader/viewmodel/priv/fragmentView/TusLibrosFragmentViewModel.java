package com.tfg.charmreader.viewmodel.priv.fragmentView;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.tfg.charmreader.data.model.Libro;
import com.tfg.charmreader.data.model.LibrosDeUsuario;
import com.tfg.charmreader.data.repository.priv.LibroRepository;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TusLibrosFragmentViewModel extends AndroidViewModel {

    private final LibroRepository libroRepo = new LibroRepository();
    private final MutableLiveData<List<Libro>> librosDetalle = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<LibrosDeUsuario>> relacionesUsuario = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> mensaje = new MutableLiveData<>();

    public TusLibrosFragmentViewModel(@NonNull Application application) { super(application); }

    public LiveData<List<Libro>> getLibrosDetalle() { return librosDetalle; }
    public LiveData<List<LibrosDeUsuario>> getRelacionesUsuario() { return relacionesUsuario; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getMensaje() { return mensaje; }

    public void cargarBiblioteca() {
        int idU = AuthRepository.getInstance(getApplication()).getIdUsuario();
        if (idU <= 0) return;

        isLoading.setValue(true);
        // Paso 1: Obtener relaciones de usuario (progreso/fechas)
        libroRepo.obtenerHistorialLectura(idU, new Callback<List<LibrosDeUsuario>>() {
            @Override
            public void onResponse(Call<List<LibrosDeUsuario>> call, Response<List<LibrosDeUsuario>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<LibrosDeUsuario> relaciones = response.body();
                    relacionesUsuario.postValue(relaciones);

                    if (relaciones.isEmpty()) {
                        librosDetalle.postValue(new ArrayList<>());
                        isLoading.postValue(false);
                        return;
                    }

                    // Paso 2: Extraer IDs y pedir detalles de los libros
                    List<Integer> ids = relaciones.stream()
                            .map(ldu -> ldu.getId().getIdL())
                            .collect(Collectors.toList());

                    libroRepo.obtenerDetallesLibros(ids, new Callback<List<Libro>>() {
                        @Override
                        public void onResponse(Call<List<Libro>> c, Response<List<Libro>> r) {
                            isLoading.postValue(false);
                            if (r.isSuccessful()) librosDetalle.postValue(r.body());
                        }
                        @Override public void onFailure(Call<List<Libro>> c, Throwable t) { isLoading.postValue(false); }
                    });
                } else {
                    isLoading.postValue(false);
                }
            }
            @Override public void onFailure(Call<List<LibrosDeUsuario>> call, Throwable t) { isLoading.postValue(false); }
        });
    }

    public void reiniciarLectura(LibrosDeUsuario ldu) {
        ldu.setCapitulo(0);
        ldu.setScroll(0f);
        ldu.setFechaFin(null);
        libroRepo.actualizarProgreso(ldu, new Callback<LibrosDeUsuario>() {
            @Override
            public void onResponse(Call<LibrosDeUsuario> call, Response<LibrosDeUsuario> response) {
                if (response.isSuccessful()) {
                    mensaje.postValue("Lectura reiniciada");
                    cargarBiblioteca();
                }
            }
            @Override public void onFailure(Call<LibrosDeUsuario> call, Throwable t) {}
        });
    }

    public void eliminarLibro(int idLibro) {
        int idU = AuthRepository.getInstance(getApplication()).getIdUsuario();
        libroRepo.eliminarLibro(idU, idLibro, new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    mensaje.postValue("Libro eliminado");
                    cargarBiblioteca();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }
}