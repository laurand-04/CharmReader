package com.tfg.charmreader.ui.priv.fragmentView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tfg.charmreader.data.model.LibrosSinEstrenar;
import com.tfg.charmreader.databinding.FragmentEsperaBinding;
import com.tfg.charmreader.ui.priv.adapterRecyclerView.LibrosSinEstrenarAdapter;
import com.tfg.charmreader.ui.priv.futuro.FuturoLibroActivity;
import com.tfg.charmreader.ui.priv.futuro.NuevoFuturoLibroActivity;
import com.tfg.charmreader.viewmodel.priv.fragmentView.EsperaFragmentViewModel;

import java.util.ArrayList;

public class EsperaFragment extends Fragment {

    private FragmentEsperaBinding binding; // Variable para ViewBinding
    private EsperaFragmentViewModel viewModel;
    private LibrosSinEstrenarAdapter adapter;

    public EsperaFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inicializamos el Binding
        binding = FragmentEsperaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(EsperaFragmentViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupListeners();

        viewModel.cargarLibros();
    }

    private void setupRecyclerView() {
        binding.recyclerLibrosSinEstrenar.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LibrosSinEstrenarAdapter(new ArrayList<>(), libro -> {
            Intent intent = new Intent(getActivity(), FuturoLibroActivity.class);
            intent.putExtra("libro_seleccionado", libro);
            startActivity(intent);
        });

        adapter.setOnItemLongClickListener(this::mostrarDialogoEliminar);
        binding.recyclerLibrosSinEstrenar.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getLibros().observe(getViewLifecycleOwner(), lista -> {
            actualizarInterfaz(new ArrayList<>(lista));
        });

        viewModel.getMensaje().observe(getViewLifecycleOwner(), msg -> {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });

        // Puedes observar el isLoading para mostrar un ProgressBar si lo tienes en el XML
    }

    private void setupListeners() {
        binding.searchViewEspera.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                adapter.filtrar(newText);
                return true;
            }
        });

        binding.fabAddLibrosSinEstrenar.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NuevoFuturoLibroActivity.class);
            startActivityForResult(intent, 123);
        });
    }

    private void mostrarDialogoEliminar(LibrosSinEstrenar libro) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar lanzamiento")
                .setMessage("¿Deseas quitar '" + libro.getId().getNombre() + "' de la lista?")
                .setNegativeButton("CANCELAR", null)
                .setPositiveButton("ELIMINAR", (dialog, which) -> viewModel.eliminarLibro(libro))
                .show();
    }

    private void actualizarInterfaz(ArrayList<LibrosSinEstrenar> lista) {
        boolean vacio = lista.isEmpty();
        binding.layoutEmptyEspera.setVisibility(vacio ? View.VISIBLE : View.GONE);
        binding.recyclerLibrosSinEstrenar.setVisibility(vacio ? View.GONE : View.VISIBLE);

        adapter.setLibros(lista);
        int total = lista.size();
        binding.tvCountEspera.setText((total == 1) ? "1 LANZAMIENTO PENDIENTE" : total + " LANZAMIENTOS PENDIENTES");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            viewModel.cargarLibros();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.cargarLibros();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Muy importante para evitar Memory Leaks
    }
}