package com.tfg.charmreader.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_ApiBackup;
import com.tfg.charmreader.objetosBD.API;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GestionBackup extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ajusta el nombre del layout si es buckup o backup
        setContentView(R.layout.activity_gestion_buckup);

        // BOTÓN EXPORTAR - Ahora llama a la interfaz API
        findViewById(R.id.btnDescargar).setOnClickListener(v -> {
            I_ApiBackup api = API.getInstancia().create(I_ApiBackup.class);
            api.descargarBackup().enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Guardar el archivo en un hilo secundario para no congelar la pantalla
                        new Thread(() -> {
                            boolean resultado = guardarArchivoEnDescargas(response.body());
                            runOnUiThread(() -> {
                                if (resultado) {
                                    Toast.makeText(GestionBackup.this, "Copia guardada en la carpeta Descargas", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(GestionBackup.this, "Error al guardar el archivo en el dispositivo", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }).start();
                    } else {
                        Toast.makeText(GestionBackup.this, "Error del servidor: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(GestionBackup.this, "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // BOTÓN IMPORTAR
        findViewById(R.id.btnSubir).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, 101);
        });
    }

    // Método para escribir los datos descargados en la carpeta pública de Descargas
    private boolean guardarArchivoEnDescargas(ResponseBody body) {
        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path, "backup_biblioteca.csv");

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];
                inputStream = body.byteStream();
                outputStream = new FileOutputStream(file);

                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) break;
                    outputStream.write(fileReader, 0, read);
                }
                outputStream.flush();
                return true;
            } finally {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            subirArchivoAlServidor(data.getData());
        }
    }

    private void subirArchivoAlServidor(Uri uri) {
        try {
            File file = copyUriToFile(uri);
            if (file == null || !file.exists()) {
                Toast.makeText(this, "No se pudo preparar el archivo", Toast.LENGTH_SHORT).show();
                return;
            }

            // Usamos "text/csv" que es el MIME type correcto
            RequestBody requestFile = RequestBody.create(MediaType.parse("text/csv"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            I_ApiBackup api = API.getInstancia().create(I_ApiBackup.class);
            api.importarBackup(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(GestionBackup.this, "¡Base de datos restaurada!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(GestionBackup.this, "Error: El servidor rechazó el archivo", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(GestionBackup.this, "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace(); // <--- MIRA EL LOGCAT SI FALLA
            Toast.makeText(this, "Error al procesar archivo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File copyUriToFile(Uri uri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        if (inputStream == null) return null;

        // Creamos el archivo temporal en la caché interna de la app
        File tempFile = new File(getCacheDir(), "upload_backup.csv");

        // Si ya existe uno de una subida anterior, lo borramos para que no haya conflictos
        if (tempFile.exists()) tempFile.delete();

        FileOutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[1024];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }

        outputStream.flush();
        outputStream.close();
        inputStream.close();

        return tempFile;
    }
}