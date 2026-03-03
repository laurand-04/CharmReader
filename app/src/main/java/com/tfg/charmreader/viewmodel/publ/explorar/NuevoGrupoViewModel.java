package com.tfg.charmreader.viewmodel.publ.explorar;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.tfg.charmreader.data.model.GrupoLectura;
import com.tfg.charmreader.data.model.Miembro;
import com.tfg.charmreader.data.network.API.CloudinaryClient;
import com.tfg.charmreader.data.repository.GrupoRepository;
import com.tfg.charmreader.data.repository.publ.InfoGrupoRepository;

import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NuevoGrupoViewModel extends ViewModel {
    private final GrupoRepository grupoRepository = new GrupoRepository();
    private final InfoGrupoRepository infoGrupoRepository = new InfoGrupoRepository();


    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<String> mensaje = new MutableLiveData<>();

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getIsSuccess() { return isSuccess; }
    public LiveData<String> getMensaje() { return mensaje; }

    public void crearNuevoGrupo(String nombre, String ubicacion, String desc, String frecuencia, Uri imagenUri, int idU) {
        isLoading.setValue(true);

        if (imagenUri != null) {
            subirACloudinary(imagenUri, url -> procederCrear(nombre, ubicacion, desc, frecuencia, url, idU));
        } else {
            procederCrear(nombre, ubicacion, desc, frecuencia, "https://res.cloudinary.com/tu_cloud/image/upload/default_group.png", idU);
        }
    }

    private void subirACloudinary(Uri uri, CloudinaryUrlCallback callback) {
        CloudinaryClient.nuevoUpload(uri).callback(new UploadCallback() {
            @Override public void onSuccess(String r, Map res) { callback.onUrlReady((String) res.get("secure_url")); }
            @Override public void onError(String r, ErrorInfo e) {
                mensaje.postValue("Error al subir imagen");
                isLoading.postValue(false);
            }
            @Override public void onStart(String r) {}
            @Override public void onProgress(String r, long b, long t) {}
            @Override public void onReschedule(String r, ErrorInfo e) {}
        }).dispatch();
    }

    private void procederCrear(String n, String u, String d, String f, String url, int idU) {
        GrupoLectura.Frecuencia frecuenciaEnum = GrupoLectura.stringToFrecuencia(f);
        GrupoLectura nuevo = new GrupoLectura(n, u, d, frecuenciaEnum, url, idU);

        grupoRepository.crearGrupo(nuevo, new Callback<GrupoLectura>() {
            @Override
            public void onResponse(Call<GrupoLectura> call, Response<GrupoLectura> response) {
                if (response.isSuccessful() && response.body() != null) {
                    suscribirAdmin(response.body().getIdGrupo(), idU);
                } else {
                    mensaje.postValue("Error al crear el grupo en el servidor");
                    isLoading.postValue(false);
                }
            }
            @Override public void onFailure(Call<GrupoLectura> call, Throwable t) {
                mensaje.postValue("Fallo de red");
                isLoading.postValue(false);
            }
        });
    }

    private void suscribirAdmin(int idG, int idU) {
        infoGrupoRepository.unirse(new Miembro(idG, idU), new Callback<Miembro>() {
            @Override public void onResponse(Call<Miembro> c, Response<Miembro> r) {
                isLoading.postValue(false);
                isSuccess.postValue(true);
            }
            @Override public void onFailure(Call<Miembro> c, Throwable t) {
                isLoading.postValue(false);
                isSuccess.postValue(true); // El grupo se creó, aunque falló la auto-suscripción
            }
        });
    }

    interface CloudinaryUrlCallback { void onUrlReady(String url); }
}