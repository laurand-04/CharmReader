package com.tfg.charmreader.viewmodel.publ.misGrupos.creados;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tfg.charmreader.data.model.Sesion;
import com.tfg.charmreader.data.repository.publ.SesionRepository;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SesionViewModel extends ViewModel {
    private final SesionRepository repository = new SesionRepository();

    private final MutableLiveData<List<Sesion>> sesiones = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> mensaje = new MutableLiveData<>();

    public LiveData<List<Sesion>> getSesiones() { return sesiones; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getMensaje() { return mensaje; }

    public void cargarSesiones(int idGrupo) {
        isLoading.setValue(true);
        repository.obtenerSesiones(idGrupo, new Callback<List<Sesion>>() {
            @Override
            public void onResponse(Call<List<Sesion>> call, Response<List<Sesion>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Sesion> todasLasSesiones = response.body();
                    List<Sesion> sesionesFuturas = new ArrayList<>();

                    Date hoy = new Date();

                    for (Sesion s : todasLasSesiones) {
                        if (s.getFecha() != null && !s.getFecha().before(hoy)) {
                            sesionesFuturas.add(s);
                        }
                    }

                    sesiones.postValue(sesionesFuturas);
                }
            }
            @Override
            public void onFailure(Call<List<Sesion>> call, Throwable t) {
                isLoading.postValue(false);
                mensaje.postValue("Error al conectar con el servidor");
            }
        });
    }

    public void eliminarSesion(Sesion s) {
        isLoading.setValue(true);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String fechaStr = sdf.format(s.getFecha());

        repository.eliminarSesion(s.getIdGrupo(), fechaStr, s.getHora(), new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    mensaje.postValue("✅ Sesión eliminada");
                    cargarSesiones(s.getIdGrupo()); // Recargamos lista
                } else {
                    isLoading.postValue(false);
                    mensaje.postValue("❌ Error al eliminar");
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                isLoading.postValue(false);
                mensaje.postValue("Error de red");
            }
        });
    }
}