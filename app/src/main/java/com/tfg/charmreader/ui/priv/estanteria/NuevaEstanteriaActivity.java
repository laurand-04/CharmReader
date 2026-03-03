package com.tfg.charmreader.ui.priv.estanteria;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tfg.charmreader.R;
import com.tfg.charmreader.databinding.ActivityNuevaEstanteriaBinding;
import com.tfg.charmreader.viewmodel.priv.estanteria.EstanteriaViewModel;

public class NuevaEstanteriaActivity extends AppCompatActivity {
    private ActivityNuevaEstanteriaBinding binding;
    private EstanteriaViewModel viewModel;
    private String colorSeleccionado = "#F3E5F5";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNuevaEstanteriaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        aplicarEfectoBlur();

        viewModel = new ViewModelProvider(this).get(EstanteriaViewModel.class);
        viewModel.setContext(this);

        setupObservers();
        setupListeners();
        configurarSelectorColores();
    }

    private void setupObservers() {
        viewModel.getIsSuccess().observe(this, success -> {
            if (success) {
                setResult(Activity.RESULT_OK);
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            binding.tilTitulo.setError(msg);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.getIsLoading().observe(this, loading -> {
            binding.layoutLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.layoutContenido.setAlpha(loading ? 0.5f : 1.0f);
            binding.btnGuardar.setEnabled(!loading);
        });
    }

    private void setupListeners() {
        binding.btnBackNuevaEstanteria.setOnClickListener(v -> comprobarYSalir());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() { comprobarYSalir(); }
        });

        binding.btnGuardar.setOnClickListener(v -> {
            String titulo = binding.etTitulo.getText().toString().trim();
            viewModel.crearEstanteria(this, titulo, colorSeleccionado);
        });
    }

    private void configurarSelectorColores() {
        View.OnClickListener colorListener = v -> {
            resetearEscalasColores();
            v.setScaleX(1.3f);
            v.setScaleY(1.3f);

            int id = v.getId();
            if (id == R.id.color1) colorSeleccionado = "#F3E5F5";
            else if (id == R.id.color2) colorSeleccionado = "#E3F2FD";
            else if (id == R.id.color3) colorSeleccionado = "#E8F5E9";
            else if (id == R.id.color4) colorSeleccionado = "#FFF3E0";
            else if (id == R.id.color5) colorSeleccionado = "#FFEBEE";

            binding.ivIconoPreview.getBackground().setColorFilter(
                    android.graphics.Color.parseColor(colorSeleccionado),
                    PorterDuff.Mode.SRC_IN
            );
        };

        binding.color1.setOnClickListener(colorListener);
        binding.color2.setOnClickListener(colorListener);
        binding.color3.setOnClickListener(colorListener);
        binding.color4.setOnClickListener(colorListener);
        binding.color5.setOnClickListener(colorListener);
    }

    private void resetearEscalasColores() {
        binding.color1.setScaleX(1f); binding.color1.setScaleY(1f);
        binding.color2.setScaleX(1f); binding.color2.setScaleY(1f);
        binding.color3.setScaleX(1f); binding.color3.setScaleY(1f);
        binding.color4.setScaleX(1f); binding.color4.setScaleY(1f);
        binding.color5.setScaleX(1f); binding.color5.setScaleY(1f);
    }

    private void comprobarYSalir() {
        if (!binding.etTitulo.getText().toString().trim().isEmpty()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("¿Descartar estantería?")
                    .setMessage("Tienes cambios sin guardar.")
                    .setNegativeButton("Seguir editando", null)
                    .setPositiveButton("Descartar", (d, w) -> finish())
                    .show();
        } else {
            finish();
        }
    }

    private void aplicarEfectoBlur() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            getWindow().getAttributes().setBlurBehindRadius(20);
        }
    }
}