package com.tfg.charmreader.ui.autentication;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.tfg.charmreader.databinding.ActivityRecuperacionBinding;
import com.tfg.charmreader.viewmodel.autentication.RecuperacionViewModel;

public class RecuperacionActivity extends AppCompatActivity {
    private ActivityRecuperacionBinding binding;
    private RecuperacionViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecuperacionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RecuperacionViewModel.class);
        viewModel.setContext(this);

        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        binding.btnVolver.setOnClickListener(v -> finish());

        binding.recuperacionButton.setOnClickListener(v -> {
            String email = binding.emailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                mostrarAlerta("Campo vacío", "Introduce tu email para enviarte las instrucciones.");
            } else {
                viewModel.enviarCorreoRecuperacion(email);
            }
        });
    }

    private void observeViewModel() {
        viewModel.getState().observe(this, state -> {
            binding.recuperacionButton.setEnabled(!state.loading);
            if (binding.progressBar != null) {
                binding.progressBar.setVisibility(state.loading ? View.VISIBLE : View.GONE);
            }

            if (state.success) {
                String mensajeExito = "Si existe una cuenta asociada, recibirás un correo en breve.\n\n" +
                        "⚠️ Revisa tu carpeta de SPAM si no lo encuentras.";
                mostrarAlerta("Correo enviado", mensajeExito, () -> finish());
            } else if (state.error != null) {
                mostrarAlerta("Error", state.error);
            }
        });
    }

    public void mostrarAlerta(String titulo, String contenido, Runnable accionAlCerrar) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(contenido)
                .setCancelable(false)
                .setPositiveButton("Entendido", (dialog, which) -> {
                    if (accionAlCerrar != null) accionAlCerrar.run();
                })
                .show();
    }

    public void mostrarAlerta(String titulo, String contenido) {
        mostrarAlerta(titulo, contenido, null);
    }
}