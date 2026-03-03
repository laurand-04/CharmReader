package com.tfg.charmreader.ui.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tfg.charmreader.databinding.ActivityAdminMainBinding;
import com.tfg.charmreader.ui.autentication.LoginActivity;
import com.tfg.charmreader.viewmodel.admin.AdminMainViewModel;

public class AdminMainActivity extends AppCompatActivity {

    private ActivityAdminMainBinding binding;
    private AdminMainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inflar Binding y Configurar UI
        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2. Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(AdminMainViewModel.class);
        viewModel.setContext(this);

        // 3. Configurar Listeners y Observadores
        configurarListeners();
        configurarObservadores();
    }

    private void configurarObservadores() {
        viewModel.getNavigateToLogin().observe(this, debeNavegar -> {
            if (debeNavegar) {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void configurarListeners() {
        // Navegación a gestiones
        binding.cardUsuarios.setOnClickListener(v -> startActivity(new Intent(this, GestionUsuariosActivity.class)));
        binding.cardGrupos.setOnClickListener(v -> startActivity(new Intent(this, GestionGruposActivity.class)));
        binding.cardStats.setOnClickListener(v -> startActivity(new Intent(this, EstadisticasAdminActivity.class)));
        binding.cardBackup.setOnClickListener(v -> startActivity(new Intent(this, GestionBackupActivity.class)));

        // Logout
        binding.btnAdminLogout.setOnClickListener(v -> mostrarDialogoCierre());
    }

    private void mostrarDialogoCierre() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cerrar Sesión Admin")
                .setMessage("¿Deseas salir del panel de administración?")
                .setNegativeButton("CANCELAR", null)
                .setPositiveButton("SALIR", (dialog, which) -> viewModel.logout())
                .show();
    }

}