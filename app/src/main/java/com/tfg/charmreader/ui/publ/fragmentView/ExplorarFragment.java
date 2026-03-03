package com.tfg.charmreader.ui.publ.fragmentView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.tfg.charmreader.databinding.FragmentExplorarBinding;
import com.tfg.charmreader.ui.publ.adapterReclyclerView.GrupoLecturaAdapter;
import com.tfg.charmreader.ui.publ.explorar.InfoGrupoPublicaActivity;
import com.tfg.charmreader.ui.publ.explorar.NuevoGrupoActivity;
import com.tfg.charmreader.viewmodel.publ.fragmentView.ExplorarFragmentViewModel;

import java.util.ArrayList;

public class ExplorarFragment extends Fragment {

    private FragmentExplorarBinding binding;
    private ExplorarFragmentViewModel viewModel;
    private GrupoLecturaAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentExplorarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ExplorarFragmentViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupListeners();

        viewModel.cargarGrupos();
    }

    private void setupRecyclerView() {
        binding.rvGrupos.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GrupoLecturaAdapter(new ArrayList<>(), grupo -> {
            Intent intent = new Intent(getActivity(), InfoGrupoPublicaActivity.class);
            intent.putExtra("objetoGrupo", grupo);
            startActivity(intent);
        });
        binding.rvGrupos.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getGrupos().observe(getViewLifecycleOwner(), lista -> {
            boolean vacio = lista.isEmpty();
            binding.layoutEmptyGrupos.setVisibility(vacio ? View.VISIBLE : View.GONE);
            binding.rvGrupos.setVisibility(vacio ? View.GONE : View.VISIBLE);
            adapter.setGrupoLectura(lista);
        });

        viewModel.getGrupoEncontrado().observe(getViewLifecycleOwner(), grupo -> {
            if (grupo != null) {
                Intent intent = new Intent(getActivity(), InfoGrupoPublicaActivity.class);
                intent.putExtra("objetoGrupo", grupo);
                startActivity(intent);
            }
        });

        viewModel.getMensajeError().observe(getViewLifecycleOwner(), msg ->
                mostrarAlerta("Aviso", msg));
    }

    private void setupListeners() {
        binding.fabCrearGrupo.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), NuevoGrupoActivity.class)));

        binding.searchViewGrupos.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) viewModel.buscarGrupoPorNombre(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) adapter.filtrar(newText);
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.cargarGrupos();
    }

    private void mostrarAlerta(String titulo, String contenido) {
        new AlertDialog.Builder(requireContext())
                .setTitle(titulo)
                .setMessage(contenido)
                .setPositiveButton("Entendido", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}