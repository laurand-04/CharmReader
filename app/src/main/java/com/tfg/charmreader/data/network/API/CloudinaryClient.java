package com.tfg.charmreader.data.network.API;

import android.content.Context;
import android.net.Uri;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class CloudinaryClient {

    private static boolean inicializado = false;

    /**
     * Inicializa la configuración de Cloudinary.
     * Asegúrate de reemplazar los valores con tus credenciales del Dashboard de Cloudinary.
     */
    public static void init(Context context) {
        if (!inicializado) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dsmxhglte"); // Reemplazar
            config.put("api_key", "174419754634384");       // Reemplazar
            config.put("api_secret", "mGhLHRwgUqbal6z4n8QrSzsI-Pg"); // Reemplazar

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
                .option("unsigned_upload_preset", "tu_preset_name")
                .option("transformation", new com.cloudinary.Transformation()
                        .width(400)
                        .height(600)
                        .crop("fill")); // Formato típico de portada de libro
    }
}