package com.tfg.charmreader.viewmodel.autentication.perfil.publico;

import android.app.Application;
import android.util.Log;

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
        Log.d("DEBUG_PERFIL", "PASO 1: Llamando a cargarObrasPublicadas para ID: " + idUsuario);
        detallesObrasPublicadas.postValue(null);

        obrasRepo.obtenerObrasDeUsuario(idUsuario, new Callback<List<Obras>>() {
            @Override
            public void onResponse(Call<List<Obras>> call, Response<List<Obras>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Obras> listaObras = response.body();
                    Log.d("DEBUG_PERFIL", "PASO 2: Obras recibidas del servidor. Cantidad: " + listaObras.size());

                    List<Integer> ids = new ArrayList<>();
                    for (Obras obra : listaObras) {
                        Log.d("DEBUG_PERFIL", "Obra encontrada: ID_Libro = " + obra.getIdLibro());
                        if (obra.isPublicado()) {
                            ids.add(obra.getIdLibro());
                        }
                    }

                    if (!ids.isEmpty()) {
                        Log.d("DEBUG_PERFIL", "PASO 3: Intentando cargar detalles para " + ids.size() + " libros.");
                        cargarDetallesObras(ids);
                    } else {
                        Log.w("DEBUG_PERFIL", "PASO 3 (FALLO): La lista de obras no tiene IDs de libros válidos.");
                        detallesObrasPublicadas.postValue(new ArrayList<>());
                    }
                } else {
                    Log.e("DEBUG_PERFIL", "PASO 2 (ERROR): Respuesta no exitosa. Código: " + response.code());
                    detallesObrasPublicadas.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<Obras>> call, Throwable t) {
                Log.e("DEBUG_PERFIL", "PASO 2 (FALLO TOTAL): Error de red o crash: " + t.getMessage());
                detallesObrasPublicadas.postValue(new ArrayList<>());
            }
        });
    }

    private void cargarDetallesObras(List<Integer> ids) {
        Log.d("DEBUG_PERFIL", "PASO 4: Enviando petición de detalles a libroRepo...");
        libroRepo.obtenerDetallesLibros(ids, new Callback<List<Libro>>() {
            @Override
            public void onResponse(Call<List<Libro>> call, Response<List<Libro>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("DEBUG_PERFIL", "PASO 5: Detalles de libros recibidos. Cantidad: " + response.body().size());
                    detallesObrasPublicadas.postValue(response.body());
                } else {
                    Log.e("DEBUG_PERFIL", "PASO 5 (ERROR): Fallo al obtener detalles. Código: " + response.code());
                    detallesObrasPublicadas.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<Libro>> call, Throwable t) {
                Log.e("DEBUG_PERFIL", "PASO 5 (FALLO TOTAL): Error en detalles: " + t.getMessage());
                detallesObrasPublicadas.postValue(new ArrayList<>());
            }
        });
    }
}