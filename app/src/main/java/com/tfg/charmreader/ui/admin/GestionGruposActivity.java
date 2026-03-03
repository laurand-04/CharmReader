package com.tfg.charmreader.ui.admin;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.tfg.charmreader.databinding.ActivityGestionGruposBinding;
import com.tfg.charmreader.ui.admin.adapterRecyclerView.GrupoAdapter;
import com.tfg.charmreader.viewmodel.admin.GestionGruposViewModel;

public class GestionGruposActivity extends AppCompatActivity {

    private ActivityGestionGruposBinding binding;
    private GestionGruposViewModel viewModel;
    private GrupoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGestionGruposBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configurarBarraEstado();

        viewModel = new ViewModelProvider(this).get(GestionGruposViewModel.class);

        setupRecyclerView();
        setupObservers();

        binding.btnBackGestionGrupos.setOnClickListener(v -> finish());

        viewModel.cargarGrupos();
    }

    private void setupRecyclerView() {
        binding.rvGruposAdmin.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupObservers() {
        viewModel.getGrupos().observe(this, grupos -> {
            adapter = new GrupoAdapter(grupos, grupo -> viewModel.eliminarGrupo(grupo));
            binding.rvGruposAdmin.setAdapter(adapter);
        });

        viewModel.getIsLoading().observe(this, loading -> {
            binding.layoutLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.layoutContenido.setAlpha(loading ? 0.4f : 1.0f);
        });

        viewModel.getMessage().observe(this, msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    private void configurarBarraEstado() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }
}