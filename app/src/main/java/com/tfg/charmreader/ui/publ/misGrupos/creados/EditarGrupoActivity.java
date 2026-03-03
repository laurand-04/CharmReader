package com.tfg.charmreader.ui.publ.misGrupos.creados;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tfg.charmreader.data.model.GrupoLectura;
import com.tfg.charmreader.databinding.ActivityEditarGrupoBinding;
import com.tfg.charmreader.viewmodel.publ.misGrupos.creados.EditarGrupoViewModel;

public class EditarGrupoActivity extends AppCompatActivity {

    private ActivityEditarGrupoBinding binding;
    private EditarGrupoViewModel viewModel;
    private GrupoLectura grupoOriginal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditarGrupoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        grupoOriginal = (GrupoLectura) getIntent().getSerializableExtra("objetoGrupo");
        if (grupoOriginal == null) { finish(); return; }

        viewModel = new ViewModelProvider(this).get(EditarGrupoViewModel.class);

        setupUI();
        setupObservers();
    }

    private void setupUI() {
        // Rellenar campos
        binding.etNombreEdicion.setText(grupoOriginal.getNombre());
        binding.etUbicacionEdicion.setText(grupoOriginal.getUbicacion());
        binding.etDescEdicion.setText(grupoOriginal.getDescripcion());

        // Configurar selector frecuencia
        String[] opciones = {"SEMANAL", "QUINCENAL", "MENSUAL"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, opciones);
        binding.spinnerFrecuencia.setAdapter(adapter);
        binding.spinnerFrecuencia.setText(grupoOriginal.getFrecuenciaReunion().name(), false);

        binding.btnBackEditar.setOnClickListener(v -> comprobarYSalir());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { comprobarYSalir(); }
        });

        binding.btnGuardarEdicion.setOnClickListener(v -> {
            viewModel.actualizarGrupo(grupoOriginal,
                    binding.etNombreEdicion.getText().toString().trim(),
                    binding.etUbicacionEdicion.getText().toString().trim(),
                    binding.etDescEdicion.getText().toString().trim(),
                    binding.spinnerFrecuencia.getText().toString());
        });
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, loading -> {
            binding.layoutLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.layoutContenido.setAlpha(loading ? 0.4f : 1.0f);
        });

        viewModel.getGrupoActualizado().observe(this, grupo -> {
            Toast.makeText(this, "¡Grupo actualizado!", Toast.LENGTH_SHORT).show();
            Intent data = new Intent();
            data.putExtra("grupoActualizado", grupo);
            setResult(RESULT_OK, data);
            finish();
        });

        viewModel.getMensaje().observe(this, msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    private void comprobarYSalir() {
        if (viewModel.detectarCambios(grupoOriginal,
                binding.etNombreEdicion.getText().toString().trim(),
                binding.etUbicacionEdicion.getText().toString().trim(),
                binding.etDescEdicion.getText().toString().trim(),
                binding.spinnerFrecuencia.getText().toString())) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("¿Descartar cambios?")
                    .setMessage("Tienes modificaciones sin guardar.")
                    .setNegativeButton("Seguir editando", null)
                    .setPositiveButton("Descartar", (d, w) -> finish())
                    .show();
        } else finish();
    }
}