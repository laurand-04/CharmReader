package com.tfg.charmreader.ui.publ.misGrupos.creados;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tfg.charmreader.R;
import com.tfg.charmreader.data.model.Sesion;
import com.tfg.charmreader.databinding.ActivityGestionSesionesBinding;
import com.tfg.charmreader.ui.publ.adapterReclyclerView.SesionAdapter;
import com.tfg.charmreader.viewmodel.publ.misGrupos.creados.SesionViewModel;

import java.util.ArrayList;

public class GestionSesionesActivity extends AppCompatActivity {

    private ActivityGestionSesionesBinding binding;
    private SesionViewModel viewModel;
    private int idGrupo;
    private SesionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGestionSesionesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.getRoot().setBackgroundColor(getColor(R.color.ic_launcher_background));
        binding.getRoot().setFitsSystemWindows(true);


        idGrupo = getIntent().getIntExtra("idGrupo", -1);
        if (idGrupo == -1) { finish(); return; }

        viewModel = new ViewModelProvider(this).get(SesionViewModel.class);

        setupRecyclerView();
        setupObservers();

        binding.btnBackSesiones.setOnClickListener(v -> finish());
        binding.fabNuevaSesionDetalle.setOnClickListener(v -> {
            Intent intent = new Intent(this, NuevaSesionActivity.class);
            intent.putExtra("idGrupo", idGrupo);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.cargarSesiones(idGrupo);
    }

    private void setupRecyclerView() {
        binding.rvSesiones.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SesionAdapter(new ArrayList<>(), this::confirmarEliminacion);
        binding.rvSesiones.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getSesiones().observe(this, sesiones -> {
            adapter.updateData(sesiones); // Asegúrate de tener este métod0 en el Adapter
            boolean vacio = sesiones.isEmpty();
            binding.layoutEmptySesiones.setVisibility(vacio ? View.VISIBLE : View.GONE);
            binding.rvSesiones.setVisibility(vacio ? View.GONE : View.VISIBLE);
        });

        viewModel.getIsLoading().observe(this, loading -> {
            binding.layoutLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.layoutContenido.setAlpha(loading ? 0.4f : 1.0f);
        });

        viewModel.getMensaje().observe(this, msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    private void confirmarEliminacion(Sesion s) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar Sesión")
                .setMessage("¿Deseas cancelar esta reunión definitivamente?")
                .setNegativeButton("No", null)
                .setPositiveButton("Sí, eliminar", (dialog, which) -> viewModel.eliminarSesion(s))
                .show();
    }
}