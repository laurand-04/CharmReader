package com.tfg.charmreader.admin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.tfg.charmreader.R;
import com.tfg.charmreader.autentication.LoginActivity;

public class AdminMainActivity extends AppCompatActivity {

    private CardView cardUsuarios, cardGrupos, cardStats, cardBackup;
    private ImageButton btnAdminLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Barra de estado blanca
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        setContentView(R.layout.activity_admin_main);

        vincularVistas();
        configurarListeners();
    }

    private void vincularVistas() {
        cardUsuarios = findViewById(R.id.cardUsuarios);
        cardGrupos = findViewById(R.id.cardGrupos);
        cardStats = findViewById(R.id.cardStats);
        cardBackup = findViewById(R.id.cardBackup);
        btnAdminLogout = findViewById(R.id.btnAdminLogout);
    }

    private void configurarListeners() {
        cardUsuarios.setOnClickListener(v -> startActivity(new Intent(this, GestionUsuarios.class)));
        cardGrupos.setOnClickListener(v -> startActivity(new Intent(this, GestionGrupos.class)));
        cardStats.setOnClickListener(v -> startActivity(new Intent(this, EstadisticasAdmin.class)));
        cardBackup.setOnClickListener(v -> startActivity(new Intent(this, GestionBackup.class)));

        btnAdminLogout.setOnClickListener(v -> mostrarDialogoCierre());
    }

    private void mostrarDialogoCierre() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cerrar Sesión Admin")
                .setMessage("¿Deseas salir del panel de administración?")
                .setNegativeButton("CANCELAR", null)
                .setPositiveButton("SALIR", (dialog, which) -> realizarCerrarSesionAdmin())
                .show();
    }

    // MÉTODO CORREGIDO: Ahora limpia SharedPreferences igual que en Perfil.java
    private void realizarCerrarSesionAdmin() {
        // 1. Cerrar sesión en Firebase
        FirebaseAuth.getInstance().signOut();

        // 2. Limpiar TODAS las preferencias de sesión (ID, flags, admin status, etc.)
        // Asegúrate de usar el mismo nombre de archivo que usas en el Login ("sesion_usuario")
        SharedPreferences preferences = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear(); // Esto borra todo: idUsuario, isAdmin, etc.
        editor.apply();

        // 3. Redirigir al Login y limpiar el historial de actividades
        Intent intent = new Intent(AdminMainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Finalizar la actividad actual
        finish();
    }
}