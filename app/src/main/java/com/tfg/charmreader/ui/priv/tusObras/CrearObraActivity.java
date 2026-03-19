package com.tfg.charmreader.ui.priv.tusObras;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.tfg.charmreader.data.model.Obras;
import com.tfg.charmreader.databinding.ActivityCrearObraBinding;
import com.tfg.charmreader.viewmodel.priv.tusobras.CrearObraViewModel;

public class CrearObraActivity extends AppCompatActivity {

    private ActivityCrearObraBinding binding;
    private CrearObraViewModel viewModel;

    private Uri uriPortadaSeleccionada = null;

    // Variables para controlar si estamos modificando
    private boolean modoModificar = false;
    private Obras obraAModificar = null;

    private final ActivityResultLauncher<String> selectorDeImagen = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uriPortadaSeleccionada = uri;
                    binding.ivPortadaPreview.setImageURI(uri);
                    binding.ivPortadaPreview.setImageTintList(null);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCrearObraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(CrearObraViewModel.class);

        // 1. COMPROBAR SI ESTAMOS EN MODO "MODIFICAR"
        modoModificar = getIntent().getBooleanExtra("MODIFICAR", false);
        if (modoModificar) {
            obraAModificar = (Obras) getIntent().getSerializableExtra("OBRA");
            if (obraAModificar != null) {
                prepararInterfazParaModificar();
            } else {
                modoModificar = false; // Seguridad por si el objeto llega nulo
            }
        }

        setupListeners();
        setupObservers();
    }

    private void prepararInterfazParaModificar() {
        // Cambiamos textos de la cabecera y el botón
        binding.btnGuardarObra.setText("GUARDAR CAMBIOS");
        // Nota: para cambiar el título de la toolbar tendrías que darle un ID a ese TextView en el XML,
        // pero por ahora el botón y rellenar los campos es suficiente.

        // Rellenamos los campos con los datos actuales
        binding.etTituloObra.setText(obraAModificar.getNombre());
        binding.etAutorObra.setText(obraAModificar.getAutor());
        binding.etDescripcionObra.setText(obraAModificar.getSinopsis());
        // (La descripción no se suele guardar en la base de datos de Obras, si la tienes añádela aquí)

        // Cargamos la portada si la tiene
        String urlPortada = obraAModificar.getUrl_imagen();
        if (urlPortada != null && !urlPortada.isEmpty()) {
            binding.ivPortadaPreview.setImageTintList(null);
            Glide.with(this)
                    .load(urlPortada)
                    .centerCrop()
                    .into(binding.ivPortadaPreview);
        }
    }

    private void setupListeners() {
        binding.btnBackCrearObra.setOnClickListener(v -> finish());

        binding.cardPortada.setOnClickListener(v -> {
            selectorDeImagen.launch("image/*");
        });

        binding.btnGuardarObra.setOnClickListener(v -> {
            String titulo = binding.etTituloObra.getText() != null ? binding.etTituloObra.getText().toString().trim() : "";
            String autor = binding.etAutorObra.getText() != null ? binding.etAutorObra.getText().toString().trim() : "";
            String descripcion = binding.etDescripcionObra.getText() != null ? binding.etDescripcionObra.getText().toString().trim() : "";

            if (titulo.isEmpty()) {
                Toast.makeText(this, "El título no puede estar vacío.", Toast.LENGTH_SHORT).show();
                binding.etTituloObra.requestFocus();
                return;
            }

            if (autor.isEmpty()) {
                Toast.makeText(this, "Debes indicar el nombre del autor.", Toast.LENGTH_SHORT).show();
                binding.etAutorObra.requestFocus();
                return;
            }

            // 2. DECIDIR QUÉ FUNCIÓN EJECUTAR
            if (modoModificar && obraAModificar != null) {
                viewModel.modificarObra(obraAModificar, titulo, autor, descripcion, uriPortadaSeleccionada);
            } else {
                viewModel.crearNuevaObra(titulo, autor, descripcion, uriPortadaSeleccionada);
            }
        });
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.layoutLoadingObra.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnGuardarObra.setEnabled(!isLoading);
        });

        viewModel.getMensaje().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getCreacionExitosa().observe(this, exito -> {
            if (exito) {
                String msj = modoModificar ? "¡Obra modificada con éxito!" : "¡Obra creada con éxito!";
                Toast.makeText(this, msj, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}