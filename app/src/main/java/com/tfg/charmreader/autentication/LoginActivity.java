package com.tfg.charmreader.autentication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tfg.charmreader.Utilidades;
import com.tfg.charmreader.admin.AdminMainActivity;
import com.tfg.charmreader.menu.MainActivity;
import com.tfg.charmreader.databinding.ActivityLoginBinding;
import com.tfg.charmreader.objetosBD.Usuario;

import androidx.appcompat.app.AlertDialog;
import androidx.core.splashscreen.SplashScreen;

import java.io.IOException;

import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        boolean estaLogeado = preferences.getBoolean("logeado", false);
        boolean esAdmin = preferences.getBoolean("esAdmin", false);

        if (estaLogeado) {
            abrirPantallaCorrespondiente(esAdmin);
            return;
        }

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
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            guardarSesion(false, email);
                        } else {
                            mostrarAlertaBloqueanteVerificacion(user, email);
                        }
                    } else {
                        mostrarError(task.getException());
                    }
                });
    }

    private void mostrarAlertaBloqueanteVerificacion(FirebaseUser user, String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cuenta no verificada");
        builder.setMessage("Tu correo aún no ha sido confirmado. Por favor, pulsa el enlace que enviamos a: " + email);
        builder.setCancelable(false);
        builder.setPositiveButton("YA HE VERIFICADO", null);
        builder.setNeutralButton("REENVIAR CORREO", (dialog, which) -> {
            if (user != null) user.sendEmailVerification();
        });
        builder.setNegativeButton("CANCELAR", (dialog, which) -> {
            mAuth.signOut();
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            user.reload().addOnCompleteListener(task -> {
                if (user.isEmailVerified()) {
                    dialog.dismiss();
                    guardarSesion(false, email);
                } else {
                    mostrarAlerta("Error", "Sigues sin verificar el correo.");
                }
            });
        });
    }

    private void guardarSesion(boolean isAdmin, String email) {
        binding.loginButton.setEnabled(false);

        new Thread(() -> {
            SharedPreferences preferences = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            editor.putBoolean("logeado", true);
            editor.putBoolean("esAdmin", isAdmin);
            editor.putString("correoUsuario", email);

            if (!isAdmin) {
                try {
                    Response<Usuario> response = Utilidades.apiUsuario.getIdUsuarioPorCorreo(email).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        int idUsuarioSQL = response.body().getId();
                        editor.putInt("idUsuario", idUsuarioSQL);
                        Log.d("DEBUG_LOGIN", "ID SQL guardado: " + idUsuarioSQL);
                    }
                } catch (IOException e) {
                    Log.e("API_ERROR", "Error de red al obtener ID", e);
                }
            }

            editor.apply();

            // 🔥 IMPORTANTE: Navegar después de guardar
            runOnUiThread(() -> abrirPantallaCorrespondiente(isAdmin));
        }).start();
    }

    private void abrirPantallaCorrespondiente(boolean esAdmin) {
        Intent intent;
        if (esAdmin) {
            intent = new Intent(this, AdminMainActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
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
        String mensajeError = e != null ? e.getMessage() : "Error desconocido";
        mostrarAlerta("Error", mensajeError);
    }
}