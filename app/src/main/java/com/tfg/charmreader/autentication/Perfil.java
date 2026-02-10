package com.tfg.charmreader.autentication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tfg.charmreader.R;
import com.tfg.charmreader.Utilidades;

public class Perfil extends AppCompatActivity {

    private ImageButton btnVolver;
    private TextView tvNombreUsuario, tvEmailUsuario;
    private Button btnCerrarSesion, btnCambiarPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        vincularVistas();
        cargarDatosUsuario();
        configurarListeners();

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
        btnVolver.setOnClickListener(v -> finish()); // Lanza el dispatcher de atrás

        // Lógica para cambiar contraseña enviando correo
        btnCambiarPassword.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.getEmail() != null) {
                enviarCorreoRecuperacion(user.getEmail());
            }
        });

        btnCerrarSesion.setOnClickListener(v -> {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Cerrar sesión")
                    .setMessage("¿Estás seguro de que deseas salir de tu cuenta?")
                    .setCancelable(true)
                    .setNegativeButton("CANCELAR", (dialog, which) -> dialog.dismiss())
                    .setPositiveButton("CERRAR SESIÓN", (dialog, which) -> realizarCerrarSesion())
                    .show();
        });
    }

    private void enviarCorreoRecuperacion(String email) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Se ha enviado un correo a " + email + " para cambiar tu contraseña", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Error al enviar el correo: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void realizarCerrarSesion() {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences preferences = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Perfil.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}