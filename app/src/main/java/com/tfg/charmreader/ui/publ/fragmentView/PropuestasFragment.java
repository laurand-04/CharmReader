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

import com.tfg.charmreader.data.model.BookEn;
import com.tfg.charmreader.data.model.GrupoLectura;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import com.tfg.charmreader.databinding.FragmentPropuestasBinding;
import com.tfg.charmreader.ui.publ.adapterReclyclerView.LibroPropuestoAdapter;
import com.tfg.charmreader.ui.publ.misGrupos.suscritos.LibroActualActivity;
import com.tfg.charmreader.viewmodel.publ.fragmentView.PropuestasFragmentViewModel;

import java.util.ArrayList;

public class PropuestasFragment extends Fragment {

    private FragmentPropuestasBinding binding;
    private PropuestasFragmentViewModel viewModel;
    private LibroPropuestoAdapter adapter;
    private GrupoLectura grupo;
    private int idUsuarioLogueado;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPropuestasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        idUsuarioLogueado = AuthRepository.getInstance(requireContext()).getIdUsuario();
        viewModel = new ViewModelProvider(this).get(PropuestasFragmentViewModel.class);

        if (getActivity() != null && getActivity().getIntent().hasExtra("objetoGrupo")) {
            grupo = (GrupoLectura) getActivity().getIntent().getSerializableExtra("objetoGrupo");
        }

        setupRecyclerView();
        setupObservers();

        if (idUsuarioLogueado != -1 && grupo != null) {
            viewModel.cargarDatos(grupo.getIdGrupo());
        } else {
            mostrarEstadoVacio(true);
        }
    }

    private void setupRecyclerView() {
        binding.rvPropuestas.setLayoutManager(new LinearLayoutManager(getContext()));
        // Iniciamos con valores por defecto; el observer actualizará el adapter
        adapter = new LibroPropuestoAdapter(new ArrayList<>(), 0, new LibroPropuestoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BookEn libro) {
                Intent i = new Intent(getContext(), LibroActualActivity.class);
                i.putExtra("libroSeleccionado", libro);
                startActivity(i);
            }

            @Override
            public void onVotarClick(BookEn libro) {
                viewModel.ejecutarVoto(idUsuarioLogueado, grupo.getIdGrupo(), libro.getId());
            }
        });

        // Pasamos la API necesaria al adapter (esto se hereda de tu lógica original)
        adapter.setVotacionApi(viewModel.getApiVotacion(), idUsuarioLogueado, (grupo != null ? grupo.getIdGrupo() : -1));
        binding.rvPropuestas.setAdapter(adapter);
    }

    private void setupObservers() {
        // Observar cambios en la lista de libros
        viewModel.getListaLibros().observe(getViewLifecycleOwner(), libros -> {
            Integer total = viewModel.getTotalMiembros().getValue();
            adapter.updateData(libros, total != null ? total : 0);
            mostrarEstadoVacio(libros.isEmpty());
        });

        // Observar mensajes de feedback
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void mostrarEstadoVacio(boolean estaVacio) {
        binding.layoutEmptyPropuestas.setVisibility(estaVacio ? View.VISIBLE : View.GONE);
        binding.rvPropuestas.setVisibility(estaVacio ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}