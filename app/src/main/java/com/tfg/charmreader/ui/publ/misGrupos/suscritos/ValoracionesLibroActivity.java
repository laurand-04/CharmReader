package com.tfg.charmreader.ui.publ.misGrupos.suscritos;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.tfg.charmreader.databinding.ActivityValoracionesLibroBinding;
import com.tfg.charmreader.ui.publ.adapterReclyclerView.ValoracionAdapter;
import com.tfg.charmreader.viewmodel.publ.misGrupos.suscritos.ValoracionesLibroViewModel;

import java.util.ArrayList;

public class ValoracionesLibroActivity extends AppCompatActivity {

    private ActivityValoracionesLibroBinding binding;
    private ValoracionesLibroViewModel viewModel;
    private ValoracionAdapter adapter;
    private int idLibro, idGrupo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityValoracionesLibroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configurarStatusBar();

        idLibro = getIntent().getIntExtra("idLibro", -1);
        idGrupo = getIntent().getIntExtra("idGrupo", -1);

        if (idLibro == -1) { finish(); return; }

        viewModel = new ViewModelProvider(this).get(ValoracionesLibroViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargamos datos al volver de crear una reseña nueva
        viewModel.cargarValoraciones(idLibro);
    }

    private void setupRecyclerView() {
        binding.rvComentariosLibro.setLayoutManager(new LinearLayoutManager(this));
        // Usamos el mismo adaptador que ya tiene el método updateData
        adapter = new ValoracionAdapter(new ArrayList<>(), null);
        binding.rvComentariosLibro.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getValoraciones().observe(this, lista -> {
            adapter.updateData(lista);
            // Podrías añadir un layoutEmpty aquí también si lo deseas
        });

        viewModel.getMensajeError().observe(this, msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    private void setupListeners() {
        binding.btnBackValoraciones.setOnClickListener(v -> finish());

        binding.fabAddResena.setOnClickListener(v -> {
            Intent intent = new Intent(this, ValoracionLibroNuevaActivity.class);
            intent.putExtra("idLibro", idLibro);
            intent.putExtra("idGrupo", idGrupo);
            startActivity(intent);
        });
    }

    private void configurarStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }
}