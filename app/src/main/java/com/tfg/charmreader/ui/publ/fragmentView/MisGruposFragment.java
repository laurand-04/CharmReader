package com.tfg.charmreader.ui.publ.fragmentView;

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
import com.google.android.material.tabs.TabLayout;
import com.tfg.charmreader.R;
import com.tfg.charmreader.data.model.GrupoLectura;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import com.tfg.charmreader.databinding.FragmentMisGruposBinding;
import com.tfg.charmreader.ui.publ.adapterReclyclerView.GrupoLecturaAdapter;
import com.tfg.charmreader.ui.publ.misGrupos.creados.ManejoGrupoActivity;
import com.tfg.charmreader.ui.publ.misGrupos.suscritos.InfoGrupoPrivadaActivity;
import com.tfg.charmreader.viewmodel.publ.fragmentView.MisGruposFragmentViewModel;

import java.util.ArrayList;
import java.util.List;

public class MisGruposFragment extends Fragment {

    private FragmentMisGruposBinding binding;
    private MisGruposFragmentViewModel viewModel;
    private GrupoLecturaAdapter adapter;
    private int idUsuario;
    private List<GrupoLectura> listaCompletaActual = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMisGruposBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        idUsuario = AuthRepository.getInstance(getContext()).getIdUsuario();
        viewModel = new ViewModelProvider(this).get(MisGruposFragmentViewModel.class);

        setupRecyclerView();
        setupTabs();
        setupSearchView();
        setupObservers();

        if (idUsuario > 0) viewModel.cargarDatos(idUsuario);
    }

    private void setupRecyclerView() {
        binding.rvMisGrupos.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GrupoLecturaAdapter(new ArrayList<>(), grupo -> {
            Class<?> destino = (binding.tabLayoutMisGrupos.getSelectedTabPosition() == 0)
                    ? InfoGrupoPrivadaActivity.class
                    : ManejoGrupoActivity.class;
            Intent intent = new Intent(getActivity(), destino);
            intent.putExtra("objetoGrupo", grupo);
            startActivity(intent);
        });

        adapter.setOnItemLongClickListener(this::mostrarDialogoGestionAdmin);
        binding.rvMisGrupos.setAdapter(adapter);
    }

    private void setupObservers() {
        // Observar suscritos
        viewModel.getListaSuscritos().observe(getViewLifecycleOwner(), grupos -> {
            if (binding.tabLayoutMisGrupos.getSelectedTabPosition() == 0) {
                actualizarListaUI(grupos);
            }
        });

        // Observar creados
        viewModel.getListaCreados().observe(getViewLifecycleOwner(), grupos -> {
            if (binding.tabLayoutMisGrupos.getSelectedTabPosition() == 1) {
                actualizarListaUI(grupos);
            }
        });

        // Observar Toasts (mensajes de éxito/error)
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupTabs() {

        if (binding.tabLayoutMisGrupos.getTabCount() == 0) {
            binding.tabLayoutMisGrupos.addTab(
                    binding.tabLayoutMisGrupos.newTab().setText("Suscritos")
            );
            binding.tabLayoutMisGrupos.addTab(
                    binding.tabLayoutMisGrupos.newTab().setText("Creados")
            );
        }

        binding.tabLayoutMisGrupos.addOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        List<GrupoLectura> lista = (tab.getPosition() == 0)
                                ? viewModel.getListaSuscritos().getValue()
                                : viewModel.getListaCreados().getValue();
                        actualizarListaUI(lista);
                    }
                    @Override public void onTabUnselected(TabLayout.Tab tab) {}
                    @Override public void onTabReselected(TabLayout.Tab tab) {}
                }
        );
    }

    private void actualizarListaUI(List<GrupoLectura> lista) {
        listaCompletaActual = (lista != null) ? lista : new ArrayList<>();
        adapter.setGrupoLectura(listaCompletaActual);
        actualizarEstadoVacio(listaCompletaActual.isEmpty());
    }

    private void actualizarEstadoVacio(boolean estaVacio) {
        binding.layoutEmptyMisGrupos.setVisibility(estaVacio ? View.VISIBLE : View.GONE);
        binding.rvMisGrupos.setVisibility(estaVacio ? View.GONE : View.VISIBLE);
        if (estaVacio) {
            boolean esSuscritos = binding.tabLayoutMisGrupos.getSelectedTabPosition() == 0;
            binding.ivEmptyIconMisGrupos.setImageResource(esSuscritos ? R.drawable.ic_people : R.drawable.ic_libro);
            binding.tvEmptyTitleMisGrupos.setText(esSuscritos ? "¡Estás solo por aquí!" : "¿Tienes una idea?");
            binding.tvEmptySubtitleMisGrupos.setText(esSuscritos ? "Aún no te has unido a ningún grupo." : "Aún no has creado ningún grupo.");
        }
    }

    private void setupSearchView() {
        binding.searchViewMisGrupos.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                filtrar(newText);
                return true;
            }
        });
    }

    private void filtrar(String texto) {
        List<GrupoLectura> filtrada = new ArrayList<>();
        for (GrupoLectura g : listaCompletaActual) {
            if (g.getNombre().toLowerCase().contains(texto.toLowerCase())) {
                filtrada.add(g);
            }
        }
        adapter.setGrupoLectura(filtrada);
        actualizarEstadoVacio(filtrada.isEmpty());
    }

    private void mostrarDialogoGestionAdmin(GrupoLectura grupo) {
        if (binding.tabLayoutMisGrupos.getSelectedTabPosition() == 1) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Abandonar Grupo")
                    .setMessage("¿Deseas dejar de ser administrador de '" + grupo.getNombre() + "'?")
                    .setNegativeButton("CANCELAR", null)
                    .setPositiveButton("ACEPTAR", (d, w) -> viewModel.gestionarSalidaAdmin(grupo.getIdGrupo(), idUsuario))
                    .show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (idUsuario > 0) viewModel.cargarDatos(idUsuario);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}