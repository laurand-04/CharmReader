package com.tfg.charmreader.ui.autentication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.tfg.charmreader.R;
import com.tfg.charmreader.databinding.ActivityPerfilBinding;
import com.tfg.charmreader.utils.PDFReporter;
import com.tfg.charmreader.viewmodel.autentication.PerfilViewModel;

public class PerfilActivity extends AppCompatActivity {

    private ActivityPerfilBinding binding;
    private PerfilViewModel viewModel;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    viewModel.cambiarFoto(result.getData().getData());
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configurarEstadoBarra();

        viewModel = new ViewModelProvider(this).get(PerfilViewModel.class);
        viewModel.setContext(this);

        setupObservers();
        setupListeners();

        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        viewModel.cargarDatos(email);
    }

    private void setupObservers() {
        viewModel.getUserData().observe(this, usuario -> {
            binding.tvEmailUsuario.setText(usuario.getCorreo());
            binding.tvNombreUsuario.setText(usuario.getNombre() != null && !usuario.getNombre().isEmpty() ?
                    usuario.getNombre() : "Usuario #" + usuario.getId());

            Glide.with(this)
                    .load(usuario.getFoto())
                    .placeholder(R.drawable.ic_person)
                    .centerCrop()
                    .into(binding.ivFotoPerfil);
        });

        viewModel.getIsLoading().observe(this, loading -> {
            binding.btnAbrirEdicion.setEnabled(!loading);
            binding.btnExportarPDF.setEnabled(!loading);
        });

        viewModel.getMessage().observe(this, msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());

        // Escuchador para generar el PDF cuando los datos estén listos
        viewModel.getPdfReadyData().observe(this, wrapper -> {
            PDFReporter reporter = new PDFReporter(this);
            reporter.generarInformeLectura(viewModel.getUserData().getValue(),
                    wrapper.relaciones,
                    wrapper.libroMap,
                    null, null);
        });

        viewModel.getIsLoading().observe(this, loading -> {
            binding.btnAbrirEdicion.setEnabled(!loading);
            binding.btnExportarPDF.setEnabled(!loading);

            // Mostrar/ocultar ruleta de foto
            binding.pbFotoPerfil.setVisibility(loading ? View.VISIBLE : View.GONE);
        });
    }

    private void setupListeners() {
        binding.btnVolverPerfil.setOnClickListener(v -> finish());

        binding.btnAbrirEdicion.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        });

        binding.btnExportarPDF.setOnClickListener(v -> {
            if (viewModel.getUserData().getValue() != null) {
                viewModel.prepararDatosPDF(viewModel.getUserData().getValue().getId());
            }
        });

        binding.btnCerrarSesion.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Cerrar sesión")
                    .setMessage("¿Estás seguro?")
                    .setPositiveButton("SÍ", (d, w) -> cerrarSesion())
                    .setNegativeButton("NO", null)
                    .show();
        });

        binding.btnEditarNombre.setOnClickListener(v ->{
            mostrarDialogoNombre();
        });

        binding.btnCambiarPassword.setOnClickListener(v -> {
            viewModel.cambiarPassword();
        });
    }

    private void mostrarDialogoNombre() {
        if (viewModel.getUserData().getValue() == null) return;
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_editar_perfil);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        com.google.android.material.textfield.TextInputEditText et = dialog.findViewById(R.id.etNuevoNombreDialog);
        com.google.android.material.button.MaterialButton btn = dialog.findViewById(R.id.btnGuardarNombre);
        ImageView btnCerrar = dialog.findViewById(R.id.btnCerrarDialog);
        et.setText(viewModel.getUserData().getValue().getNombre());
        if (btnCerrar != null) btnCerrar.setOnClickListener(v -> dialog.dismiss());
        btn.setOnClickListener(v -> {
            String nuevoNom = et.getText().toString().trim();
            if (!nuevoNom.isEmpty()) {
                viewModel.getUserData().getValue().setNombre(nuevoNom);
                viewModel.actualizarUsuario(viewModel.getUserData().getValue());
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void cerrarSesion() {
        FirebaseAuth.getInstance().signOut();
        getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void configurarEstadoBarra() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }
}