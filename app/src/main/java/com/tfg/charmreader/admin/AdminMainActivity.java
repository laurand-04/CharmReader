package com.tfg.charmreader.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.tfg.charmreader.R;
import com.tfg.charmreader.autentication.LoginActivity;

public class AdminMainActivity extends AppCompatActivity {

    private MaterialCardView cardUsuarios, cardGrupos, cardStats;
    private ImageButton btnAdminLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        cardUsuarios = findViewById(R.id.cardUsuarios);
        cardGrupos = findViewById(R.id.cardGrupos);
        cardStats = findViewById(R.id.cardStats);
        btnAdminLogout = findViewById(R.id.btnAdminLogout);

        cardUsuarios.setOnClickListener(v -> {
            Intent intent = new Intent(this, GestionUsuarios.class);
            startActivity(intent);
        });

        cardGrupos.setOnClickListener(v -> {
            // CAMBIO: Ahora sí navegamos a la actividad de gestión
            Intent intent = new Intent(this, GestionGrupos.class);
            startActivity(intent);
        });

        cardStats.setOnClickListener(v -> {
            Intent intent = new Intent(this, EstadisticasAdmin.class);
            startActivity(intent);
        });

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