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

import com.google.android.material.tabs.TabLayout;
import com.tfg.charmreader.R;
import com.tfg.charmreader.data.model.GrupoLectura;
import com.tfg.charmreader.data.model.Usuario;
import com.tfg.charmreader.databinding.FragmentExplorarBinding;
import com.tfg.charmreader.ui.autentication.perfil.publico.PerfilPublicoActivity;
import com.tfg.charmreader.ui.publ.adapterReclyclerView.GrupoLecturaAdapter;
import com.tfg.charmreader.ui.publ.adapterReclyclerView.UsuarioAdapter;
import com.tfg.charmreader.ui.publ.explorar.InfoGrupoPublicaActivity;
import com.tfg.charmreader.viewmodel.publ.fragmentView.ExplorarFragmentViewModel;

import java.util.ArrayList;
import java.util.List;

public class ExplorarFragment extends Fragment {

    private FragmentExplorarBinding binding;
    private ExplorarFragmentViewModel viewModel;

    private GrupoLecturaAdapter grupoAdapter;
    private UsuarioAdapter usuarioAdapter;

    private List<GrupoLectura> cacheGrupos = new ArrayList<>();
    private List<Usuario> cacheUsuarios = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentExplorarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ExplorarFragmentViewModel.class);

        setupTabs();
        setupRecyclerViews();
        setupObservers();
        setupListeners();

        viewModel.cargarDatosExplorar();
    }

    private void setupTabs() {
        if (binding.tabLayoutExplorar.getTabCount() == 0) {
            binding.tabLayoutExplorar.addTab(binding.tabLayoutExplorar.newTab().setText("Grupos"));
            binding.tabLayoutExplorar.addTab(binding.tabLayoutExplorar.newTab().setText("Usuarios"));
        }

        binding.tabLayoutExplorar.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                gestionarCambioTab(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerViews() {
        binding.rvExplorar.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializar Adaptador de Grupos
        grupoAdapter = new GrupoLecturaAdapter(new ArrayList<>(), grupo -> {
            Intent intent = new Intent(getActivity(), InfoGrupoPublicaActivity.class);
            intent.putExtra("objetoGrupo", grupo);
            startActivity(intent);
        });

        usuarioAdapter = new UsuarioAdapter(new ArrayList<>(), usuario -> {
            // Solo permitimos navegar si el usuario es público (opcional, según tu lógica de adapter)
            Intent intent = new Intent(getActivity(), PerfilPublicoActivity.class);
            intent.putExtra("objetoUsuario", usuario); // Pasamos el usuario a la actividad
            startActivity(intent);
        });

        // Por defecto, mostrar grupos
        binding.rvExplorar.setAdapter(grupoAdapter);
    }

    private void setupObservers() {
        // Observar Grupos
        viewModel.getGrupos().observe(getViewLifecycleOwner(), lista -> {
            cacheGrupos = (lista != null) ? lista : new ArrayList<>();
            if (binding.tabLayoutExplorar.getSelectedTabPosition() == 0) {
                grupoAdapter.setGrupoLectura(cacheGrupos);
                actualizarEstadoVacio(cacheGrupos.isEmpty());
            }
        });

        // Observar Usuarios
        viewModel.getUsuariosPublicos().observe(getViewLifecycleOwner(), lista -> {
            cacheUsuarios = (lista != null) ? lista : new ArrayList<>();
            if (binding.tabLayoutExplorar.getSelectedTabPosition() == 1) {
                usuarioAdapter.setUsuarios(cacheUsuarios);
                actualizarEstadoVacio(cacheUsuarios.isEmpty());
            }
        });

        // Error y Navegación desde búsqueda directa
        viewModel.getGrupoEncontrado().observe(getViewLifecycleOwner(), grupo -> {
            if (grupo != null) {
                Intent intent = new Intent(getActivity(), InfoGrupoPublicaActivity.class);
                intent.putExtra("objetoGrupo", grupo);
                startActivity(intent);
            }
        });

        viewModel.getMensajeError().observe(getViewLifecycleOwner(), msg -> mostrarAlerta("Aviso", msg));
    }

    private void setupListeners() {
        binding.searchViewExplorar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.isEmpty()) return false;
                if (binding.tabLayoutExplorar.getSelectedTabPosition() == 0) {
                    viewModel.buscarGrupoPorNombre(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (binding.tabLayoutExplorar.getSelectedTabPosition() == 0) {
                    grupoAdapter.filtrar(newText);
                    actualizarEstadoVacio(false); // El propio adapter filtra, si queda vacío lo manejamos
                } else {
                    usuarioAdapter.filtrar(newText);
                }
                return true;
            }
        });
    }

    private void gestionarCambioTab(int posicion) {
        if (posicion == 0) {
            binding.rvExplorar.setAdapter(grupoAdapter);
            grupoAdapter.setGrupoLectura(cacheGrupos);
            actualizarEstadoVacio(cacheGrupos.isEmpty());
            binding.searchViewExplorar.setQueryHint("Busca grupos...");
        } else {
            binding.rvExplorar.setAdapter(usuarioAdapter);
            usuarioAdapter.setUsuarios(cacheUsuarios);
            actualizarEstadoVacio(cacheUsuarios.isEmpty());
            binding.searchViewExplorar.setQueryHint("Busca usuarios...");
        }
    }

    private void actualizarEstadoVacio(boolean vacio) {
        binding.layoutEmptyExplorar.setVisibility(vacio ? View.VISIBLE : View.GONE);
        binding.rvExplorar.setVisibility(vacio ? View.GONE : View.VISIBLE);

        if (vacio) {
            boolean esGrupos = binding.tabLayoutExplorar.getSelectedTabPosition() == 0;
            binding.ivEmptyIconExplorar.setImageResource(esGrupos ? R.drawable.ic_groups : R.drawable.ic_person);
            binding.tvEmptyTitleExplorar.setText(esGrupos ? "No hay grupos" : "No hay usuarios públicos");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.cargarDatosExplorar();
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