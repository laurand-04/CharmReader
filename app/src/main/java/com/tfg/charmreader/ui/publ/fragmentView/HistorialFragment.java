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
import com.tfg.charmreader.data.model.BookEn;
import com.tfg.charmreader.data.model.CatalogoLectura;
import com.tfg.charmreader.data.model.GrupoLectura;
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

        binding.rvHistorial.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getActivity() != null && getActivity().getIntent().hasExtra("objetoGrupo")) {
            grupo = (GrupoLectura) getActivity().getIntent().getSerializableExtra("objetoGrupo");
            setupObservers();
            viewModel.cargarHistorial(grupo.getIdGrupo());
        }
    }

    private void setupObservers() {
        // Observamos el estado vacío
        viewModel.getIsEmpty().observe(getViewLifecycleOwner(), estaVacio -> {
            binding.layoutEmptyHistorial.setVisibility(estaVacio ? View.VISIBLE : View.GONE);
            binding.rvHistorial.setVisibility(estaVacio ? View.GONE : View.VISIBLE);
        });

        // Observamos los datos (el ViewModel garantiza que se cargan juntos)
        viewModel.getLibros().observe(getViewLifecycleOwner(), listaLibros -> {
            List<CatalogoLectura> listaFechas = viewModel.getCatalogo().getValue();
            if (listaLibros != null && listaFechas != null) {
                configurarAdapter(listaLibros, listaFechas);
            }
        });

        // Opcional: ProgressBar
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            // binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });
    }

    private void configurarAdapter(List<BookEn> listaLibros, List<CatalogoLectura> listaFechas) {
        LibroHistorialAdapter adapter = new LibroHistorialAdapter(
                listaLibros,
                listaFechas,
                libro -> {
                    Intent intent = new Intent(getContext(), ValoracionesLibroActivity.class);
                    intent.putExtra("idLibro", libro.getId());
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