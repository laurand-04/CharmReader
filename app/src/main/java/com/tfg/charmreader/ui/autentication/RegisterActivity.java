package com.tfg.charmreader.ui.autentication;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.tfg.charmreader.databinding.ActivityRegisterBinding;
import com.tfg.charmreader.viewmodel.autentication.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inflar ViewBinding
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
        viewModel.setContext(this);

        // 3. Configurar UI y Observadores
        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        // Botón volver
        binding.btnVolver.setOnClickListener(v -> finish());

        // Botón de registro con validaciones previas
        binding.registerButton.setOnClickListener(v -> {
            String email = binding.emailEditText.getText().toString().trim();
            String password = binding.passwordEditText.getText().toString().trim();
            String confirm = binding.confirmPasswordEditText.getText().toString().trim();

            if (validarCampos(email, password, confirm)) {
                viewModel.register(email, password);
            }
        });

    }

    private boolean validarCampos(String email, String password, String confirm) {
        if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            mostrarAlerta("Alerta", "Rellena todos los campos para poder registrarte");
            return false;
        }
        if (password.length() < 6) {
            mostrarAlerta("Error", "La contraseña debe tener al menos 6 caracteres");
            return false;
        }
        if (!password.equals(confirm)) {
            mostrarAlerta("Error", "Las contraseñas no coinciden");
            return false;
        }
        return true;
    }

    private void observeViewModel() {
        viewModel.getState().observe(this, state -> {
            // Control de UI según el estado de carga
            binding.registerButton.setEnabled(!state.loading);
            // Si añadiste el ProgressBar al layout como sugerí:
            if (binding.progressBar != null) {
                binding.progressBar.setVisibility(state.loading ? View.VISIBLE : View.GONE);
            }

            if (state.success) {
                mostrarAlertaExito();
            } else if (state.error != null) {
                mostrarAlerta("Error de Registro", state.error);
            }
        });
    }

    private void mostrarAlertaExito() {
        new AlertDialog.Builder(this)
                .setTitle("¡Registro casi completado!")
                .setMessage("Hemos enviado un enlace de verificación a tu correo. Por favor, confírmalo antes de iniciar sesión.")
                .setCancelable(false)
                .setPositiveButton("Entendido", (dialog, which) -> comprobar()) // Cerramos y volvemos al Login
                .show();
    }

    private void comprobar(){
        if(!viewModel.isVerified()){
            mostrarAlertaExito();
        }else{
            viewModel.enviarCorreoBienvenida();
            finish();
        }
    }

    private void mostrarAlerta(String titulo, String contenido) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(contenido)
                .setPositiveButton("Aceptar", null)
                .show();
    }
}