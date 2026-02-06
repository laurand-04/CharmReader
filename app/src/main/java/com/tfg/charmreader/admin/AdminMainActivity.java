package com.tfg.charmreader.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.tfg.charmreader.R;
import com.tfg.charmreader.autentication.LoginActivity;

public class AdminMainActivity extends AppCompatActivity {

    private MaterialCardView cardUsuarios, cardGrupos, cardStats, cardBackup;
    private ImageButton btnAdminLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        // Enlaces con el XML
        cardUsuarios = findViewById(R.id.cardUsuarios);
        cardGrupos = findViewById(R.id.cardGrupos);
        cardStats = findViewById(R.id.cardStats);
        cardBackup = findViewById(R.id.cardBackup); // ID que añadimos al XML
        btnAdminLogout = findViewById(R.id.btnAdminLogout);

        // Listeners de navegación
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
                .setPositiveButton("SALIR", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("CANCELAR", null)
                .show();
    }
}