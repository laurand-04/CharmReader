package com.tfg.charmreader.viewmodel.publ.misGrupos.creados;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tfg.charmreader.data.model.GrupoLectura;
import com.tfg.charmreader.data.model.Usuario;
import com.tfg.charmreader.data.repository.GrupoRepository;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarGrupoViewModel extends ViewModel {
    private final GrupoRepository repository = new GrupoRepository();

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<GrupoLectura> grupoActualizado = new MutableLiveData<>();
    private final MutableLiveData<String> mensaje = new MutableLiveData<>();

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<GrupoLectura> getGrupoActualizado() { return grupoActualizado; }
    public LiveData<String> getMensaje() { return mensaje; }

    public void actualizarGrupo(GrupoLectura grupo, String nombre, String ubicacion, String desc, String frecuencia) {
        if (nombre.isEmpty()) {
            mensaje.setValue("El nombre es obligatorio");
            return;
        }

        isLoading.setValue(true);
        grupo.setNombre(nombre);
        grupo.setUbicacion(ubicacion);
        grupo.setDescripcion(desc);
        grupo.setFrecuenciaReunion(GrupoLectura.Frecuencia.valueOf(frecuencia));

        repository.actualizarGrupo(grupo, new Callback<GrupoLectura>() {
            @Override
            public void onResponse(Call<GrupoLectura> call, Response<GrupoLectura> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    grupoActualizado.postValue(response.body());
                } else {
                    mensaje.postValue("Error al actualizar");
                }
            }

            @Override
            public void onFailure(Call<GrupoLectura> call, Throwable t) {
                isLoading.postValue(false);
                mensaje.postValue("Error de red");
            }
        });
    }

    public void actualizarGrupo(GrupoLectura grupo) {
        repository.actualizarGrupo(grupo, new retrofit2.Callback<>() {
            @Override
            public void onResponse(retrofit2.Call<GrupoLectura> call, retrofit2.Response<GrupoLectura> response) {
                isLoading.postValue(false);
                if (response.isSuccessful()) {
                    grupoActualizado.postValue(response.body());
                }
            }
            @Override public void onFailure(retrofit2.Call<GrupoLectura> call, Throwable t) {
                isLoading.postValue(false);
            }
        });
    }
    public void cambiarFoto(Uri uri, GrupoLectura grupo) {
        isLoading.setValue(true);
        repository.subirImagen(uri, new com.cloudinary.android.callback.UploadCallback() {
            @Override public void onStart(String requestId) {}
            @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
            @Override public void onSuccess(String requestId, Map resultData) {
                String url = (String) resultData.get("secure_url");

                    grupo.setUrl(url);
                    actualizarGrupo(grupo);

            }
            @Override public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                isLoading.postValue(false);
                mensaje.postValue("Error al subir imagen");
            }
            @Override public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {}
        });
    }


    public boolean detectarCambios(GrupoLectura original, String n, String u, String d, String f) {
        if (original == null) return false;
        return !n.equals(original.getNombre()) ||
                !u.equals(original.getUbicacion()) ||
                !d.equals(original.getDescripcion()) ||
                !f.equals(original.getFrecuenciaReunion().name());
    }
}