package com.tfg.charmreader.ui.autentication.perfil.publico;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.tfg.charmreader.databinding.FragmentUltimasLecturasBinding;
import com.tfg.charmreader.ui.priv.adapterRecyclerView.LibrosAdapter;
import com.tfg.charmreader.viewmodel.autentication.perfil.publico.PerfilPublicoViewModel;

import java.util.ArrayList;

public class UltimasLecturasFragment extends Fragment {

    private FragmentUltimasLecturasBinding binding;
    private PerfilPublicoViewModel viewModel;
    private int idUsuario;
    private LibrosAdapter adapter;

    public UltimasLecturasFragment(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUltimasLecturasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Usamos el ViewModel compartido
        viewModel = new ViewModelProvider(this).get(PerfilPublicoViewModel.class);

        setupRecyclerView();
        setupObservers();

        viewModel.cargarUltimasLecturas(idUsuario);
    }

    private void setupRecyclerView() {
        binding.rvUltimasLecturas.setLayoutManager(new LinearLayoutManager(getContext()));
        // Listener null para que no sea interactivo
        adapter = new LibrosAdapter(new ArrayList<>(), null);
        binding.rvUltimasLecturas.setAdapter(adapter);
    }

    private void setupObservers() {
        // Observamos directamente los detalles de los libros (la lista final)
        viewModel.getDetallesUltimasLecturas().observe(getViewLifecycleOwner(), libros -> {
            if (libros != null && !libros.isEmpty()) {
                // Usamos setData con una lista vacía de relaciones ya que no queremos badges en perfil ajeno
                adapter.setData(libros, new ArrayList<>());
                binding.tvEmptyLecturas.setVisibility(View.GONE);
                binding.rvUltimasLecturas.setVisibility(View.VISIBLE);
            } else {
                binding.tvEmptyLecturas.setVisibility(View.VISIBLE);
                binding.rvUltimasLecturas.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}