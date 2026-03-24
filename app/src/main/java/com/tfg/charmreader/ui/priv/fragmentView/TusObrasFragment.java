package com.tfg.charmreader.ui.priv.fragmentView;

import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tfg.charmreader.R;
import com.tfg.charmreader.data.model.Obras;
import com.tfg.charmreader.databinding.FragmentTusObrasBinding;
import com.tfg.charmreader.ui.priv.adapterRecyclerView.ObrasAdapter;
import com.tfg.charmreader.ui.priv.tusLibros.CargarNuevoLibroActivity;
import com.tfg.charmreader.ui.priv.tusObras.CrearObraActivity;
import com.tfg.charmreader.ui.priv.tusObras.EditorObraActivity;
import com.tfg.charmreader.viewmodel.priv.fragmentView.TusObrasFragmentViewModel;

import java.util.ArrayList;

public class TusObrasFragment extends Fragment {

    private FragmentTusObrasBinding binding;
    private TusObrasFragmentViewModel viewModel;
    private ObrasAdapter adapter;
    private Obras obraSeleccionada; // Guarda la obra pulsada para abrirla tras verificar/descargar
    private AlertDialog loadingDialog;

    public TusObrasFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTusObrasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TusObrasFragmentViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupListeners();
    }

    private void setupRecyclerView() {
        binding.recyclerObras.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ObrasAdapter(new ArrayList<>(), obra -> {
            // PASO 1: Al pulsar, guardamos la obra y verificamos si el archivo existe o hay que descargarlo
            this.obraSeleccionada = obra;
            viewModel.verificarYDescargarObra(obra);
        });

        adapter.setOnItemLongClickListener(this::mostrarDialogoEliminar);
        binding.recyclerObras.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getObras().observe(getViewLifecycleOwner(), obras -> {
            boolean vacio = obras == null || obras.isEmpty();
            binding.layoutEmptyTusObras.setVisibility(vacio ? View.VISIBLE : View.GONE);
            binding.recyclerObras.setVisibility(vacio ? View.GONE : View.VISIBLE);
            adapter.setData(obras);
        });

        viewModel.getMensaje().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        // PASO 2: Observador para saber cuando el archivo está listo físicamente en el móvil
        viewModel.getArchivoListoParaOperar().observe(getViewLifecycleOwner(), listo -> {
            if (listo && obraSeleccionada != null) {
                abrirOpcionesObra(obraSeleccionada);
                viewModel.resetArchivoStatus();
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (loading) {
                mostrarLoading();
            } else {
                ocultarLoading();
            }
        });
    }

    private void setupListeners() {
        binding.searchViewObras.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                adapter.filtrar(newText);
                return true;
            }
        });

        binding.fabAddObra.setOnClickListener(v -> {
            startActivityForResult(new Intent(getActivity(), CrearObraActivity.class), 200);
        });

        binding.chipGroupFiltersObras.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.chipSinFinalizarObras.getId()) {
                adapter.filtrarPorEstado("SIN_FINALIZAR");
            } else if (checkedId == binding.chipFinalizadosObras.getId()) {
                adapter.filtrarPorEstado("FINALIZADOS");
            } else {
                adapter.filtrarPorEstado("TODOS");
            }
        });
    }

    private void abrirOpcionesObra(Obras obra) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_opciones_obra, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Vinculación de elementos del diálogo
        ImageView btnClose = dialogView.findViewById(R.id.btnCloseDialogObra);
        TextView tvTitulo = dialogView.findViewById(R.id.tvTituloDialogObra);
        MaterialButton btnContinuar = dialogView.findViewById(R.id.btnContinuarEscribiendo);
        MaterialButton btnSincronizar = dialogView.findViewById(R.id.btnSincronizarNube); // Nuevo botón
        MaterialButton btnMetadatos = dialogView.findViewById(R.id.btnModificarMetadatos);
        MaterialButton btnEstado = dialogView.findViewById(R.id.btnCambiarEstado);
        MaterialButton btnDescargar = dialogView.findViewById(R.id.btnDescargarLibro);
        MaterialButton btnPublicar = dialogView.findViewById(R.id.btnPublicarLibro);
        ImageView ivPortada = dialogView.findViewById(R.id.ivPortadaDialogObra);

        tvTitulo.setText(obra.getNombre());

        // Lógica de portada
        if (obra.getUrl_imagen() != null && !obra.getUrl_imagen().trim().isEmpty()) {
            ivPortada.setImageTintList(null);
            ivPortada.setPadding(0, 0, 0, 0);
            Glide.with(requireContext()).load(obra.getUrl_imagen()).circleCrop().into(ivPortada);
        }

        // Estado del libro
        boolean estaFinalizado = obra.getFinalizado();
        btnEstado.setText(estaFinalizado ? "Volver a borrador" : "Marcar como finalizado");
        btnEstado.setIconResource(estaFinalizado ? R.drawable.ic_close : R.drawable.ic_check_circle);

        // --- Listeners de los botones del diálogo ---

        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnContinuar.setOnClickListener(v -> {
            dialog.dismiss();
            irAEditor(obra);
        });

        btnSincronizar.setOnClickListener(v -> {
            dialog.dismiss();
            // Esto bajará la versión de Cloudinary y machacará la local
            viewModel.sincronizarObraDesdeNube(obra);
        });

        btnMetadatos.setOnClickListener(v -> {
            dialog.dismiss();
            abrirModificarMetadatos(obra);
        });

        btnEstado.setOnClickListener(v -> {
            dialog.dismiss();
            viewModel.cambiarEstadoObra(obra, !estaFinalizado);
        });

        btnDescargar.setOnClickListener(v -> {
            dialog.dismiss();
            exportarArchivoLocal(obra);
        });

        btnPublicar.setOnClickListener(v -> {
            dialog.dismiss();
            if (obra.getFinalizado()) {
                confirmarPublicacion(obra);
            } else {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Obra en borrador")
                        .setMessage("¿Deseas publicar esta obra en la comunidad aunque sea un borrador?")
                        .setNegativeButton("CANCELAR", null)
                        .setPositiveButton("PUBLICAR", (d, w) -> confirmarPublicacion(obra))
                        .show();
            }
        });

        dialog.show();
    }

    private void irAEditor(Obras obra) {
        Intent intent = new Intent(getActivity(), EditorObraActivity.class);
        intent.putExtra("OBRA", obra);
        intent.putExtra("RUTA_OBRA", obra.getRuta());
        startActivity(intent);
    }

    private void abrirModificarMetadatos(Obras obra) {
        Intent intent = new Intent(getActivity(), CrearObraActivity.class);
        intent.putExtra("MODIFICAR", true);
        intent.putExtra("OBRA", obra);
        startActivity(intent);
    }

    private void exportarArchivoLocal(Obras obra) {
        if (obra.getFinalizado()) {
            viewModel.descargarObra(obra);
        } else {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Obra en borrador")
                    .setMessage("¿Quieres descargar el archivo .epub de este borrador?")
                    .setNegativeButton("CANCELAR", null)
                    .setPositiveButton("DESCARGAR", (d, w) -> viewModel.descargarObra(obra))
                    .show();
        }
    }

    private void confirmarPublicacion(Obras obra) {
        Intent intent = new Intent(requireContext(), CargarNuevoLibroActivity.class);
        intent.putExtra("urlObra", obra.getRuta());
        intent.putExtra("grupo", false);
        intent.putExtra("obra", obra);
        startActivity(intent);
    }

    private void mostrarDialogoEliminar(Obras obra) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar obra")
                .setMessage("¿Estás seguro de eliminar '" + obra.getNombre() + "' de forma permanente?")
                .setNegativeButton("CANCELAR", null)
                .setPositiveButton("ELIMINAR", (d, w) -> viewModel.eliminarObra(obra.getId()))
                .show();
    }

    private void mostrarLoading() {
        if (loadingDialog == null) {
            loadingDialog = new MaterialAlertDialogBuilder(requireContext())
                    .setView(R.layout.layout_loading_dialog) // Asegúrate de tener este layout o usa uno genérico
                    .setCancelable(false)
                    .create();
            loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        loadingDialog.show();
    }

    private void ocultarLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.cargarObras();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}