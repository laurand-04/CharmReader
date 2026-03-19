package com.tfg.charmreader.viewmodel.autentication.perfil.publico;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.tfg.charmreader.data.model.Libro;
import com.tfg.charmreader.data.model.LibrosDeUsuario;
import com.tfg.charmreader.data.model.Obras;
import com.tfg.charmreader.data.repository.priv.LibroRepository;
import com.tfg.charmreader.data.repository.priv.tusObras.ObrasRepository;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilPublicoViewModel extends AndroidViewModel {

    private final LibroRepository libroRepo = new LibroRepository();
    private final ObrasRepository obrasRepo = new ObrasRepository();

    private final MutableLiveData<List<LibrosDeUsuario>> ultimasLecturas = new MutableLiveData<>();
    private final MutableLiveData<List<Libro>> detallesUltimasLecturas = new MutableLiveData<>();
    private final MutableLiveData<List<Libro>> detallesObrasPublicadas = new MutableLiveData<>();
    private final MutableLiveData<List<Obras>> obrasPublicadas = new MutableLiveData<>();

    public PerfilPublicoViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<LibrosDeUsuario>> getUltimasLecturas() { return ultimasLecturas; }
    public LiveData<List<Libro>> getDetallesUltimasLecturas() { return detallesUltimasLecturas; }
    public LiveData<List<Libro>> getDetallesObrasPublicadas() { return detallesObrasPublicadas; }
    public LiveData<List<Obras>> getObrasPublicadas() { return obrasPublicadas; }

    public void cargarUltimasLecturas(int idUsuario) {
        libroRepo.obtenerHistorialLectura(idUsuario, new Callback<List<LibrosDeUsuario>>() {
            @Override
            public void onResponse(Call<List<LibrosDeUsuario>> call, Response<List<LibrosDeUsuario>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<LibrosDeUsuario> listaRelaciones = response.body();

                    // Limitamos a 3
                    List<LibrosDeUsuario> filtrada = (listaRelaciones.size() > 3)
                            ? listaRelaciones.subList(0, 3) : listaRelaciones;

                    ultimasLecturas.postValue(filtrada);

                    // Extraer IDs para obtener los detalles de los libros
                    List<Integer> ids = new ArrayList<>();
                    for (LibrosDeUsuario ldu : filtrada) {
                        ids.add(ldu.getId().getIdL());
                    }

                    if (!ids.isEmpty()) {
                        cargarDetallesLibros(ids);
                    }
                }
            }
            @Override public void onFailure(Call<List<LibrosDeUsuario>> call, Throwable t) {}
        });
    }

    private void cargarDetallesLibros(List<Integer> ids) {
        libroRepo.obtenerDetallesLibros(ids, new Callback<List<Libro>>() {
            @Override
            public void onResponse(Call<List<Libro>> call, Response<List<Libro>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    detallesUltimasLecturas.postValue(response.body());
                }
            }
            @Override public void onFailure(Call<List<Libro>> call, Throwable t) {}
        });
    }

    public void cargarObrasPublicadas(int idUsuario) {
        obrasRepo.obtenerObrasDeUsuario(idUsuario, new Callback<List<Obras>>() {
            @Override
            public void onResponse(Call<List<Obras>> call, Response<List<Obras>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Obras> listaObras = response.body();
                    android.util.Log.d("PERFIL_PUBLICO", "filtro 2: " + listaObras.size());

                    obrasPublicadas.postValue(listaObras);

                    // 1. Extraer IDs de libros válidos (no nulos y mayores a 0)
                    List<Integer> ids = new ArrayList<>();
                    for (Obras obra : listaObras) {
                        if (obra.getIdLibro() > 0) {
                            ids.add(obra.getIdLibro());
                        }
                    }

                    // 2. Si hay IDs, pedimos los detalles de los libros
                    if (!ids.isEmpty()) {
                        cargarDetallesObras(ids);
                    } else {
                        detallesObrasPublicadas.postValue(new ArrayList<>());
                    }
                }
            }
            @Override public void onFailure(Call<List<Obras>> call, Throwable t) {}
        });
    }

    private void cargarDetallesObras(List<Integer> ids) {
        libroRepo.obtenerDetallesLibros(ids, new Callback<List<Libro>>() {
            @Override
            public void onResponse(Call<List<Libro>> call, Response<List<Libro>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    detallesObrasPublicadas.postValue(response.body());
                }
            }
            @Override public void onFailure(Call<List<Libro>> call, Throwable t) {}
        });
    }
}