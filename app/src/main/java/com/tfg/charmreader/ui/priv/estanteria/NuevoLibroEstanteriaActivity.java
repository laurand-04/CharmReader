package com.tfg.charmreader.ui.priv.estanteria;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.tfg.charmreader.databinding.ActivityNuevoLibroEstanteriaBinding;
import com.tfg.charmreader.ui.priv.adapterRecyclerView.LibrosAdapter;
import com.tfg.charmreader.viewmodel.priv.estanteria.NuevoLibroEstanteriaViewModel;

public class NuevoLibroEstanteriaActivity extends AppCompatActivity {
    private ActivityNuevoLibroEstanteriaBinding binding;
    private NuevoLibroEstanteriaViewModel viewModel;
    private LibrosAdapter adapter;
    private int idEstanteriaDestino;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNuevoLibroEstanteriaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        idEstanteriaDestino = getIntent().getIntExtra("idEstanteria", -1);
        viewModel = new ViewModelProvider(this).get(NuevoLibroEstanteriaViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupListeners();

        viewModel.cargarLibrosDisponibles(this);
    }

    private void setupRecyclerView() {
        binding.recyclerCargarNuevoLibroEstanteria.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupObservers() {
        // Observamos los detalles y las relaciones para armar el Adapter
        viewModel.getLibrosDetalles().observe(this, libros -> {
            if (libros.isEmpty()) {
                binding.layoutEmptyAddLibro.setVisibility(View.VISIBLE);
                binding.recyclerCargarNuevoLibroEstanteria.setVisibility(View.GONE);
            } else {
                binding.layoutEmptyAddLibro.setVisibility(View.GONE);
                binding.recyclerCargarNuevoLibroEstanteria.setVisibility(View.VISIBLE);

                adapter = new LibrosAdapter(libros, libro ->
                        viewModel.asignarLibro(this, libro.getId(), idEstanteriaDestino));

                adapter.setSoloPendientes(false);
                adapter.setData(libros, viewModel.getRelaciones().getValue());
                binding.recyclerCargarNuevoLibroEstanteria.setAdapter(adapter);
            }
        });

        viewModel.getIsLoading().observe(this, loading -> {
            // Podrías añadir un ProgressBar circular en el XML si lo deseas
            binding.recyclerCargarNuevoLibroEstanteria.setAlpha(loading ? 0.5f : 1.0f);
        });

        viewModel.getOperacionExitosa().observe(this, exito -> {
            if (exito) {
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void setupListeners() {
        binding.btnClose.setOnClickListener(v -> finish());

        binding.searchViewAddLibro.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) adapter.filtrar(newText);
                return true;
            }
        });
    }
}