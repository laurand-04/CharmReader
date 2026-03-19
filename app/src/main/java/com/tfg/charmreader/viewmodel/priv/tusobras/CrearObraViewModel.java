package com.tfg.charmreader.viewmodel.priv.tusobras;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tfg.charmreader.data.model.Obras;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import com.tfg.charmreader.ui.priv.tusObras.Epub;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CrearObraViewModel extends AndroidViewModel {

    // Pool de hilos para operaciones pesadas (crear archivo, subir a BD, etc.)
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // LiveData para observar los estados desde la Activity
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> mensaje = new MutableLiveData<>();
    private final MutableLiveData<Boolean> creacionExitosa = new MutableLiveData<>(false);
    private final Epub epub = new Epub(getApplication());
    private final AuthRepository authRepository = AuthRepository.getInstance(getApplication());

    public CrearObraViewModel(@NonNull Application application) {
        super(application);
    }

    // --- GETTERS ---
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getMensaje() { return mensaje; }
    public LiveData<Boolean> getCreacionExitosa() { return creacionExitosa; }

    // --- LÓGICA DE CREACIÓN ---
    public void crearNuevaObra(String titulo, String autor, String descripcion, Uri uriPortada) {
        isLoading.setValue(true);
        executor.execute(() -> {
            epub.generarObraDesdeCero(titulo, autor, descripcion, uriPortada, authRepository.getIdUsuario(), new Epub.CrearEpubCallback() {
                @Override public void onSuccess() { creacionExitosa.postValue(true); isLoading.postValue(false); }
                @Override public void onError(String e) { mensaje.postValue(e); isLoading.postValue(false); }
            });
        });
    }

    public void modificarObra(Obras obra, String titulo, String autor, String descripcion, Uri uriPortada) {
        isLoading.setValue(true);
        executor.execute(() -> {
            epub.modificarMetadatosEpub(obra, titulo, autor, descripcion, uriPortada, new Epub.CrearEpubCallback() {
                @Override public void onSuccess() { creacionExitosa.postValue(true); isLoading.postValue(false); }
                @Override public void onError(String e) { mensaje.postValue(e); isLoading.postValue(false); }
            });
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown(); // Limpiamos recursos
    }
}
