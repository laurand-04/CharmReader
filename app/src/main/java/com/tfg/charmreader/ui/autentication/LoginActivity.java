package com.tfg.charmreader.ui.autentication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tfg.charmreader.databinding.ActivityLoginBinding;
import com.tfg.charmreader.ui.MainActivity;
import com.tfg.charmreader.ui.admin.AdminMainActivity;
import com.tfg.charmreader.viewmodel.autentication.LoginViewModel;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Splash Screen y comprobación de sesión rápida
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser()!=null){
            abrirPantallaCorrespondiente(AuthRepository.getInstance(this).isAdmin());
        }

        // 2. Inflar vista y ViewModel
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nota: Si usas una Factory para el ViewModel pásala aquí.
        // Si no, asegúrate de que LoginViewModel tenga acceso al contexto.
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        viewModel.setContext(this);

        setUpListeners();
        observeViewModel();
    }

    private void setUpListeners() {
        binding.loginButton.setOnClickListener(v -> {
            String email = binding.emailEditText.getText().toString().trim();
            String password = binding.passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                mostrarAlerta("Alerta", "Rellena todos los campos para poder iniciar sesión");
            } else {
                viewModel.login(email, password);
            }
        });

        binding.registerTextView.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        binding.recoverTextView.setOnClickListener(v ->
                startActivity(new Intent(this, RecuperacionActivity.class)));
    }

    private void observeViewModel() {
        viewModel.getLoginState().observe(this, state -> {
            // Controlar el botón de login según si está cargando
            binding.loginButton.setEnabled(!state.loading);
            binding.progressBar.setVisibility(state.loading ? View.VISIBLE : View.GONE);

            if (state.success) {
                abrirPantallaCorrespondiente(state.isAdmin);
            } else if (state.needsVerification) {
                mostrarAlertaBloqueanteVerificacion();
            } else if (state.errorMessage != null) {
                mostrarAlerta("Error", state.errorMessage);
            }
        });
    }

    private void mostrarAlertaBloqueanteVerificacion() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = user != null ? user.getEmail() : "";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cuenta no verificada");
        builder.setMessage("Tu correo aún no ha sido confirmado. Pulsa el enlace enviado a: " + email);
        builder.setCancelable(false);

        builder.setPositiveButton("YA HE VERIFICADO", null);
        builder.setNeutralButton("REENVIAR CORREO", (dialog, which) -> {
            if (user != null) user.sendEmailVerification();
        });
        builder.setNegativeButton("CANCELAR", (dialog, which) -> {
            FirebaseAuth.getInstance().signOut();
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            user.reload().addOnCompleteListener(task -> {
                if (user.isEmailVerified()) {
                    dialog.dismiss();
                    viewModel.fetchIdAndSave(email); // Llamada al VM para finalizar
                } else {
                    mostrarAlerta("Error", "Sigues sin verificar el correo.");
                }
            });
        });
    }

    private void abrirPantallaCorrespondiente(boolean esAdmin) {
        Intent intent = new Intent(this, esAdmin ? AdminMainActivity.class : MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void mostrarAlerta(String titulo, String contenido) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(contenido)
                .setPositiveButton("Entendido", null)
                .show();
    }
}