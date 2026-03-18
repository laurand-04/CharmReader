package com.tfg.charmreader.ui.priv.fragmentView;

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
import com.tfg.charmreader.R;
import com.tfg.charmreader.data.model.ObrasModel;
import com.tfg.charmreader.databinding.FragmentTusObrasBinding;
import com.tfg.charmreader.ui.priv.adapterRecyclerView.ObrasAdapter;
import com.tfg.charmreader.viewmodel.priv.fragmentView.TusObrasFragmentViewModel;

import java.util.ArrayList;

public class TusObrasFragment extends Fragment {

    private FragmentTusObrasBinding binding;
    private TusObrasFragmentViewModel viewModel;
    private ObrasAdapter adapter;

    // Variable para controlar el estado del filtro de "Recientes"
    private boolean ordenRecientesActivado = false;

    public TusObrasFragment() {
        // Constructor público vacío requerido por Android
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Utilizamos el ViewBinding generado a partir de fragment_tus_obras.xml
        binding = FragmentTusObrasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializamos el ViewModel (asegúrate de crear esta clase después)
        viewModel = new ViewModelProvider(this).get(TusObrasFragmentViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupListeners();
    }

    private void setupRecyclerView() {
        binding.recyclerObras.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializamos el adaptador
        adapter = new ObrasAdapter(new ArrayList<>(), obra -> {
            // Acción al hacer click normal (Abrir la obra para leer/editar) TODO
            //abrirEditor(obra);
        });

        // Acción al mantener pulsado (Eliminar)
        adapter.setOnItemLongClickListener(this::mostrarDialogoEliminar);

        binding.recyclerObras.setAdapter(adapter);
    }

    private void setupObservers() {
        // Observamos la lista de obras desde el ViewModel
        viewModel.getObras().observe(getViewLifecycleOwner(), obras -> {
            boolean vacio = obras == null || obras.isEmpty();

            // Alternamos la visibilidad del "Empty State" y el RecyclerView
            binding.layoutEmptyTusObras.setVisibility(vacio ? View.VISIBLE : View.GONE);
            binding.recyclerObras.setVisibility(vacio ? View.GONE : View.VISIBLE);

            // Actualizamos los datos en el adaptador
            adapter.setData(obras);
        });

        // Observamos los mensajes para mostrar Toasts (errores, confirmaciones, etc.)
        viewModel.getMensaje().observe(getViewLifecycleOwner(), msg ->
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show());
    }

    private void setupListeners() {
        // Buscador por texto
        binding.searchViewObras.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filtrar(newText);
                return true;
            }
        });

        // Botón flotante para crear nueva obra TODO
        /*binding.fabAddObra.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CrearObraActivity.class);
            startActivityForResult(intent, 200);
        });*/

        // Filtros de estado (Radio Buttons estilo Chips)
        binding.chipGroupFiltersObras.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.chipSinFinalizarObras.getId()) {
                adapter.filtrarPorEstado("SIN_FINALIZAR");
            } else if (checkedId == binding.chipFinalizadosObras.getId()) {
                adapter.filtrarPorEstado("FINALIZADOS");
            } else {
                // Por defecto o si selecciona "Todos"
                adapter.filtrarPorEstado("TODOS");
            }
        });
    }

    private void mostrarDialogoEliminar(ObrasModel obra) {
        String tituloObra = obra.getNombre() != null ? obra.getNombre() : "esta obra";

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar obra")
                .setMessage("¿Estás seguro de que deseas eliminar '" + tituloObra + "'?\nEsta acción no se puede deshacer.")
                .setNegativeButton("CANCELAR", null)
                .setPositiveButton("ELIMINAR", (dialog, which) -> {
                    // Llamamos al ViewModel para que elimine la obra en el backend
                    viewModel.eliminarObra(obra.getId());
                })
                .show();
    }
    //TODO
    /*private void abrirEditor(ObrasModel obra) {
        // Redirige a la pantalla donde el usuario escribe o edita su obra
        Intent intent = new Intent(getActivity(), EditorObraActivity.class);
        intent.putExtra("ID_OBRA", obra.getId());
        intent.putExtra("RUTA_OBRA", obra.getRuta());
        startActivity(intent);
    }*/

    @Override
    public void onResume() {
        super.onResume();
        // Recargamos las obras cada vez que el fragmento vuelve a ser visible
        // (por ejemplo, al volver de crear una obra nueva)
        viewModel.cargarObras();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Evitamos fugas de memoria limpiando el binding
        binding = null;
    }
}