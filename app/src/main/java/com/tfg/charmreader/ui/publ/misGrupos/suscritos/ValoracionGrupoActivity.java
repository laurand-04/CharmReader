package com.tfg.charmreader.ui.publ.misGrupos.suscritos;

import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tfg.charmreader.databinding.ActivityValoracionGrupoBinding;
import com.tfg.charmreader.viewmodel.publ.misGrupos.suscritos.ValoracionGrupoViewModel;

public class ValoracionGrupoActivity extends AppCompatActivity {

    private ActivityValoracionGrupoBinding binding;
    private ValoracionGrupoViewModel viewModel;
    private int idLibro, idGrupo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityValoracionGrupoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        aplicarEfectoBlur();

        idLibro = getIntent().getIntExtra("idLibro", -1);
        idGrupo = getIntent().getIntExtra("idGrupo", -1);

        viewModel = new ViewModelProvider(this).get(ValoracionGrupoViewModel.class);

        setupUI();
        setupObservers();
    }

    private void setupUI() {
        // Personalizar título si es reseña de libro o grupo
        if (idLibro != -1) {
            binding.tvTituloVentana.setText("Reseña del Libro");
            binding.tvPreguntaValoracion.setText("¿Qué te ha parecido esta lectura?");
        }

        binding.btnEnviarValoracion.setOnClickListener(v ->
                viewModel.publicarValoracion(idGrupo, idLibro,
                        binding.ratingValoracion.getRating(),
                        binding.etReseA.getText().toString())
        );

        binding.btnBackValoracion.setOnClickListener(v -> comprobarYSalir());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() { comprobarYSalir(); }
        });
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, loading -> {
            binding.btnEnviarValoracion.setEnabled(!loading);
            binding.btnEnviarValoracion.setText(loading ? "PUBLICANDO..." : "PUBLICAR RESEÑA");
        });

        viewModel.getIsSuccess().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "¡Reseña publicada!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });

        viewModel.getMensaje().observe(this, msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    private void comprobarYSalir() {
        if (binding.ratingValoracion.getRating() > 0 || !binding.etReseA.getText().toString().trim().isEmpty()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("¿Descartar reseña?")
                    .setMessage("Si sales ahora perderás lo que has escrito.")
                    .setNegativeButton("Seguir escribiendo", null)
                    .setPositiveButton("Descartar", (dialog, which) -> finish())
                    .show();
        } else finish();
    }

    private void aplicarEfectoBlur() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            getWindow().getAttributes().setBlurBehindRadius(25);
        }
    }
}