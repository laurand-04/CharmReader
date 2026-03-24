package com.tfg.charmreader.viewmodel.priv.fragmentView;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tfg.charmreader.data.model.Obras;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import com.tfg.charmreader.data.repository.priv.tusObras.ObrasRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TusObrasFragmentViewModel extends AndroidViewModel {

    private final ObrasRepository obrasRepo = new ObrasRepository();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<List<Obras>> obras = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> mensaje = new MutableLiveData<>();
    private final MutableLiveData<Boolean> archivoListoParaOperar = new MutableLiveData<>(false);

    public TusObrasFragmentViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Obras>> getObras() { return obras; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getMensaje() { return mensaje; }
    public LiveData<Boolean> getArchivoListoParaOperar() { return archivoListoParaOperar; }

    public void cargarObras() {
        int idU = AuthRepository.getInstance(getApplication()).getIdUsuario();
        if (idU <= 0) return;
        isLoading.setValue(true);
        obrasRepo.obtenerObrasDeUsuario(idU, new Callback<List<Obras>>() {
            @Override
            public void onResponse(Call<List<Obras>> call, Response<List<Obras>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) obras.postValue(response.body());
                else obras.postValue(new ArrayList<>());
            }
            @Override
            public void onFailure(Call<List<Obras>> call, Throwable t) {
                isLoading.postValue(false);
                mensaje.postValue("Error de conexión");
            }
        });
    }

    // --- LÓGICA DE VERIFICACIÓN INICIAL (Hacer click en la lista) ---
    public void verificarYDescargarObra(Obras obra) {
        if (obra.getRuta() == null) return;
        File file = new File(obra.getRuta());
        if (file.exists()) {
            archivoListoParaOperar.setValue(true);
        } else {
            sincronizarObraDesdeNube(obra); // Si no existe, forzamos descarga
        }
    }

    // --- SINCRONIZACIÓN (BAJAR DE LA NUBE Y PISAR LOCAL) ---
    public void sincronizarObraDesdeNube(Obras obra) {
        if (obra.getUrl_obra() == null || obra.getUrl_obra().isEmpty()) {
            mensaje.setValue("Esta obra no tiene copia en la nube.");
            return;
        }

        isLoading.postValue(true); // DISPARA LA RULETA EN EL FRAGMENT

        executor.execute(() -> {
            try {
                File destino = new File(obra.getRuta());
                if (destino.exists()) destino.delete();

                File parent = destino.getParentFile();
                if (parent != null && !parent.exists()) parent.mkdirs();

                InputStream is = new URL(obra.getUrl_obra()).openStream();
                FileOutputStream os = new FileOutputStream(destino);

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.close();
                is.close();

                // ÉXITO
                isLoading.postValue(false); // QUITA LA RULETA
                mensaje.postValue("Obra sincronizada correctamente");

                // Si quieres que tras sincronizar se abra el editor automáticamente:
                // archivoListoParaOperar.postValue(true);

            } catch (Exception e) {
                Log.e("TusObrasVM", "Error en sincronización", e);
                isLoading.postValue(false); // QUITA LA RULETA AUNQUE HAYA ERROR
                mensaje.postValue("Error al descargar de la nube");
            }
        });
    }

    public void resetArchivoStatus() { archivoListoParaOperar.setValue(false); }

    public void eliminarObra(int idObra) {
        obrasRepo.eliminarObra(idObra, new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) { mensaje.postValue("Obra eliminada"); cargarObras(); }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {}
        });
    }

    public void cambiarEstadoObra(Obras obra, boolean marcarComoFinalizado) {
        isLoading.setValue(true);
        obra.setFinalizado(marcarComoFinalizado);
        obrasRepo.guardarObra(obra, new Callback<Obras>() {
            @Override
            public void onResponse(Call<Obras> call, Response<Obras> response) {
                isLoading.postValue(false);
                if (response.isSuccessful()) {
                    mensaje.postValue(marcarComoFinalizado ? "Obra finalizada" : "Borrador");
                    cargarObras();
                }
            }
            @Override
            public void onFailure(Call<Obras> call, Throwable t) { isLoading.postValue(false); }
        });
    }

    public void descargarObra(Obras obra) {
        isLoading.setValue(true);
        new Thread(() -> {
            try {
                File fileOrigen = new File(obra.getRuta());
                String nombreArchivo = (obra.getNombre() != null ? obra.getNombre() : "obra") + ".epub";
                Context context = getApplication().getApplicationContext();
                boolean exito = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo);
                    values.put(MediaStore.Downloads.MIME_TYPE, "application/epub+zip");
                    values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                    Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                    if (uri != null) {
                        try (OutputStream os = context.getContentResolver().openOutputStream(uri);
                             FileInputStream is = new FileInputStream(fileOrigen)) {
                            byte[] buffer = new byte[1024]; int len;
                            while ((len = is.read(buffer)) > 0) os.write(buffer, 0, len);
                            exito = true;
                        }
                    }
                } else {
                    File d = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    File f = new File(d, nombreArchivo);
                    try (FileInputStream is = new FileInputStream(fileOrigen);
                         OutputStream os = new FileOutputStream(f)) {
                        byte[] b = new byte[1024]; int l;
                        while ((l = is.read(b)) > 0) os.write(b, 0, l);
                        exito = true;
                    }
                }
                mensaje.postValue(exito ? "Exportado a Descargas" : "Error al exportar");
            } catch (Exception e) { mensaje.postValue("Error de exportación"); }
            finally { isLoading.postValue(false); }
        }).start();
    }

    @Override
    protected void onCleared() { super.onCleared(); executor.shutdown(); }
}