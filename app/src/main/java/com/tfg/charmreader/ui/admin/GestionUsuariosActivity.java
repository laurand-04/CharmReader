package com.tfg.charmreader.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.tfg.charmreader.databinding.ActivityGestionUsuariosBinding;
import com.tfg.charmreader.ui.admin.adapterRecyclerView.UsuarioAdapter;
import com.tfg.charmreader.viewmodel.admin.GestionUsuariosViewModel;

public class GestionUsuariosActivity extends AppCompatActivity {

    private ActivityGestionUsuariosBinding binding;
    private GestionUsuariosViewModel viewModel;
    private UsuarioAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGestionUsuariosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(GestionUsuariosViewModel.class);
        viewModel.setContext(this);

        setupRecyclerView();
        setupObservers();

        binding.btnBackGestionUsuarios.setOnClickListener(v -> finish());

        viewModel.cargarUsuarios();
    }

    private void setupRecyclerView() {
        binding.rvUsuariosAdmin.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupObservers() {
        viewModel.getUsuarios().observe(this, usuarios -> {
            adapter = new UsuarioAdapter(usuarios, usuario -> viewModel.eliminarUsuario(usuario));
            binding.rvUsuariosAdmin.setAdapter(adapter);
        });

        viewModel.getIsLoading().observe(this, loading -> {
            binding.layoutLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.layoutContenido.setAlpha(loading ? 0.4f : 1.0f);
        });

        viewModel.getMessage().observe(this, msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }
}