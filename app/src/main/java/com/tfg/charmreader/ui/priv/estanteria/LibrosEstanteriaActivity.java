package com.tfg.charmreader.ui.priv.estanteria;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.tfg.charmreader.databinding.ActivityLibrosEstanteriaBinding;
import com.tfg.charmreader.ui.priv.adapterRecyclerView.LibrosAdapter;
import com.tfg.charmreader.data.model.Libro;
import com.tfg.charmreader.data.model.LibrosDeUsuario;
import com.tfg.charmreader.viewmodel.priv.estanteria.LibrosEstanteriaViewModel;

public class LibrosEstanteriaActivity extends AppCompatActivity {
    private ActivityLibrosEstanteriaBinding binding;
    private LibrosEstanteriaViewModel viewModel;
    private LibrosAdapter adapter;
    private int idEstanteria;

    private final ActivityResultLauncher<Intent> launcherCargarLibro = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> { if (result.getResultCode() == RESULT_OK) viewModel.cargarLibrosPorEstanteria(this, idEstanteria); }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLibrosEstanteriaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        idEstanteria = getIntent().getIntExtra("idEstanteria", -1);
        viewModel = new ViewModelProvider(this).get(LibrosEstanteriaViewModel.class);

        aplicarColoresDinamicos();
        setupRecyclerView();
        setupObservers();
        setupListeners();

        viewModel.cargarLibrosPorEstanteria(this, idEstanteria);
    }

    private void setupObservers() {
        viewModel.getLibros().observe(this, libros -> {
            actualizarInterfaz(libros);
            adapter = new LibrosAdapter(libros, libro -> abrirValoracion(libro));
            adapter.setSoloPendientes(false);
            adapter.setOnItemLongClickListener(this::mostrarDialogoQuitar);
            adapter.setData(libros, viewModel.getRelaciones().getValue());
            binding.recyclerLibrosEstanteria.setAdapter(adapter);
        });

        viewModel.getIsLoading().observe(this, loading -> {
            binding.layoutLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.layoutContenido.setAlpha(loading ? 0.4f : 1.0f);
        });

        viewModel.getMensaje().observe(this, msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    private void abrirValoracion(Libro libro) {
        for (LibrosDeUsuario ldu : viewModel.getRelaciones().getValue()) {
            if (ldu.getId().getIdL() == libro.getId()) {
                Intent intent = new Intent(this, ValoracionLibroActivity.class);
                intent.putExtra("OBJETO_LIBRO_USUARIO", ldu);
                intent.putExtra("URL_LIBRO", ldu.getRuta());
                intent.putExtra("idL", ldu.getId().getIdL());
                intent.putExtra("idU", ldu.getId().getIdU());
                startActivity(intent);
                return;
            }
        }
    }

    private void actualizarInterfaz(java.util.List<Libro> libros) {
        boolean vacio = libros.isEmpty();
        binding.layoutEmptyLibrosEstanteria.setVisibility(vacio ? View.VISIBLE : View.GONE);
        binding.recyclerLibrosEstanteria.setVisibility(vacio ? View.GONE : View.VISIBLE);
        int total = libros.size();
        binding.tvCantidadLibrosEstanteria.setText((total == 1) ? "1 libro" : total + " libros");
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.fabAddLibrosEstanteria.setOnClickListener(v -> {
            Intent intent = new Intent(this, NuevoLibroEstanteriaActivity.class);
            intent.putExtra("idEstanteria", idEstanteria);
            launcherCargarLibro.launch(intent);
        });

        binding.searchViewLibrosEst.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { return false; }
            @Override public boolean onQueryTextChange(String n) {
                if (adapter != null) adapter.filtrar(n);
                return true;
            }
        });
    }

    private void mostrarDialogoQuitar(Libro libro) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Quitar de la estantería")
                .setMessage("¿Deseas quitar '" + libro.getNombre() + "'?")
                .setNegativeButton("CANCELAR", null)
                .setPositiveButton("QUITAR", (d, w) -> viewModel.desvincularLibro(this, libro.getId(), idEstanteria))
                .show();
    }

    private void aplicarColoresDinamicos() {
        String nombre = getIntent().getStringExtra("Nombre");
        String colorPastel = getIntent().getStringExtra("Color");
        if (nombre != null) binding.tvTituloEstanteria.setText(nombre);
        if (colorPastel != null) {
            int fuerte = obtenerColorFuerte(colorPastel);
            binding.viewEstanteriaColor.getBackground().setColorFilter(fuerte, PorterDuff.Mode.SRC_IN);
            binding.statusColorContainer.setStrokeColor(ColorStateList.valueOf(fuerte));
            binding.btnBack.setStrokeColor(ColorStateList.valueOf(fuerte));
            binding.tvCantidadLibrosEstanteria.setTextColor(fuerte);
            binding.fabAddLibrosEstanteria.setBackgroundTintList(ColorStateList.valueOf(fuerte));
        }
    }

    private int obtenerColorFuerte(String pastel) {
        switch (pastel.toUpperCase()) {
            case "#F3E5F5": return Color.parseColor("#664FA4");
            case "#E3F2FD": return Color.parseColor("#1976D2");
            case "#E8F5E9": return Color.parseColor("#388E3C");
            case "#FFF3E0": return Color.parseColor("#F57C00");
            case "#FFEBEE": return Color.parseColor("#C2185B");
            default: return Color.parseColor("#664FA4");
        }
    }

    private void setupRecyclerView() {
        binding.recyclerLibrosEstanteria.setLayoutManager(new LinearLayoutManager(this));
    }
}