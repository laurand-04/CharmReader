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
import com.tfg.charmreader.data.model.Estanteria;
import com.tfg.charmreader.databinding.FragmentEstanteriaBinding;
import com.tfg.charmreader.ui.priv.adapterRecyclerView.EstanteriasAdapter;
import com.tfg.charmreader.ui.priv.estanteria.LibrosEstanteriaActivity;
import com.tfg.charmreader.ui.priv.estanteria.NuevaEstanteriaActivity;
import com.tfg.charmreader.viewmodel.priv.fragmentView.EstanteriaFragmentViewModel;

import java.util.ArrayList;

public class EstanteriaFragment extends Fragment {

    private FragmentEstanteriaBinding binding;
    private EstanteriaFragmentViewModel viewModel;
    private EstanteriasAdapter adapter;

    public EstanteriaFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEstanteriaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(EstanteriaFragmentViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupListeners();

        viewModel.cargarEstanterias();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.cargarEstanterias();
        }
    }

    private void setupRecyclerView() {
        binding.recyclerEstanterias.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EstanteriasAdapter(new ArrayList<>(), estanteria -> {
            Intent intent = new Intent(getActivity(), LibrosEstanteriaActivity.class);
            intent.putExtra("Nombre", estanteria.getNombre());
            intent.putExtra("idEstanteria", estanteria.getId());
            intent.putExtra("Color", estanteria.getColor());
            startActivity(intent);
        });

        adapter.setOnItemLongClickListener(this::mostrarDialogoEliminar);
        binding.recyclerEstanterias.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getEstanterias().observe(getViewLifecycleOwner(), lista -> {
            boolean vacio = lista.isEmpty();
            binding.layoutEmptyEstanteria.setVisibility(vacio ? View.VISIBLE : View.GONE);
            binding.recyclerEstanterias.setVisibility(vacio ? View.GONE : View.VISIBLE);
            adapter.setEstanterias(lista);
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), msg ->
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show());
    }

    private void setupListeners() {
        binding.searchViewEstanterias.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                adapter.filtrar(newText);
                return true;
            }
        });

        binding.fabAddEstanteria.setOnClickListener(v -> {
            startActivityForResult(new Intent(getActivity(), NuevaEstanteriaActivity.class), 123);
        });
    }

    private void mostrarDialogoEliminar(Estanteria e) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar estantería")
                .setMessage("¿Eliminar '" + e.getNombre() + "'?")
                .setNegativeButton("CANCELAR", null)
                .setPositiveButton("ELIMINAR", (d, w) -> viewModel.eliminarEstanteria(e))
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 123 && resultCode == Activity.RESULT_OK) viewModel.cargarEstanterias();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}