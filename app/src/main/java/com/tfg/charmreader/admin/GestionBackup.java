package com.tfg.charmreader.admin;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
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

    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Estética Admin
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        setContentView(R.layout.activity_gestion_buckup);

        btnBack = findViewById(R.id.btnBackBackup);
        btnBack.setOnClickListener(v -> finish());

        // CONFIGURACIÓN DE LOS BOTONES (Usando IDs del XML rediseñado)
        findViewById(R.id.btnDescargar).setOnClickListener(v -> ejecutarExportacion());
        findViewById(R.id.btnSubir).setOnClickListener(v -> abrirSelectorArchivo());
    }

    private void ejecutarExportacion() {
        Toast.makeText(this, "Iniciando descarga de copia...", Toast.LENGTH_SHORT).show();
        I_ApiBackup api = API.getInstancia().create(I_ApiBackup.class);
        api.descargarBackup().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new Thread(() -> {
                        boolean resultado = guardarArchivoEnDescargas(response.body());
                        runOnUiThread(() -> {
                            if (resultado) {
                                Toast.makeText(GestionBackup.this, "Copia guardada en la carpeta Descargas", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(GestionBackup.this, "Error al escribir el archivo", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).start();
                }
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(GestionBackup.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void abrirSelectorArchivo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/comma-separated-values|text/csv|application/csv");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Selecciona el backup CSV"), 101);
    }

    private boolean guardarArchivoEnDescargas(ResponseBody body) {
        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path, "charmreader_backup_" + System.currentTimeMillis() + ".csv");

            InputStream inputStream = body.byteStream();
            OutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            return true;
        } catch (Exception e) {
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
            if (file == null) return;

            RequestBody requestFile = RequestBody.create(MediaType.parse("text/csv"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            I_ApiBackup api = API.getInstancia().create(I_ApiBackup.class);
            api.importarBackup(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(GestionBackup.this, "¡Sistema restaurado correctamente!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(GestionBackup.this, "Error: El servidor no pudo procesar el CSV", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(GestionBackup.this, "Error de red", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File copyUriToFile(Uri uri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File tempFile = new File(getCacheDir(), "temp_backup.csv");
        FileOutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[1024];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }
        outputStream.close();
        inputStream.close();
        return tempFile;
    }
}