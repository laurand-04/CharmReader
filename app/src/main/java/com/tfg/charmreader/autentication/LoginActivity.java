package com.tfg.charmreader.autentication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.tfg.charmreader.admin.AdminMainActivity;
import com.tfg.charmreader.menu.MainActivity;
import com.tfg.charmreader.databinding.ActivityLoginBinding;
import androidx.appcompat.app.AlertDialog;
import androidx.core.splashscreen.SplashScreen;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. La Splash Screen siempre es lo primero
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        // 2. Comprobamos la sesión inmediatamente después del super
        SharedPreferences preferences = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        boolean estaLogeado = preferences.getBoolean("logeado", false);
        boolean esAdmin = preferences.getBoolean("esAdmin", false);

        if (estaLogeado) {
            Intent intent;
            if (esAdmin) {
                intent = new Intent(this, AdminMainActivity.class);
            } else {
                intent = new Intent(this, MainActivity.class);
            }
            startActivity(intent);
            finish();
            return; // Nos vamos, pero habiendo cumplido con el super.onCreate
        }

        // 3. Si no está logeado, cargamos la interfaz normal
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.loginButton.setOnClickListener(v -> login());
        binding.registerTextView.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
        binding.recoverTextView.setOnClickListener(v ->
                startActivity(new Intent(this, RecuperacionActivity.class)));
    }

    private void login() {
        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            mostrarAlerta("Alerta", "Rellena todos los campos para poder iniciar sesión");
            return;
        }

        if (email.equals("admin") && password.equals("admin")) {
            guardarSesion(true, email);
            startActivity(new Intent(this, AdminMainActivity.class));
            finish();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        guardarSesion(false, email);
                        irAMainActivity();
                    } else {
                        mostrarError(task.getException());
                    }
                });
    }

    private void guardarSesion(boolean isAdmin, String email) {
        SharedPreferences preferences = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("logeado", true);
        editor.putBoolean("esAdmin", isAdmin);
        editor.putString("correoUsuario", email);
        editor.apply();
    }

    private void irAMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void mostrarAlerta(String titulo, String contenido) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titulo);
        builder.setMessage(contenido);
        builder.setPositiveButton("Entendido", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    public void mostrarError(Exception e){
        String mensajeError = "Error desconocido";
        if (e instanceof FirebaseAuthInvalidUserException) {
            mensajeError = "Usuario no registrado o deshabilitado.";
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            mensajeError = "Credenciales inválidas, revisa el email o la contraseña.";
        } else if (e != null) {
            mensajeError = e.getMessage();
        }
        mostrarAlerta("Error", mensajeError);
    }
}