package com.tfg.charmreader.ui.publ.misGrupos.creados;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tfg.charmreader.databinding.ActivitySuscritosBinding;
import com.tfg.charmreader.ui.publ.adapterReclyclerView.MiembroAdapter;
import com.tfg.charmreader.viewmodel.publ.misGrupos.creados.SuscritosViewModel;
import java.util.ArrayList;

public class SuscritosActivity extends AppCompatActivity {

    private ActivitySuscritosBinding binding;
    private SuscritosViewModel viewModel;
    private int idGrupo;
    private MiembroAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySuscritosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configurarStatusBar();
        idGrupo = getIntent().getIntExtra("idGrupo", -1);
        if (idGrupo == -1) { finish(); return; }

        viewModel = new ViewModelProvider(this).get(SuscritosViewModel.class);

        setupRecyclerView();
        setupObservers();

        binding.btnBackSuscritos.setOnClickListener(v -> finish());
        viewModel.cargarMiembros(idGrupo);
    }

    private void setupRecyclerView() {
        binding.rvSuscritos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MiembroAdapter(new ArrayList<>(), this::confirmarEliminacion);
        binding.rvSuscritos.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getMiembros().observe(this, lista -> {
            adapter.updateData(lista); // Asegúrate de añadir updateData en tu MiembroAdapter
        });

        viewModel.getIsLoading().observe(this, loading -> {
            binding.layoutLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.layoutContenido.setAlpha(loading ? 0.4f : 1.0f);
        });

        viewModel.getMensaje().observe(this, msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    private void confirmarEliminacion(int idUsuario) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("⚠️ Expulsar miembro")
                .setMessage("¿Estás seguro de que quieres eliminar a este usuario del grupo?")
                .setPositiveButton("Expulsar", (dialog, which) -> viewModel.expulsarUsuario(idGrupo, idUsuario))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void configurarStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }
}