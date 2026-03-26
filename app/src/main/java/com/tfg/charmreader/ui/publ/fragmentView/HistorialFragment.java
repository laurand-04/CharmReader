package com.tfg.charmreader.ui.publ.fragmentView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.tfg.charmreader.data.model.GrupoLectura;
import com.tfg.charmreader.data.pojo.LibroHistorialUI;
import com.tfg.charmreader.databinding.FragmentHistorialBinding;
import com.tfg.charmreader.ui.publ.adapterReclyclerView.LibroHistorialAdapter;
import com.tfg.charmreader.ui.publ.misGrupos.suscritos.ValoracionesLibroActivity;
import com.tfg.charmreader.viewmodel.publ.fragmentView.HistorialFragmentViewModel;

import java.util.List;

public class HistorialFragment extends Fragment {

    private FragmentHistorialBinding binding;
    private HistorialFragmentViewModel viewModel;
    private GrupoLectura grupo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistorialBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HistorialFragmentViewModel.class);

        // Configuración básica del RecyclerView
        binding.rvHistorial.setLayoutManager(new LinearLayoutManager(getContext()));

        // Recuperar objeto grupo del Intent de la Activity contenedora
        if (getActivity() != null && getActivity().getIntent().hasExtra("objetoGrupo")) {
            grupo = (GrupoLectura) getActivity().getIntent().getSerializableExtra("objetoGrupo");
            setupObservers();
            viewModel.cargarHistorial(grupo.getIdGrupo());
        }
    }

    private void setupObservers() {
        // Observamos si la lista está vacía para mostrar el layout de aviso
        viewModel.getIsEmpty().observe(getViewLifecycleOwner(), estaVacio -> {
            binding.layoutEmptyHistorial.setVisibility(estaVacio ? View.VISIBLE : View.GONE);
            binding.rvHistorial.setVisibility(estaVacio ? View.GONE : View.VISIBLE);
        });

        // Observamos la lista única de objetos UI (Libro + Fecha + Valoración)
        viewModel.getLibrosHistorial().observe(getViewLifecycleOwner(), listaUI -> {
            if (listaUI != null) {
                configurarAdapter(listaUI);
            }
        });

        // Observamos el estado de carga (opcional, por si quieres mostrar un Spinner)
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            // Si añades un ProgressBar al XML, contrólalo aquí:
            // binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });
    }

    private void configurarAdapter(List<LibroHistorialUI> listaUI) {
        LibroHistorialAdapter adapter = new LibroHistorialAdapter(
                listaUI,
                itemUI -> {
                    // Al hacer click, enviamos los datos a la actividad de valoraciones
                    Intent intent = new Intent(getContext(), ValoracionesLibroActivity.class);
                    intent.putExtra("idLibro", itemUI.getLibro().getId());
                    intent.putExtra("idGrupo", grupo.getIdGrupo());
                    startActivity(intent);
                }
        );
        binding.rvHistorial.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}