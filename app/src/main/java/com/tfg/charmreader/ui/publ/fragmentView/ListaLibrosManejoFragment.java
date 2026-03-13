package com.tfg.charmreader.ui.publ.fragmentView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tfg.charmreader.R;
import com.tfg.charmreader.data.model.BookEn;
import com.tfg.charmreader.databinding.FragmentListaLibrosManejoBinding;
import com.tfg.charmreader.ui.priv.proximamente.ProximoLibroActivity;
import com.tfg.charmreader.ui.publ.adapterReclyclerView.LibroPropuestoAdministradorAdapter;
import com.tfg.charmreader.viewmodel.publ.fragmentView.ListaLibrosManejoFragmentViewModel;
import com.tfg.charmreader.viewmodel.publ.misGrupos.creados.ManejoGrupoViewModel;

import java.util.ArrayList;

public class ListaLibrosManejoFragment extends Fragment {

    private FragmentListaLibrosManejoBinding binding;
    private ManejoGrupoViewModel sharedViewModel;
    private ListaLibrosManejoFragmentViewModel viewModel;
    private int idGrupo, tipoLista;
    private LibroPropuestoAdministradorAdapter adapter;

    public static ListaLibrosManejoFragment newInstance(int idGrupo, int tipoLista) {
        ListaLibrosManejoFragment fragment = new ListaLibrosManejoFragment();
        Bundle args = new Bundle();
        args.putInt("idGrupo", idGrupo);
        args.putInt("tipoLista", tipoLista);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentListaLibrosManejoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        idGrupo = getArguments().getInt("idGrupo");
        tipoLista = getArguments().getInt("tipoLista");

        // ViewModels
        sharedViewModel = new ViewModelProvider(requireActivity()).get(ManejoGrupoViewModel.class);
        viewModel = new ViewModelProvider(this).get(ListaLibrosManejoFragmentViewModel.class);

        setupRecyclerView();
        setupObservers();
    }

    private void setupRecyclerView() {
        binding.rvListaManejo.setLayoutManager(new LinearLayoutManager(getContext()));
        // Pasamos una lista vacía inicial, el adapter se actualizará por LiveData
        adapter = new LibroPropuestoAdministradorAdapter(new ArrayList<>(), idGrupo, libro -> {
            if (tipoLista == 0) {
                Intent intent = new Intent(getActivity(), ProximoLibroActivity.class);
                intent.putExtra("mostrarSubirEpub", true);
                intent.putExtra("idLibro", libro.getId());
                startActivity(intent);
            }
        });

        adapter.setOnLibroLongClickListener(libro -> {
            if (tipoLista == 0) mostrarDialogoEliminar(libro);
        });

        binding.rvListaManejo.setAdapter(adapter);
    }

    private void setupObservers() {
        // Observar la lista de libros
        viewModel.getLibros().observe(getViewLifecycleOwner(), libros -> {
            adapter.updateLista(libros);
            mostrarEstadoVacio(libros.isEmpty());
        });

        // Observar mensajes de Toast
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });

        // Observar trigger de refresco global (de ManejoGrupoViewModel)
        sharedViewModel.getRefreshTrigger().observe(getViewLifecycleOwner(), shouldRefresh -> {
            if (shouldRefresh) viewModel.cargarDatos(idGrupo, tipoLista);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.cargarDatos(idGrupo, tipoLista);
    }

    private void mostrarEstadoVacio(boolean vacio) {
        binding.layoutEmptyManejo.setVisibility(vacio ? View.VISIBLE : View.GONE);
        binding.rvListaManejo.setVisibility(vacio ? View.GONE : View.VISIBLE);
        if (vacio) {
            switch (tipoLista) {
                case 0:
                    binding.ivEmptyIconManejo.setImageResource(R.drawable.ic_libro);
                    binding.tvEmptyTitleManejo.setText("¡Lluvia de ideas!");
                    binding.tvEmptySubtitleManejo.setText("No hay propuestas todavía.");
                    break;
                case 1:
                    binding.ivEmptyIconManejo.setImageResource(R.drawable.ic_people);
                    binding.tvEmptyTitleManejo.setText("Tiempo de descanso");
                    binding.tvEmptySubtitleManejo.setText("No hay lectura actual.");
                    break;
                case 2:
                    binding.ivEmptyIconManejo.setImageResource(R.drawable.ic_gavel);
                    binding.tvEmptyTitleManejo.setText("Camino por recorrer");
                    binding.tvEmptySubtitleManejo.setText("Aún no hay lecturas finalizadas.");
                    break;
            }
        }
    }

    private void mostrarDialogoEliminar(BookEn libro) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar Propuesta")
                .setMessage("¿Deseas quitar '" + libro.getTitulo() + "'?")
                .setNegativeButton("CANCELAR", null)
                .setPositiveButton("ELIMINAR", (dialog, which) ->
                        viewModel.eliminarPropuesta(idGrupo, libro.getId(), sharedViewModel))
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}