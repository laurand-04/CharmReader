package com.tfg.charmreader.ui.admin;

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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tfg.charmreader.R;
import com.tfg.charmreader.databinding.ActivityGestionBuckupBinding;
import com.tfg.charmreader.viewmodel.admin.BackupViewModel;

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

public class GestionBackupActivity extends AppCompatActivity {

    private ActivityGestionBuckupBinding binding;
    private BackupViewModel viewModel;
    private static final int PICK_CSV_FILE = 101;
    private final String PALABRA_CONTROL = "RESTAURAR INFORMACION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGestionBuckupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configurarUI();
        viewModel = new ViewModelProvider(this).get(BackupViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, loading -> {
            binding.btnDescargar.setEnabled(!loading);
            binding.btnSubir.setEnabled(!loading);
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.getStatusMessage().observe(this, msg -> Toast.makeText(this, msg, Toast.LENGTH_LONG).show());

        viewModel.getDownloadData().observe(this, body -> {
            new Thread(() -> {
                File file = guardarArchivoEnDescargas(body);
                runOnUiThread(() -> {
                    if (file != null) Toast.makeText(this, "✅ Guardado: " + file.getName(), Toast.LENGTH_LONG).show();
                });
            }).start();
        });
    }

    private void setupListeners() {
        binding.btnBackBackup.setOnClickListener(v -> finish());
        binding.btnDescargar.setOnClickListener(v -> viewModel.exportarDatos());
        binding.btnSubir.setOnClickListener(v -> abrirSelectorArchivo());
    }

    // --- Mantenemos los métodos de ayuda de archivos aquí por usar Context/Intent ---
    private void abrirSelectorArchivo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"text/comma-separated-values", "text/csv", "application/csv"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Selecciona el backup CSV"), PICK_CSV_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CSV_FILE && resultCode == RESULT_OK && data != null) {
            mostrarDialogoSeguridad(data.getData());
        }
    }

    private void mostrarDialogoSeguridad(Uri uri) {
        FrameLayout container = new FrameLayout(this);
        final EditText input = new EditText(this);
        input.setHint("Escribe la frase aquí...");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        input.setGravity(Gravity.CENTER);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 50; params.rightMargin = 50; params.topMargin = 20;
        input.setLayoutParams(params);
        container.addView(input);

        new MaterialAlertDialogBuilder(this)
                .setTitle("⚠️ Confirmación Crítica")
                .setMessage("Esta acción borrará la BD actual. Escribe:\n\n" + PALABRA_CONTROL)
                .setView(container)
                .setPositiveButton("CONFIRMAR", (d, w) -> {
                    if (input.getText().toString().trim().equalsIgnoreCase(PALABRA_CONTROL)) {
                        prepararImportacion(uri);
                    }
                }).show();
    }

    private void prepararImportacion(Uri uri) {
        try {
            File file = copyUriToFile(uri);
            if (file != null) {
                RequestBody requestFile = RequestBody.create(MediaType.parse("text/csv"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
                viewModel.importarDatos(body);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File guardarArchivoEnDescargas(ResponseBody body) {
        try {
            String fecha = new SimpleDateFormat("dd_MM_yyyy_HHmm", Locale.getDefault()).format(new Date());
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path, "CharmReader_Backup_" + fecha + ".csv");

            try (InputStream is = body.byteStream(); OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) os.write(buffer, 0, read);
            }
            return file;
        } catch (Exception e) { return null; }
    }

    private File copyUriToFile(Uri uri) throws Exception {
        InputStream is = getContentResolver().openInputStream(uri);
        File tempFile = new File(getCacheDir(), "upload_temp.csv");
        try (FileOutputStream os = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) os.write(buffer, 0, read);
        }
        return tempFile;
    }

    private void configurarUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }
}