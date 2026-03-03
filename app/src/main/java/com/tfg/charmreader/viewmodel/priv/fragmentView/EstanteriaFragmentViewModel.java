package com.tfg.charmreader.viewmodel.priv.fragmentView;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.tfg.charmreader.data.model.Estanteria;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import com.tfg.charmreader.data.repository.priv.LibroRepository;
import com.tfg.charmreader.data.repository.priv.estanteria.EstanteriaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EstanteriaFragmentViewModel extends AndroidViewModel {

    // Inyectamos el repositorio
    private final EstanteriaRepository estanteriaRepository = new EstanteriaRepository();
    private final LibroRepository libroRepository = new LibroRepository();

    private final MutableLiveData<List<Estanteria>> estanterias = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();

    public EstanteriaFragmentViewModel(@NonNull Application application) { super(application); }

    public LiveData<List<Estanteria>> getEstanterias() { return estanterias; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getToastMessage() { return toastMessage; }

    public void cargarEstanterias() {
        int idUsuario = AuthRepository.getInstance(getApplication()).getIdUsuario();
        if (idUsuario <= 0) return;

        isLoading.setValue(true);
        estanteriaRepository.obtenerEstanteriasDeUsuario(idUsuario, new Callback<List<Estanteria>>() {
            @Override
            public void onResponse(Call<List<Estanteria>> call, Response<List<Estanteria>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cargarConteos(response.body());
                } else {
                    isLoading.postValue(false);
                    estanterias.postValue(new ArrayList<>());
                }
            }
            @Override public void onFailure(Call<List<Estanteria>> call, Throwable t) {
                isLoading.postValue(false);
            }
        });
    }

    private void cargarConteos(List<Estanteria> lista) {
        if (lista.isEmpty()) {
            estanterias.postValue(lista);
            isLoading.postValue(false);
            return;
        }

        AtomicInteger pendientes = new AtomicInteger(lista.size());
        for (Estanteria e : lista) {
            libroRepository.contarLibrosEnEstanteria(e.getId(), new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> res) {
                    if (res.isSuccessful() && res.body() != null) e.setCantidadLibros(res.body());
                    if (pendientes.decrementAndGet() == 0) {
                        estanterias.postValue(lista);
                        isLoading.postValue(false);
                    }
                }
                @Override public void onFailure(Call<Integer> call, Throwable t) {
                    if (pendientes.decrementAndGet() == 0) {
                        estanterias.postValue(lista);
                        isLoading.postValue(false);
                    }
                }
            });
        }
    }

    public void eliminarEstanteria(Estanteria e) {
        estanteriaRepository.eliminarEstanteria(e.getId(), new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && Boolean.TRUE.equals(response.body())) {
                    toastMessage.postValue("Estantería eliminada");
                    cargarEstanterias();
                } else {
                    toastMessage.postValue("Error al eliminar");
                }
            }
            @Override public void onFailure(Call<Boolean> call, Throwable t) {
                toastMessage.postValue("Fallo de red");
            }
        });
    }
}