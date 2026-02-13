package com.tfg.charmreader.autentication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tfg.charmreader.R;
import com.tfg.charmreader.Utilidades;

public class Perfil extends AppCompatActivity {

    private ImageView btnVolver;
    private TextView tvNombreUsuario, tvEmailUsuario;
    private MaterialButton btnCerrarSesion, btnCambiarPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Estética CharmReader: Barra de estado blanca
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        setContentView(R.layout.activity_perfil);

        vincularVistas();
        cargarDatosUsuario();
        configurarListeners();

        // Control del botón atrás físico
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
    }

    private void vincularVistas() {
        btnVolver = findViewById(R.id.btnVolverPerfil);
        tvNombreUsuario = findViewById(R.id.tvNombreUsuario);
        tvEmailUsuario = findViewById(R.id.tvEmailUsuario);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnCambiarPassword = findViewById(R.id.btnCambiarPassword);
    }

    private void cargarDatosUsuario() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            tvEmailUsuario.setText(user.getEmail());

            Utilidades.obtenerIdUsuarioDesdeAPI(new Utilidades.IdUsuarioCallback() {
                @Override
                public void onIdCargado(int idUsuario) {
                    runOnUiThread(() -> {
                        // Priorizar DisplayName si existe, sino usar ID
                        if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                            tvNombreUsuario.setText(user.getDisplayName());
                        } else {
                            tvNombreUsuario.setText("Usuario #" + idUsuario);
                        }
                    });
                }
            });
        }
    }

    private void configurarListeners() {
        // Al pulsar atrás en la cabecera, lanzamos el dispatcher de atrás para la animación
        btnVolver.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        btnCambiarPassword.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.getEmail() != null) {
                enviarCorreoRecuperacion(user.getEmail());
            }
        });

        btnCerrarSesion.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Cerrar sesión")
                    .setMessage("¿Estás seguro de que deseas salir de tu cuenta?")
                    .setNegativeButton("CANCELAR", (dialog, which) -> dialog.dismiss())
                    .setPositiveButton("CERRAR SESIÓN", (dialog, which) -> realizarCerrarSesion())
                    .show();
        });
    }

    private void enviarCorreoRecuperacion(String email) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Correo enviado a " + email, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void realizarCerrarSesion() {
        FirebaseAuth.getInstance().signOut();
        // Limpiar preferencias de sesión
        SharedPreferences preferences = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        preferences.edit().clear().apply();

        Intent intent = new Intent(Perfil.this, com.tfg.charmreader.autentication.LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}