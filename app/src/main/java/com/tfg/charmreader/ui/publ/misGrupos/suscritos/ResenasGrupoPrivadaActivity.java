package com.tfg.charmreader.ui.publ.misGrupos.suscritos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.tfg.charmreader.data.model.GrupoLectura;
import com.tfg.charmreader.databinding.ActivityResenasGrupoPrivadaBinding;
import com.tfg.charmreader.ui.publ.adapterReclyclerView.ValoracionAdapter;
import com.tfg.charmreader.viewmodel.publ.misGrupos.suscritos.ResenasGrupoViewModel;

import java.util.ArrayList;

public class ResenasGrupoPrivadaActivity extends AppCompatActivity {

    private ActivityResenasGrupoPrivadaBinding binding;
    private ResenasGrupoViewModel viewModel;
    private ValoracionAdapter adapter;
    private GrupoLectura grupo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResenasGrupoPrivadaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        grupo = (GrupoLectura) getIntent().getSerializableExtra("objetoGrupo");
        if (grupo == null) { finish(); return; }

        viewModel = new ViewModelProvider(this).get(ResenasGrupoViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.cargarResenas(grupo.getIdGrupo());
    }

    private void setupRecyclerView() {
        binding.rvResenasGrupo.setLayoutManager(new LinearLayoutManager(this));
        // Inicializamos con lista vacía, el LiveData se encargará de llenarla
        adapter = new ValoracionAdapter(new ArrayList<>(), valoracion -> {
            // Acción opcional al pulsar
        });
        binding.rvResenasGrupo.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getValoraciones().observe(this, lista -> {
            adapter.updateData(lista); // Asegúrate de tener updateData en el ValoracionAdapter
            actualizarInterfaz(lista.isEmpty());
        });

        viewModel.getIsLoading().observe(this, loading -> {
            // Podrías añadir un ProgressBar en el XML y controlarlo aquí
        });

        viewModel.getMensajeError().observe(this, msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    private void setupListeners() {
        binding.btnBackResenas.setOnClickListener(v -> finish());

        binding.fabNuevaResena.setOnClickListener(v -> {
            Intent intent = new Intent(this, ValoracionGrupoActivity.class);
            intent.putExtra("idGrupo", grupo.getIdGrupo());
            intent.putExtra("idLibro", -1);
            startActivity(intent);
        });
    }

    private void actualizarInterfaz(boolean vacio) {
        binding.layoutEmptyResenas.setVisibility(vacio ? View.VISIBLE : View.GONE);
        binding.rvResenasGrupo.setVisibility(vacio ? View.GONE : View.VISIBLE);
    }
}