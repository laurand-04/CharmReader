package com.tfg.charmreader.viewmodel.publ.misGrupos.creados;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tfg.charmreader.data.model.Sesion;
import com.tfg.charmreader.data.repository.publ.SesionRepository;
import java.util.Calendar;
import java.util.Date;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NuevaSesionViewModel extends ViewModel {
    private final SesionRepository repository = new SesionRepository();

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getIsSuccess() { return isSuccess; }
    public LiveData<String> getError() { return error; }

    public void guardarSesion(int idGrupo, Date fecha, String hora, String capIniStr, String capFinStr) {
        if (fecha == null || hora.isEmpty() || capIniStr.isEmpty() || capFinStr.isEmpty()) {
            error.setValue("⚠️ Por favor, rellena todos los campos");
            return;
        }

        int capIni = Integer.parseInt(capIniStr);
        int capFin = Integer.parseInt(capFinStr);

        if (capFin <= capIni) {
            error.setValue("❌ El capítulo final debe ser mayor al inicial");
            return;
        }

        Calendar hoy = Calendar.getInstance();
        hoy.set(Calendar.HOUR_OF_DAY, 0); hoy.set(Calendar.MINUTE, 0);
        hoy.set(Calendar.SECOND, 0); hoy.set(Calendar.MILLISECOND, 0);

        if (fecha.before(hoy.getTime())) {
            error.setValue("❌ No puedes programar una sesión en el pasado");
            return;
        }

        isLoading.setValue(true);
        Sesion nueva = new Sesion();
        nueva.setIdGrupo(idGrupo);
        nueva.setFecha(fecha);
        nueva.setHora(hora);
        nueva.setCapituloInicio(capIni);
        nueva.setCapituloFinalizacion(capFin);

        repository.nuevaSesion(nueva, new Callback<Sesion>() {
            @Override
            public void onResponse(Call<Sesion> call, Response<Sesion> response) {
                isLoading.postValue(false);
                if (response.isSuccessful()) isSuccess.postValue(true);
                else error.postValue("Error del servidor al guardar");
            }

            @Override
            public void onFailure(Call<Sesion> call, Throwable t) {
                isLoading.postValue(false);
                error.postValue("Error de red");
            }
        });
    }
}