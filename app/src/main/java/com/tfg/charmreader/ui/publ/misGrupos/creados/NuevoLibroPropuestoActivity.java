package com.tfg.charmreader.ui.publ.misGrupos.creados;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.tfg.charmreader.R;
import com.tfg.charmreader.databinding.ActivityBuscadorApiExternaBinding;
import com.tfg.charmreader.ui.priv.adapterRecyclerView.BookExtAdapter;
import com.tfg.charmreader.viewmodel.publ.misGrupos.creados.NuevoLibroPropuestoViewModel;
import java.util.ArrayList;

public class NuevoLibroPropuestoActivity extends AppCompatActivity {

    private ActivityBuscadorApiExternaBinding binding;
    private NuevoLibroPropuestoViewModel viewModel;
    private BookExtAdapter adapter;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable = null;
    private int idGrupo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBuscadorApiExternaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        idGrupo = getIntent().getIntExtra("idGrupo", -1);
        viewModel = new ViewModelProvider(this).get(NuevoLibroPropuestoViewModel.class);

        setupUI();
        setupObservers();
    }

    private void setupUI() {
        binding.recyclerViewBook.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookExtAdapter(new ArrayList<>(), book -> viewModel.proponerLibroAGrupo(book, idGrupo));
        binding.recyclerViewBook.setAdapter(adapter);

        binding.btnBackBuscador.setOnClickListener(v -> finish());

        binding.searchViewAPI.setIconifiedByDefault(false);
        binding.searchViewAPI.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.trim().length() >= 3) viewModel.buscarLibro(query.trim());
                binding.searchViewAPI.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                String q = newText.trim();
                if (q.length() >= 3) {
                    searchRunnable = () -> viewModel.buscarLibro(q);
                    searchHandler.postDelayed(searchRunnable, 800);
                } else if (q.isEmpty()) {
                    mostrarEstadoVacio(true, true);
                }
                return true;
            }
        });
    }

    private void setupObservers() {
        viewModel.getResultados().observe(this, lista -> {
            boolean vacio = (lista == null || lista.isEmpty());
            mostrarEstadoVacio(vacio, binding.searchViewAPI.getQuery().toString().isEmpty());
            adapter.updateData(vacio ? new ArrayList<>() : lista);
        });

        viewModel.getIsLoading().observe(this, loading ->
                binding.progressBarSearch.setVisibility(loading ? View.VISIBLE : View.GONE));

        viewModel.getIsSaving().observe(this, saving -> {
            binding.layoutLoading.setVisibility(saving ? View.VISIBLE : View.GONE);
            binding.layoutContenido.setAlpha(saving ? 0.3f : 1.0f);
        });

        viewModel.getSuccessAction().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "¡Libro propuesto al grupo!", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
                finish();
            }
        });

        viewModel.getMensaje().observe(this, msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    private void mostrarEstadoVacio(boolean mostrar, boolean esInicio) {
        binding.layoutEmptyBuscador.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        binding.recyclerViewBook.setVisibility(mostrar ? View.GONE : View.VISIBLE);
        if (mostrar) {
            binding.tvEmptyTitleBuscador.setText(esInicio ? "Encuentra la próxima lectura" : "Sin resultados");
            binding.tvEmptySubtitleBuscador.setText(esInicio ? "Busca en la biblioteca global." : "Prueba con otros términos.");
            binding.ivEmptyIconBuscador.setImageResource(esInicio ? R.drawable.ic_search : R.drawable.ic_libro);
        }
    }
}