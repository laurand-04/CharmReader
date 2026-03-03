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
import com.tfg.charmreader.data.model.Libro;
import com.tfg.charmreader.data.model.LibrosDeUsuario;
import com.tfg.charmreader.databinding.FragmentTusLibrosBinding;
import com.tfg.charmreader.ui.priv.adapterRecyclerView.LibrosAdapter;
import com.tfg.charmreader.ui.priv.tusLibros.CargarNuevoLibroActivity;
import com.tfg.charmreader.ui.priv.tusLibros.VisorNActivity;
import com.tfg.charmreader.viewmodel.priv.fragmentView.TusLibrosFragmentViewModel;
import java.util.ArrayList;
import java.util.List;

public class TusLibrosFragment extends Fragment {

    private FragmentTusLibrosBinding binding;
    private TusLibrosFragmentViewModel viewModel;
    private LibrosAdapter adapter;
    private List<LibrosDeUsuario> relacionesActuales;

    public TusLibrosFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTusLibrosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TusLibrosFragmentViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupListeners();
    }

    private void setupRecyclerView() {
        binding.recyclerLibros.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LibrosAdapter(new ArrayList<>(), libro -> {
            if (relacionesActuales != null) {
                for (LibrosDeUsuario ldu : relacionesActuales) {
                    if (ldu.getId().getIdL() == libro.getId()) {
                        if (ldu.getFechaFin() != null) mostrarDialogoRelectura(ldu);
                        else abrirVisor(ldu, false);
                        return;
                    }
                }
            }
        });
        adapter.setSoloPendientes(true);
        adapter.setOnItemLongClickListener(this::mostrarDialogoEliminar);
        binding.recyclerLibros.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getRelacionesUsuario().observe(getViewLifecycleOwner(), relations ->
                this.relacionesActuales = relations);

        viewModel.getLibrosDetalle().observe(getViewLifecycleOwner(), libros -> {
            boolean vacio = libros.isEmpty();
            binding.layoutEmptyTusLibros.setVisibility(vacio ? View.VISIBLE : View.GONE);
            binding.recyclerLibros.setVisibility(vacio ? View.GONE : View.VISIBLE);
            adapter.setData(libros, relacionesActuales);
        });

        viewModel.getMensaje().observe(getViewLifecycleOwner(), msg ->
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show());
    }

    private void setupListeners() {
        binding.searchViewLibros.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                adapter.filtrar(newText);
                return true;
            }
        });

        binding.fabAdd.setOnClickListener(v -> {
            startActivityForResult(new Intent(getActivity(), CargarNuevoLibroActivity.class), 123);
        });
    }

    private void mostrarDialogoEliminar(Libro libro) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar libro")
                .setMessage("¿Deseas quitar '" + libro.getNombre() + "' de tu biblioteca?")
                .setNegativeButton("CANCELAR", null)
                .setPositiveButton("ELIMINAR", (d, w) -> viewModel.eliminarLibro(libro.getId()))
                .show();
    }

    private void mostrarDialogoRelectura(LibrosDeUsuario ldu) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("¡Libro ya leído!")
                .setMessage("¿Qué deseas hacer?")
                .setNeutralButton("CANCELAR", null)
                .setNegativeButton("REINICIAR", (d, w) -> viewModel.reiniciarLectura(ldu))
                .setPositiveButton("VER FINAL", (d, w) -> abrirVisor(ldu, false))
                .show();
    }

    private void abrirVisor(LibrosDeUsuario ldu, boolean esReinicio) {
        Intent intent = new Intent(getActivity(), VisorNActivity.class);
        intent.putExtra("OBJETO_LIBRO_USUARIO", ldu);
        intent.putExtra("URL_LIBRO", ldu.getRuta());
        intent.putExtra("idL", ldu.getId().getIdL());
        if (esReinicio) intent.putExtra("REINICIAR_LECTURA", true);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.cargarBiblioteca();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}