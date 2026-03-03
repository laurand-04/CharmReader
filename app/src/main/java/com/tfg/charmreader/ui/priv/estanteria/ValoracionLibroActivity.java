package com.tfg.charmreader.ui.priv.estanteria;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tfg.charmreader.databinding.ActivityValoracionLibroBinding;
import com.tfg.charmreader.data.model.LibrosDeUsuario;
import com.tfg.charmreader.viewmodel.priv.estanteria.ValoracionViewModel;

public class ValoracionLibroActivity extends AppCompatActivity {

    private ActivityValoracionLibroBinding binding;
    private ValoracionViewModel viewModel;
    private LibrosDeUsuario libroActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityValoracionLibroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configurarVentana();

        libroActual = (LibrosDeUsuario) getIntent().getSerializableExtra("OBJETO_LIBRO_USUARIO");
        if (libroActual == null) {
            Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(ValoracionViewModel.class);

        popularDatos();
        setupObservers();
        setupListeners();
    }

    private void popularDatos() {
        binding.ratingBar.setRating((float) libroActual.getValoracion());
        binding.etDescription.setText(libroActual.getDescripcion());
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, loading -> {
            binding.layoutLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.layoutContenido.setAlpha(loading ? 0.3f : 1.0f);
        });

        viewModel.getIsSuccess().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "¡Valoración guardada!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });

        viewModel.getError().observe(this, msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    private void setupListeners() {
        binding.btnBackValoracion.setOnClickListener(v -> finish());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() { comprobarYSalir(); }
        });

        binding.btnSubmit.setOnClickListener(v ->
                viewModel.guardarValoracion(libroActual, binding.ratingBar.getRating(), binding.etDescription.getText().toString().trim())
        );
    }

    private void comprobarYSalir() {
        if (viewModel.hayCambios(libroActual, binding.ratingBar.getRating(), binding.etDescription.getText().toString().trim())) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("¿Descartar cambios?")
                    .setMessage("Si sales ahora perderás la valoración introducida.")
                    .setNegativeButton("Seguir editando", null)
                    .setPositiveButton("Descartar", (dialog, which) -> finish())
                    .show();
        } else {
            finish();
        }
    }

    private void configurarVentana() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            getWindow().getAttributes().setBlurBehindRadius(20);
        }
    }
}