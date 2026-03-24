package com.tfg.charmreader.ui.autentication.perfil.publico;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.tfg.charmreader.data.model.Libro;
import com.tfg.charmreader.data.model.Obras;
import com.tfg.charmreader.databinding.FragmentObrasPublicadasBinding;
import com.tfg.charmreader.ui.priv.adapterRecyclerView.LibrosAdapter;
import com.tfg.charmreader.ui.priv.adapterRecyclerView.ObrasAdapter;
import com.tfg.charmreader.viewmodel.autentication.perfil.publico.PerfilPublicoViewModel;

import java.util.ArrayList;

public class ObrasPublicadasFragment extends Fragment {

    private FragmentObrasPublicadasBinding binding;
    private PerfilPublicoViewModel viewModel;
    private int idUsuario;
    private LibrosAdapter adapter;

    /*public ObrasPublicadasFragment(int idUsuario) {
        this.idUsuario = idUsuario;
    }*/
    // Borra el constructor con parámetros y usa esto:
    public static ObrasPublicadasFragment newInstance(int idUsuario) {
        ObrasPublicadasFragment fragment = new ObrasPublicadasFragment();
        Bundle args = new Bundle();
        args.putInt("ID_USUARIO", idUsuario);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.idUsuario = getArguments().getInt("ID_USUARIO");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentObrasPublicadasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Usamos el mismo ViewModel que el fragmento anterior (comparten la actividad)
        viewModel = new ViewModelProvider(requireActivity()).get(PerfilPublicoViewModel.class);

        setupRecyclerView();
        setupObservers();
        android.util.Log.d("PERFIL_PUBLICO", "filtro 2");


        viewModel.cargarObrasPublicadas(idUsuario);
    }

    private void setupRecyclerView() {
        android.util.Log.d("PERFIL_PUBLICO", "filtro 1");
        binding.rvObrasPublicadas.setLayoutManager(new LinearLayoutManager(getContext()));

        // Usamos LibrosAdapter. Pasamos un listener para el detalle.
        adapter = new LibrosAdapter(new ArrayList<>(), libro -> {
            irADetalleLibroPublico(libro);
        });

        binding.rvObrasPublicadas.setAdapter(adapter);
    }

    private void setupObservers() {
        // Observamos el nuevo LiveData de detalles
        viewModel.getDetallesObrasPublicadas().observe(getViewLifecycleOwner(), libros -> {
            if (libros != null && !libros.isEmpty()) {
                // Usamos setData con lista vacía de relaciones (sin badges)
                adapter.setData(libros, new ArrayList<>());
                binding.tvEmptyObrasPublicas.setVisibility(View.GONE);
                binding.rvObrasPublicadas.setVisibility(View.VISIBLE);
            } else {
                binding.tvEmptyObrasPublicas.setVisibility(View.VISIBLE);
                binding.rvObrasPublicadas.setVisibility(View.GONE);
            }
        });
    }

    private void irADetalleLibroPublico(Libro libro) {
        android.util.Log.d("PERFIL_PUBLICO", "Navegando a detalle de: " + libro.getNombre() + " --- " + libro.getId());

        Intent intent = new Intent(getActivity(), DetalleObraActivity.class);
        // Asegúrate de que tu clase Libro implemente Serializable o Parcelable
        intent.putExtra("Libro", libro);
        intent.putExtra("idusuario", idUsuario);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}