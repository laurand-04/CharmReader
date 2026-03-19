package com.tfg.charmreader.viewmodel.autentication.perfil;

import android.content.Context;
import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tfg.charmreader.data.model.Libro;
import com.tfg.charmreader.data.model.LibrosDeUsuario;
import com.tfg.charmreader.data.model.Usuario;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import com.tfg.charmreader.data.repository.UserRepository;
import com.tfg.charmreader.data.repository.priv.LibroRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerfilViewModel extends ViewModel {
    private UserRepository userRepository;
    private LibroRepository libroRepository;
    private AuthRepository authRepository;

    private final MutableLiveData<Usuario> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> messageLiveData = new MutableLiveData<>();

    // LiveData específico para avisar a la Activity que genere el PDF
    private final MutableLiveData<PdfDataWrapper> pdfReadyData = new MutableLiveData<>();

    public void setContext(Context context) {
        if (userRepository == null) {
            this.userRepository = new UserRepository(context);
        }
        if (authRepository == null) {
            this.authRepository = AuthRepository.getInstance(context.getApplicationContext());
        }
        if(libroRepository == null){
            this.libroRepository = new LibroRepository();
        }
    }

    public LiveData<Usuario> getUserData() { return userLiveData; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getMessage() { return messageLiveData; }
    public LiveData<PdfDataWrapper> getPdfReadyData() { return pdfReadyData; }

    public void cargarDatos(String email) {
        isLoading.setValue(true);
        userRepository.obtenerUsuario(email, new retrofit2.Callback<Usuario>() {
            @Override
            public void onResponse(retrofit2.Call<Usuario> call, retrofit2.Response<Usuario> response) {
                isLoading.postValue(false);
                if (response.isSuccessful()) userLiveData.postValue(response.body());
            }
            @Override public void onFailure(retrofit2.Call<Usuario> call, Throwable t) {
                isLoading.postValue(false);
            }
        });
    }

    public void cambiarFoto(Uri uri) {
        isLoading.setValue(true);
        userRepository.subirImagen(uri, new com.cloudinary.android.callback.UploadCallback() {
            @Override public void onStart(String requestId) {}
            @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
            @Override public void onSuccess(String requestId, Map resultData) {
                String url = (String) resultData.get("secure_url");
                Usuario actual = userLiveData.getValue();
                if (actual != null) {
                    actual.setFoto(url);
                    actualizarUsuario(actual);
                }
            }
            @Override public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                isLoading.postValue(false);
                messageLiveData.postValue("Error al subir imagen");
            }
            @Override public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {}
        });
    }

    public void actualizarUsuario(Usuario usuario) {
        userRepository.actualizarUsuario(usuario, new retrofit2.Callback<Usuario>() {
            @Override
            public void onResponse(retrofit2.Call<Usuario> call, retrofit2.Response<Usuario> response) {
                isLoading.postValue(false);
                if (response.isSuccessful()) {
                    userLiveData.postValue(response.body());
                    messageLiveData.postValue("PerfilActivity actualizado");
                }
            }
            @Override public void onFailure(retrofit2.Call<Usuario> call, Throwable t) {
                isLoading.postValue(false);
            }
        });
    }

    public void prepararDatosPDF(int usuarioId) {
        isLoading.setValue(true);
        libroRepository.obtenerHistorialLectura(usuarioId, new retrofit2.Callback<List<LibrosDeUsuario>>() {
            @Override
            public void onResponse(retrofit2.Call<List<LibrosDeUsuario>> call, retrofit2.Response<List<LibrosDeUsuario>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    obtenerDetallesParaPDF(response.body());
                } else {
                    isLoading.postValue(false);
                    messageLiveData.postValue("No se encontraron lecturas");
                }
            }
            @Override public void onFailure(retrofit2.Call<List<LibrosDeUsuario>> call, Throwable t) {
                isLoading.postValue(false);
            }
        });
    }

    private void obtenerDetallesParaPDF(List<LibrosDeUsuario> relaciones) {
        List<Integer> ids = new ArrayList<>();
        for (LibrosDeUsuario r : relaciones) ids.add(r.getId().getIdL());

        libroRepository.obtenerDetallesLibros(ids, new retrofit2.Callback<List<Libro>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Libro>> call, retrofit2.Response<List<Libro>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    Map<Integer, Libro> libroMap = new HashMap<>();
                    for (Libro l : response.body()) libroMap.put(l.getId(), l);
                    pdfReadyData.postValue(new PdfDataWrapper(relaciones, libroMap));
                }
            }
            @Override public void onFailure(retrofit2.Call<List<Libro>> call, Throwable t) {
                isLoading.postValue(false);
            }
        });
    }

    public void cambiarPassword() {
        authRepository.resetearPassword(userLiveData.getValue().getCorreo(), new AuthRepository.RepositoryCallback<Void>() {
            @Override
            public void onComplete(Void result) {
                messageLiveData.postValue("Correo enviado");
            }
            @Override
            public void onError(String message) {
                messageLiveData.postValue(message);
            }
        });
    }

    // Clase envoltorio para pasar datos a la Activity
    public static class PdfDataWrapper {
        public final List<LibrosDeUsuario> relaciones;
        public final Map<Integer, Libro> libroMap;
        public PdfDataWrapper(List<LibrosDeUsuario> r, Map<Integer, Libro> m) {
            this.relaciones = r; this.libroMap = m;
        }
    }
}