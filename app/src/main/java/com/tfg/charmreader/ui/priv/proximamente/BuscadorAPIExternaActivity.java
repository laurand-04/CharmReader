package com.tfg.charmreader.ui.priv.proximamente;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
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
import com.tfg.charmreader.viewmodel.priv.proximamente.BuscadorAPIExternaViewModel;
import java.util.ArrayList;

public class BuscadorAPIExternaActivity extends AppCompatActivity {

    private ActivityBuscadorApiExternaBinding binding;
    private BuscadorAPIExternaViewModel viewModel;
    private BookExtAdapter adapter;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBuscadorApiExternaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configurarEstadoBarra();
        viewModel = new ViewModelProvider(this).get(BuscadorAPIExternaViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupBuscador();

        binding.btnBackBuscador.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        binding.recyclerViewBook.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookExtAdapter(new ArrayList<>(), book -> viewModel.añadirADeseos(this, book));
        binding.recyclerViewBook.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getResultados().observe(this, lista -> {
            if (lista == null || lista.isEmpty()) {
                boolean esInicio = binding.searchViewAPI.getQuery().toString().isEmpty();
                mostrarEstadoVacio(true, esInicio);
                adapter.updateData(new ArrayList<>());
            } else {
                mostrarEstadoVacio(false, false);
                adapter.updateData(lista);
            }
        });

        viewModel.getIsLoading().observe(this, loading ->
                binding.progressBarSearch.setVisibility(loading ? View.VISIBLE : View.GONE));

        viewModel.getIsSaving().observe(this, saving -> {
            binding.layoutLoading.setVisibility(saving ? View.VISIBLE : View.GONE);
            binding.layoutContenido.setAlpha(saving ? 0.3f : 1.0f);
        });

        viewModel.getMensaje().observe(this, msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());

        viewModel.getSuccessSaving().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Añadido a Deseos", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
    }

    private void setupBuscador() {
        binding.searchViewAPI.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.trim().length() >= 3) viewModel.buscarLibro(query.trim());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                String query = newText.trim();
                if (query.length() >= 3) {
                    searchRunnable = () -> viewModel.buscarLibro(query);
                    searchHandler.postDelayed(searchRunnable, 800);
                } else if (query.isEmpty()) {
                    mostrarEstadoVacio(true, true);
                    adapter.updateData(new ArrayList<>());
                }
                return true;
            }
        });
    }

    private void mostrarEstadoVacio(boolean mostrar, boolean esInicio) {
        binding.layoutEmptyBuscador.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        binding.recyclerViewBook.setVisibility(mostrar ? View.GONE : View.VISIBLE);
        if (mostrar) {
            if (esInicio) {
                binding.ivEmptyIconBuscador.setImageResource(R.drawable.ic_search);
                binding.tvEmptyTitleBuscador.setText("Encuentra tu próxima lectura");
                binding.tvEmptySubtitleBuscador.setText("Busca libros por título o autor en la biblioteca global de OpenLibrary.");
            } else {
                binding.ivEmptyIconBuscador.setImageResource(R.drawable.ic_libro);
                binding.tvEmptyTitleBuscador.setText("Sin resultados");
                binding.tvEmptySubtitleBuscador.setText("No hemos encontrado libros que coincidan.");
            }
        }
    }

    private void configurarEstadoBarra() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }
}