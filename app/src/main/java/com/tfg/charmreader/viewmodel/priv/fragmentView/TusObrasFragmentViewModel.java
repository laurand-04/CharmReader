package com.tfg.charmreader.viewmodel.priv.fragmentView;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tfg.charmreader.data.model.Obras;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import com.tfg.charmreader.data.repository.priv.tusObras.ObrasRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TusObrasFragmentViewModel extends AndroidViewModel {

    private final ObrasRepository obrasRepo = new ObrasRepository();

    // Solo necesitamos una lista, ya que Obras contiene todos los datos necesarios
    private final MutableLiveData<List<Obras>> obras = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> mensaje = new MutableLiveData<>();

    public TusObrasFragmentViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Obras>> getObras() { return obras; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getMensaje() { return mensaje; }

    public void cargarObras() {
        int idU = AuthRepository.getInstance(getApplication()).getIdUsuario();
        if (idU <= 0) return;

        isLoading.setValue(true);

        // Hacemos una única llamada para obtener las obras del usuario
        obrasRepo.obtenerObrasDeUsuario(idU, new Callback<List<Obras>>() {
            @Override
            public void onResponse(Call<List<Obras>> call, Response<List<Obras>> response) {
                isLoading.postValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    obras.postValue(response.body());
                } else if (response.code() == 204) { // 204 No Content (La lista está vacía)
                    obras.postValue(new ArrayList<>());
                } else {
                    mensaje.postValue("Error al cargar tus obras");
                    obras.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<Obras>> call, Throwable t) {
                isLoading.postValue(false);
                mensaje.postValue("Error de conexión. Inténtalo de nuevo.");
            }
        });
    }

    public void eliminarObra(int idObra) {
        // Para obras no necesitamos el idU en la ruta del servidor, ya que el id de la obra es único
        obrasRepo.eliminarObra(idObra, new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    mensaje.postValue("Obra eliminada correctamente");
                    // Recargamos la lista para que desaparezca visualmente
                    cargarObras();
                } else {
                    mensaje.postValue("No se pudo eliminar la obra");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                mensaje.postValue("Error de conexión al eliminar");
            }
        });
    }

    public void cambiarEstadoObra(Obras obra, boolean marcarComoFinalizado) {
        android.util.Log.d("API_DEBUG", "Enviando ID: " + obra.getId() + " Estado: " + marcarComoFinalizado);
        isLoading.setValue(true); // Mostramos carga mientras sincronizamos con la API

        // 1. Actualizamos el estado en el objeto local
        obra.setFinalizado(marcarComoFinalizado);

        // 2. Llamamos al repositorio para persistir el cambio
        obrasRepo.guardarObra(obra, new Callback<Obras>() { // Asumo que devuelve Obras o ResponseBody
            @Override
            public void onResponse(Call<Obras> call, Response<Obras> response) {
                isLoading.postValue(false);

                if (response.isSuccessful()) {
                    String estadoStr = marcarComoFinalizado ? "Obra finalizada" : "Obra movida a borradores";
                    mensaje.postValue(estadoStr);
                    android.util.Log.d("API_DEBUG", "Respuesta exitosa del servidor");
                    // 3. Recargamos la lista para que el adaptador refresque la vista
                    // y los filtros (Chips) funcionen correctamente
                    cargarObras();
                } else {
                    android.util.Log.e("API_DEBUG", "Error servidor: " + response.code());
                    // Si falla el servidor, revertimos el cambio localmente para no engañar al usuario
                    obra.setFinalizado(!marcarComoFinalizado);
                    mensaje.postValue("No se pudo actualizar el estado en el servidor");
                }
            }

            @Override
            public void onFailure(Call<Obras> call, Throwable t) {
                isLoading.postValue(false);
                // Revertimos cambio local por error de red
                obra.setFinalizado(!marcarComoFinalizado);
                mensaje.postValue("Error de conexión. El estado no se guardó.");
            }
        });
    }

    public void descargarObra(Obras obra) {
        isLoading.setValue(true);

        // Usamos un hilo para no bloquear la UI durante la copia del archivo
        new Thread(() -> {
            try {
                File fileOrigen = new File(obra.getRuta());
                if (!fileOrigen.exists()) {
                    mensaje.postValue("El archivo original no existe en el dispositivo.");
                    isLoading.postValue(false);
                    return;
                }

                String nombreArchivo = obra.getNombre() + ".epub";
                Context context = getApplication().getApplicationContext();
                boolean exito;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Lógica para Android 10+ (MediaStore)
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo);
                    values.put(MediaStore.Downloads.MIME_TYPE, "application/epub+zip");
                    values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                    Uri uriDestino = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

                    if (uriDestino != null) {
                        try (OutputStream os = context.getContentResolver().openOutputStream(uriDestino);
                             FileInputStream is = new FileInputStream(fileOrigen)) {
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = is.read(buffer)) > 0) {
                                os.write(buffer, 0, len);
                            }
                            exito = true;
                        }
                    } else {
                        exito = false;
                    }
                } else {
                    // Lógica para Android 9 y anteriores
                    File descargas = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    File fileDestino = new File(descargas, nombreArchivo);

                    try (FileInputStream is = new FileInputStream(fileOrigen);
                         OutputStream os = new java.io.FileOutputStream(fileDestino)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = is.read(buffer)) > 0) {
                            os.write(buffer, 0, len);
                        }
                        exito = true;
                    }
                }

                if (exito) {
                    mensaje.postValue("Libro descargado en la carpeta Descargas");
                } else {
                    mensaje.postValue("No se pudo completar la descarga");
                }

            } catch (Exception e) {
                android.util.Log.e("DESCARGA", "Error: " + e.getMessage());
                mensaje.postValue("Error al exportar el archivo");
            } finally {
                isLoading.postValue(false);
            }
        }).start();
    }
}