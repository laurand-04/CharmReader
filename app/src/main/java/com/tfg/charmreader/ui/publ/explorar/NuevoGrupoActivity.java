package com.tfg.charmreader.ui.publ.explorar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import com.tfg.charmreader.databinding.ActivityNuevoGrupoBinding;
import com.tfg.charmreader.viewmodel.publ.explorar.NuevoGrupoViewModel;

public class NuevoGrupoActivity extends AppCompatActivity {

    private ActivityNuevoGrupoBinding binding;
    private NuevoGrupoViewModel viewModel;
    private Uri uriImagenSeleccionada;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    uriImagenSeleccionada = result.getData().getData();
                    binding.ivPreviewGrupo.setImageURI(uriImagenSeleccionada);
                    binding.ivPreviewGrupo.setImageTintList(null);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNuevoGrupoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(NuevoGrupoViewModel.class);

        setupUI();
        setupObservers();
    }

    private void setupUI() {
        // Dropdown Frecuencia
        String[] opciones = {"Semanal", "Quincenal", "Mensual"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, opciones);
        binding.autoCompleteFrecuencia.setAdapter(adapter);
        binding.autoCompleteFrecuencia.setText(opciones[0], false);

        binding.cardSeleccionarImagen.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        binding.btnGuardarGrupo.setOnClickListener(v -> validarYEnviar());

        binding.btnBackNuevoGrupo.setOnClickListener(v -> comprobarYSalir());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { comprobarYSalir(); }
        });
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, loading -> {
            binding.btnGuardarGrupo.setEnabled(!loading);
            binding.btnGuardarGrupo.setText(loading ? "CREANDO..." : "CREAR GRUPO");
            // Nota: Aquí podrías añadir una capa de carga visual (ProgressBar) en el XML
        });

        viewModel.getIsSuccess().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "¡Grupo creado con éxito!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });

        viewModel.getMensaje().observe(this, msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    private void validarYEnviar() {
        String nombre = binding.etNombreGrupo.getText().toString().trim();
        String ubicacion = binding.etUbicacionGrupo.getText().toString().trim();

        if (nombre.isEmpty() || ubicacion.isEmpty()) {
            Toast.makeText(this, "Nombre y ubicación son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        int idU = AuthRepository.getInstance(getApplicationContext()).getIdUsuario();
        viewModel.crearNuevoGrupo(nombre, ubicacion,
                binding.etDescripcionGrupo.getText().toString().trim(),
                binding.autoCompleteFrecuencia.getText().toString(),
                uriImagenSeleccionada, idU);
    }

    private void comprobarYSalir() {
        if (!binding.etNombreGrupo.getText().toString().trim().isEmpty() || uriImagenSeleccionada != null) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("¿Descartar grupo?")
                    .setMessage("Perderás los datos introducidos.")
                    .setNegativeButton("Seguir editando", null)
                    .setPositiveButton("Descartar", (d, w) -> finish())
                    .show();
        } else finish();
    }

}