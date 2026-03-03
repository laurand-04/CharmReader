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
import com.tfg.charmreader.data.model.BookEn;
import com.tfg.charmreader.databinding.FragmentProximamenteBinding;
import com.tfg.charmreader.ui.priv.adapterRecyclerView.BookIntAdapter;
import com.tfg.charmreader.ui.priv.proximamente.BuscadorAPIExternaActivity;
import com.tfg.charmreader.ui.priv.proximamente.ProximoLibroActivity;
import com.tfg.charmreader.viewmodel.priv.fragmentView.ProximamenteFragmentViewModel;

import java.util.ArrayList;

public class ProximamenteFragment extends Fragment {

    private FragmentProximamenteBinding binding;
    private ProximamenteFragmentViewModel viewModel;
    private BookIntAdapter adapter;

    public ProximamenteFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProximamenteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProximamenteFragmentViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupListeners();

        viewModel.cargarLibros();
    }

    private void setupRecyclerView() {
        binding.recyclerProximamente.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BookIntAdapter(new ArrayList<>(), libro -> {
            Intent intent = new Intent(getActivity(), ProximoLibroActivity.class);
            intent.putExtra("idLibro", libro.getId());
            startActivity(intent);
        });

        adapter.setOnItemLongClickListener(this::mostrarDialogoEliminar);
        binding.recyclerProximamente.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getLibros().observe(getViewLifecycleOwner(), lista -> {
            boolean vacio = lista.isEmpty();
            binding.layoutEmpty.setVisibility(vacio ? View.VISIBLE : View.GONE);
            binding.recyclerProximamente.setVisibility(vacio ? View.GONE : View.VISIBLE);

            adapter.setBooks(lista);
            int total = lista.size();
            binding.tvCountProximamente.setText((total == 1) ? "1 LIBRO PENDIENTE" : total + " LIBROS PENDIENTES");
        });

        viewModel.getMensaje().observe(getViewLifecycleOwner(), msg ->
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show());
    }

    private void setupListeners() {
        binding.searchViewProximamente.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                adapter.filtrar(newText);
                return true;
            }
        });

        binding.fabAddProximamente.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), BuscadorAPIExternaActivity.class);
            startActivityForResult(intent, 123);
        });
    }

    private void mostrarDialogoEliminar(BookEn book) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar libro")
                .setMessage("¿Deseas eliminar '" + book.getTitulo() + "'?")
                .setNegativeButton("CANCELAR", null)
                .setPositiveButton("ELIMINAR", (d, w) -> viewModel.eliminarLibro(book))
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
        binding = null;
    }
}