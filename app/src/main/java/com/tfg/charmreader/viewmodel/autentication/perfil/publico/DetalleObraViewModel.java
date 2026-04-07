package com.tfg.charmreader.viewmodel.autentication.perfil.publico;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tfg.charmreader.data.model.CCLibrosDeUsuario;
import com.tfg.charmreader.data.model.LibrosDeUsuario;
import com.tfg.charmreader.data.model.Obras;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import com.tfg.charmreader.data.repository.priv.LibroRepository;
import com.tfg.charmreader.data.repository.priv.tusObras.ObrasRepository;

import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleObraViewModel extends AndroidViewModel {

    private final ObrasRepository obrasRepo;
    private final LibroRepository libroRepo;
    private final AuthRepository authRepo;

    private final MutableLiveData<Obras> obraLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> descargaExitosa = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    // Usamos AndroidViewModel para poder acceder al Application Context
    public DetalleObraViewModel(@NonNull Application application) {
        super(application);
        // Inicializamos los repositorios pasando el contexto donde sea necesario
        this.obrasRepo = new ObrasRepository();
        this.libroRepo = new LibroRepository();
        // AuthRepository suele ser Singleton o requerir contexto para SharedPreferences
        this.authRepo = AuthRepository.getInstance(application.getApplicationContext());
    }

    public LiveData<Obras> getObraLiveData() { return obraLiveData; }
    public LiveData<Boolean> getDescargaExitosa() { return descargaExitosa; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void cargarDetalleObra(int idLibro) {
        isLoading.postValue(true);
        obrasRepo.obtenerObraPorIdLibro(idLibro, new Callback<Obras>() {
            @Override
            public void onResponse(Call<Obras> call, Response<Obras> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    obraLiveData.postValue(response.body());
                } else {
                    errorLiveData.postValue("Error al cargar detalles de la obra");
                }
            }
            @Override
            public void onFailure(Call<Obras> call, Throwable t) {
                isLoading.postValue(false);
                errorLiveData.postValue("Error de conexión al cargar obra");
            }
        });
    }

    public void registrarDescarga(Obras obra) {
        if (obra == null) return;

        // 1. Borramos el archivo si ya existe (re-descarga)
        boolean existiaPreviamente = eliminarArchivoSiExiste(obra.getRuta());
        isLoading.postValue(true);

        int idUsuario = authRepo.getIdUsuario();

        if (existiaPreviamente) {
            // FLUJO A: Ya existía en la biblioteca.
            // Pedimos el progreso actual para no machacarlo (no perder la página donde iba el usuario).
            libroRepo.obtenerProgreso(idUsuario, obra.getIdLibro(), new Callback<LibrosDeUsuario>() {
                @Override
                public void onResponse(Call<LibrosDeUsuario> call, Response<LibrosDeUsuario> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        LibrosDeUsuario relacion = response.body();

                        // IMPORTANTE: Le asignamos la ruta de la obra, NO null.
                        relacion.setRuta(obra.getRuta());

                        // Ahora que tenemos el objeto completo, actualizamos.
                        ejecutarActualizacion(relacion);
                    } else {
                        isLoading.postValue(false);
                        errorLiveData.postValue("Error al recuperar progreso previo");
                    }
                }

                @Override
                public void onFailure(Call<LibrosDeUsuario> call, Throwable t) {
                    isLoading.postValue(false);
                    errorLiveData.postValue("Error de red al recuperar progreso");
                }
            });
        } else {
            // FLUJO B: Es la primera vez que se descarga.
            // Creamos una relación desde cero con progreso 0.
            CCLibrosDeUsuario idRelacion = new CCLibrosDeUsuario(idUsuario, obra.getIdLibro());
            LibrosDeUsuario nuevaRelacion = new LibrosDeUsuario(idRelacion, 0, obra.getRuta(), 0, 0, 0, null);

            ejecutarActualizacion(nuevaRelacion);
        }
    }

    // Métod auxiliar para evitar repetir código de actualización
    private void ejecutarActualizacion(LibrosDeUsuario relacion) {
        libroRepo.actualizarProgreso(relacion, new Callback<LibrosDeUsuario>() {
            @Override
            public void onResponse(Call<LibrosDeUsuario> call, Response<LibrosDeUsuario> response) {
                isLoading.postValue(false);
                if (response.isSuccessful()) {
                    descargaExitosa.postValue(true);
                } else {
                    errorLiveData.postValue("Error al añadir el libro a la biblioteca");
                }
            }

            @Override
            public void onFailure(Call<LibrosDeUsuario> call, Throwable t) {
                isLoading.postValue(false);
                errorLiveData.postValue("Error de conexión al registrar descarga");
            }
        });
    }

    private boolean eliminarArchivoSiExiste(String rutaCompleta) {
        if (rutaCompleta == null || rutaCompleta.isEmpty()) {
            Log.w("FILESYSTEM", "La ruta proporcionada está vacía o es nula.");
            return false;
        }

        try {
            // Creamos el objeto File directamente con la ruta absoluta de la BD
            File archivo = new File(rutaCompleta);

            if (archivo.exists()) {
                boolean borrado = archivo.delete();
                if (borrado) {
                    Log.d("FILESYSTEM", "Archivo eliminado correctamente en: " + rutaCompleta);
                    return true;
                } else {
                    Log.e("FILESYSTEM", "No se pudo eliminar el archivo en: " + rutaCompleta);
                    return false;
                }
            } else {
                Log.d("FILESYSTEM", "El archivo no existe físicamente, no hay nada que borrar.");
                return false;
            }
        } catch (Exception e) {
            Log.e("FILESYSTEM", "Error crítico al intentar borrar el archivo: " + e.getMessage());
            return false;
        }
    }
}