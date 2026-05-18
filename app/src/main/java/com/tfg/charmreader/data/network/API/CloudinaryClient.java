package com.tfg.charmreader.data.network.API;

import android.content.Context;
import android.net.Uri;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.tfg.charmreader.viewmodel.priv.tuslibros.CargarLibroViewModel;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CloudinaryClient {

    private static boolean inicializado = false;

    public interface CloudinaryCallback {
        void onUrl(String url);
        void onError(String mensajeError); // Añadimos esto para manejar errores de forma limpia
    }

    /**
     * Inicializa la configuración de Cloudinary.
     * Asegúrate de reemplazar los valores con tus credenciales del Dashboard de Cloudinary.
     */
    public static void init(Context context) {
        if (!inicializado) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dsmxhglte"); // Reemplazar
            config.put("api_key", "?");       // Reemplazar
            config.put("api_secret", "?-Pg"); // Reemplazar

            MediaManager.init(context, config);
            inicializado = true;
        }
    }

    /**
     * Prepara una solicitud de subida optimizada.
     * @param uri URI de la imagen seleccionada.
     * @return UploadRequest configurado.
     */
    public static com.cloudinary.android.UploadRequest nuevoUpload(Uri uri) {
        return MediaManager.get().upload(uri)
                .option("folder", "perfiles")
                // IMPORTANTE: Debes crear un 'Upload Preset' tipo 'Unsigned' en Cloudinary
                .option("unsigned_upload_preset", "tu_preset_name")
                .option("transformation", new com.cloudinary.Transformation()
                        .width(500)
                        .height(500)
                        .crop("fill")
                        .gravity("face")); // Centra el recorte en la cara si la detecta
    }

    public static com.cloudinary.android.UploadRequest nuevoUpload(byte[] bytes) {
        return MediaManager.get().upload(bytes) // Acepta el array de bytes directamente
                .option("folder", "portadas_libros")
                .option("unsigned_upload_preset", "tfg_preset")
                .option("transformation", new com.cloudinary.Transformation()
                        .width(400)
                        .height(600)
                        .crop("fill")); // Formato típico de portada de libro
    }

    public static com.cloudinary.android.UploadRequest nuevoUpload(String path) {
        return MediaManager.get().upload(path)
                .option("folder", "libros_tfg")
                // IMPORTANTE: Para archivos que no son imagen/video
                .option("resource_type", "raw")
                // Asegúrate de usar un preset que permita subidas 'raw' si es unsigned
                .option("unsigned_upload_preset", "tfg_preset");
    }

    public static com.cloudinary.android.UploadRequest nuevoUpload(java.io.File file) {
        return nuevoUpload(file.getAbsolutePath());
    }

    /**
     * Sube una imagen en formato byte[] a Cloudinary.
     */
    public static void subirImagenCloudinary(byte[] data, CloudinaryCallback cb) {
        if (data == null) {
            cb.onUrl("");
            return;
        }

        // Usamos el métod de preparación que ya tenías
        nuevoUpload(data).callback(new UploadCallback() {
            @Override public void onSuccess(String requestId, Map resultData) {
                cb.onUrl((String) resultData.get("secure_url"));
            }
            @Override public void onError(String requestId, ErrorInfo error) {
                cb.onError(error.getDescription());
            }
            @Override public void onStart(String requestId) {}
            @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
            @Override public void onReschedule(String requestId, ErrorInfo error) {}
        }).dispatch();
    }

    /**
     * Sube un archivo bruto (Raw como EPUB o PDF) a Cloudinary.
     */
    public static void subirArchivoRawCloudinary(File file, CloudinaryCallback cb) {
        if (file == null || !file.exists()) {
            cb.onError("El archivo no existe o es nulo");
            return;
        }

        // Usamos el métod de preparación que ya tenías, pero le añadimos
        // las opciones específicas que estaban en el ViewModel
        nuevoUpload(file.getAbsolutePath())
                .option("resource_type", "raw") // IMPRESCINDIBLE
                .option("use_filename", true)   // Mantiene nombre original
                .callback(new UploadCallback() {
                    @Override public void onSuccess(String requestId, Map resultData) {
                        cb.onUrl((String) resultData.get("secure_url"));
                    }
                    @Override public void onError(String requestId, ErrorInfo error) {
                        cb.onError("Error al subir el archivo a la nube: " + error.getDescription());
                    }
                    @Override public void onStart(String requestId) {}
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }
}
