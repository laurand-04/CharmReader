package com.tfg.charmreader.ui.priv.tusObras;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback; // Importante para el botón físico
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tfg.charmreader.data.model.Obras;
import com.tfg.charmreader.databinding.ActivityEditorObraBinding;
import com.tfg.charmreader.viewmodel.priv.tusobras.EditorObraViewModel;

public class EditorObraActivity extends AppCompatActivity {

    private ActivityEditorObraBinding binding;
    private EditorObraViewModel viewModel;

    private boolean isFirstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditorObraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(EditorObraViewModel.class);

        setupListeners();
        setupObservers();

        // --- MANEJO DEL BOTÓN ATRÁS DEL SISTEMA ---
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mostrarDialogoDeSalida();
            }
        });

        String rutaObra = getIntent().getStringExtra("RUTA_OBRA");
        Obras obra = (Obras) getIntent().getSerializableExtra("OBRA");

        if (rutaObra != null && !rutaObra.isEmpty() && isFirstLoad) {
            viewModel.cargarObra(rutaObra, obra);
            isFirstLoad = false;
        } else {
            Toast.makeText(this, "Error: No se encontró la ruta del archivo", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupListeners() {
        // Al dar a la 'X' de la interfaz, mostramos el diálogo
        binding.btnCerrarEditor.setOnClickListener(v -> mostrarDialogoDeSalida());

        binding.btnCapituloAnterior.setOnClickListener(v -> {
            String textoActual = binding.etContenidoCapitulo.getText() != null ? binding.etContenidoCapitulo.getText().toString() : "";
            viewModel.cambiarCapitulo(-1, textoActual);
        });

        binding.btnCapituloSiguiente.setOnClickListener(v -> {
            String textoActual = binding.etContenidoCapitulo.getText() != null ? binding.etContenidoCapitulo.getText().toString() : "";
            viewModel.cambiarCapitulo(1, textoActual);
        });

        binding.btnNuevoCapitulo.setOnClickListener(v -> {
            String textoActual = binding.etContenidoCapitulo.getText() != null ? binding.etContenidoCapitulo.getText().toString() : "";
            viewModel.anadirNuevoCapitulo(textoActual);
        });

        binding.btnGuardarObra.setOnClickListener(v -> {
            String textoActual = binding.etContenidoCapitulo.getText() != null ? binding.etContenidoCapitulo.getText().toString() : "";
            viewModel.guardarObraFisicamente(textoActual);
        });

        binding.btnEliminarCapitulo.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Eliminar último capítulo")
                    .setMessage("¿Estás seguro de que deseas eliminar este capítulo? Se perderá todo su contenido.")
                    .setNegativeButton("CANCELAR", null)
                    .setPositiveButton("ELIMINAR", (dialog, which) -> {
                        viewModel.eliminarUltimoCapitulo();
                    })
                    .show();
        });
    }

    // --- DIÁLOGO DE CONFIRMACIÓN AL SALIR ---
    private void mostrarDialogoDeSalida() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("¿Salir del editor?")
                .setMessage("Asegúrate de haber guardado tus cambios. Los datos no guardados se perderán.")
                .setNegativeButton("CONTINUAR ESCRIBIENDO", null)
                .setPositiveButton("SALIR", (dialog, which) -> {
                    finish();
                })
                .show();
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.layoutLoadingEditor.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getMensaje().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getTituloCapitulo().observe(this, titulo -> {
            binding.tvTituloCapitulo.setText(titulo);
        });

        viewModel.getContenidoCapitulo().observe(this, contenido -> {
            binding.etContenidoCapitulo.setText(contenido);
            if (binding.etContenidoCapitulo.getText() != null) {
                binding.etContenidoCapitulo.setSelection(binding.etContenidoCapitulo.getText().length());
            }
        });

        viewModel.getEsPrimerCapitulo().observe(this, esPrimero -> {
            binding.btnCapituloAnterior.setVisibility(esPrimero ? View.INVISIBLE : View.VISIBLE);
        });

        viewModel.getEsUltimoCapitulo().observe(this, esUltimo -> {
            binding.btnCapituloSiguiente.setVisibility(esUltimo ? View.INVISIBLE : View.VISIBLE);
        });

        viewModel.getPuedeEliminar().observe(this, puede -> {
            binding.btnEliminarCapitulo.setVisibility(puede ? View.VISIBLE : View.GONE);
        });
    }
}