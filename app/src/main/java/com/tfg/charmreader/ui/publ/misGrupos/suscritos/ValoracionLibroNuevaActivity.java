package com.tfg.charmreader.ui.publ.misGrupos.suscritos;

import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tfg.charmreader.databinding.ActivityValoracionLibroNuevaBinding;
import com.tfg.charmreader.viewmodel.publ.misGrupos.suscritos.ValoracionLibroViewModel;

public class ValoracionLibroNuevaActivity extends AppCompatActivity {

    private ActivityValoracionLibroNuevaBinding binding;
    private ValoracionLibroViewModel viewModel;
    private int idLibro, idGrupo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityValoracionLibroNuevaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        aplicarEfectoBlur();

        idLibro = getIntent().getIntExtra("idLibro", -1);
        idGrupo = getIntent().getIntExtra("idGrupo", -1);

        viewModel = new ViewModelProvider(this).get(ValoracionLibroViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, loading -> {
            binding.btnEnviarValoracion.setEnabled(!loading);
            binding.btnEnviarValoracion.setText(loading ? "PUBLICANDO..." : "PUBLICAR VALORACIÓN");
        });

        viewModel.getIsSuccess().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "¡Reseña publicada!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });

        viewModel.getMensaje().observe(this, msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    private void setupListeners() {
        binding.btnBackValoracionLibro.setOnClickListener(v -> comprobarYSalir());

        binding.btnEnviarValoracion.setOnClickListener(v ->
                viewModel.publicarResena(idLibro, idGrupo,
                        binding.ratingValoracion.getRating(),
                        binding.etReseA.getText().toString())
        );

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() { comprobarYSalir(); }
        });
    }

    private void comprobarYSalir() {
        if (binding.ratingValoracion.getRating() > 0 || !binding.etReseA.getText().toString().trim().isEmpty()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("¿Descartar valoración?")
                    .setMessage("Si sales ahora perderás la reseña que has escrito.")
                    .setNegativeButton("Seguir escribiendo", null)
                    .setPositiveButton("Descartar", (dialog, which) -> finish())
                    .show();
        } else finish();
    }

    private void aplicarEfectoBlur() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            getWindow().getAttributes().setBlurBehindRadius(20);
        }
    }
}