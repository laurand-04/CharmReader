package com.tfg.charmreader.admin;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_ApiBackup;
import com.tfg.charmreader.objetosBD.API;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GestionBackup extends AppCompatActivity {

    private ImageView btnBack;
    private static final int PICK_CSV_FILE = 101;
    private final String PALABRA_CONTROL = "RESTAURAR INFORMACION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        setContentView(R.layout.activity_gestion_buckup);

        btnBack = findViewById(R.id.btnBackBackup);
        btnBack.setOnClickListener(v -> finish());

        findViewById(R.id.btnDescargar).setOnClickListener(v -> ejecutarExportacion());
        findViewById(R.id.btnSubir).setOnClickListener(v -> abrirSelectorArchivo());
    }

    private void ejecutarExportacion() {
        Toast.makeText(this, "Generando copia en el servidor...", Toast.LENGTH_SHORT).show();
        I_ApiBackup api = API.getInstancia().create(I_ApiBackup.class);
        api.descargarBackup().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new Thread(() -> {
                        File resultado = guardarArchivoEnDescargas(response.body());
                        runOnUiThread(() -> {
                            if (resultado != null) {
                                Toast.makeText(GestionBackup.this, "✅ Copia guardada: " + resultado.getName(), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(GestionBackup.this, "❌ Error al guardar el archivo", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).start();
                }
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(GestionBackup.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void abrirSelectorArchivo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"text/comma-separated-values", "text/csv", "application/csv"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Selecciona el backup CSV"), PICK_CSV_FILE);
    }

    private File guardarArchivoEnDescargas(ResponseBody body) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy", Locale.getDefault());
            String fechaActual = sdf.format(new Date());
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String nombreArchivo = "CharmReader_" + fechaActual + ".csv";
            File file = new File(path, nombreArchivo);

            if (file.exists()) {
                String horaActual = new SimpleDateFormat("HHmm", Locale.getDefault()).format(new Date());
                file = new File(path, "CharmReader_" + fechaActual + "_" + horaActual + ".csv");
            }

            try (InputStream inputStream = body.byteStream();
                 OutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
            }
            return file;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CSV_FILE && resultCode == RESULT_OK && data != null) {
            Uri selectedUri = data.getData();
            mostrarDialogoSeguridad(selectedUri);
        }
    }

    private void mostrarDialogoSeguridad(Uri uri) {
        // Contenedor elegante para el EditText
        FrameLayout container = new FrameLayout(this);
        final EditText input = new EditText(this);

        // Estilo del EditText
        input.setHint("Escribe la frase aquí...");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        input.setGravity(Gravity.CENTER);
        input.setBackgroundResource(android.R.drawable.editbox_background_normal);

        // Márgenes del contenedor
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(com.google.android.material.R.dimen.abc_dialog_padding_material);
        params.rightMargin = params.leftMargin;
        params.topMargin = 20;
        input.setLayoutParams(params);
        container.addView(input);

        // Construcción del Diálogo Material
        new MaterialAlertDialogBuilder(this)
                .setTitle("⚠️ Confirmación Crítica")
                .setMessage("Esta acción borrará la base de datos actual. Para continuar, escribe exactamente:\n\n" + PALABRA_CONTROL)
                .setView(container)
                .setCancelable(false)
                .setNegativeButton("CANCELAR", null)
                .setPositiveButton("CONFIRMAR RESTAURACIÓN", (dialog, which) -> {
                    String texto = input.getText().toString().trim();
                    if (texto.equalsIgnoreCase(PALABRA_CONTROL)) {
                        subirArchivoAlServidor(uri);
                    } else {
                        Toast.makeText(this, "Palabra incorrecta. Operación abortada.", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void subirArchivoAlServidor(Uri uri) {
        Toast.makeText(this, "Subiendo y restaurando...", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(GestionBackup.this, "🚀 Restauración completada con éxito", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(GestionBackup.this, "❌ Error en el servidor", Toast.LENGTH_SHORT).show();
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
        if (inputStream == null) return null;
        File tempFile = new File(getCacheDir(), "upload_temp.csv");
        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
        }
        inputStream.close();
        return tempFile;
    }
}