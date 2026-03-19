package com.tfg.charmreader.ui.priv.fragmentView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

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
            // Acción al hacer click normal (Abrir la obra para leer/editar)
            abrirEditor(obra);
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

        binding.fabAddObra.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CrearObraActivity.class);
            startActivityForResult(intent, 200);
        });

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

    private void mostrarDialogoEliminar(Obras obra) {
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

    private void abrirEditor(Obras obra) {
        // 1. Inflamos nuestra vista personalizada
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_opciones_obra, null);

        // 2. Creamos el AlertDialog y le asignamos la vista
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Para que las esquinas redondeadas de nuestro CardView se vean bien,
        // el fondo del diálogo por defecto tiene que ser transparente
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // 3. Vinculamos los elementos de la vista
        ImageView btnClose = dialogView.findViewById(R.id.btnCloseDialogObra);
        TextView tvTitulo = dialogView.findViewById(R.id.tvTituloDialogObra);
        MaterialButton btnContinuar = dialogView.findViewById(R.id.btnContinuarEscribiendo);
        MaterialButton btnMetadatos = dialogView.findViewById(R.id.btnModificarMetadatos);
        MaterialButton btnEstado = dialogView.findViewById(R.id.btnCambiarEstado);
        MaterialButton btnDescargar = dialogView.findViewById(R.id.btnDescargarLibro);
        MaterialButton btnPublicar = dialogView.findViewById(R.id.btnPublicarLibro);
        ImageView ivPortada = dialogView.findViewById(R.id.ivPortadaDialogObra);

        // 4. Llenamos los datos dinámicos
        tvTitulo.setText(obra.getNombre() != null ? obra.getNombre() : "Obra sin título");

        // --- LÓGICA DE LA PORTADA ---
        String urlPortada = obra.getUrl_imagen();

        if (urlPortada != null && !urlPortada.trim().isEmpty()) {
            // Si hay portada:
            // 1. Quitamos el tinte y el padding (para que la imagen ocupe tod el círculo)
            ivPortada.setImageTintList(null);
            ivPortada.setPadding(0, 0, 0, 0);
            // 2. Cargamos la imagen con Glide, recortándola en círculo
            Glide.with(requireContext())
                    .load(urlPortada)
                    .circleCrop() // Hace que la imagen encaje en el círculo
                    .into(ivPortada);
        } else {
            // Si NO hay portada (estado por defecto):
            // Restauramos el aspecto visual del icono genérico
            ivPortada.setImageResource(R.drawable.ic_libro);
            // Si usas ContextCompat para el color:
            // ivPortada.setColorFilter(ContextCompat.getColor(requireContext(), R.color.basico));
        }
        // -----------------------------

        // Adaptamos el texto y color del botón de estado según si está finalizado o no
        // Suponiendo que usas el estado como String. Si es booleano, cambia a: obra.isFinalizado()
        boolean estaFinalizado = obra.getFinalizado();

        if (estaFinalizado) {
            btnEstado.setText("Volver a borrador");
            btnEstado.setIconResource(R.drawable.ic_close); // Pon un icono adecuado
            btnEstado.setIconTintResource(android.R.color.holo_red_light);
        } else {
            btnEstado.setText("Marcar como finalizado");
            btnEstado.setIconResource(R.drawable.ic_check_circle);
            btnEstado.setIconTintResource(R.color.basico); // O el color verde que tenías (#4CAF50)
        }

        // 5. Asignamos los clics
        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnContinuar.setOnClickListener(v -> {
            dialog.dismiss();
            irAEditor(obra);
        });

        btnMetadatos.setOnClickListener(v -> {
            dialog.dismiss();
            abrirModificarMetadatos(obra);
        });

        btnEstado.setOnClickListener(v -> {
            dialog.dismiss();
            cambiarEstadoObra(obra, !estaFinalizado);
        });

        btnDescargar.setOnClickListener(v -> {
            dialog.dismiss();
            descargarObraLocal(obra);
        });

        btnPublicar.setOnClickListener(v -> {
            dialog.dismiss(); // Cerramos el menú de opciones

            // Verificamos si la obra está marcada como finalizada
            if (obra.getFinalizado()) {
                // Si está finalizada, procedemos directamente
                confirmarPublicacion(obra);
            } else {
                // SI NO ESTÁ FINALIZADA: Mostramos el aviso de advertencia
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Obra en borrador")
                        .setMessage("Esta obra aún no ha sido marcada como finalizada. ¿Estás seguro de que quieres publicarla en la comunidad tal como está?")
                        .setNegativeButton("CANCELAR", null)
                        .setPositiveButton("PUBLICAR DE TODAS FORMAS", (d, w) -> {
                            confirmarPublicacion(obra);
                        })
                        .show();
            }
        });

        // 6. Mostramos el diálogo
        dialog.show();
    }

    // --- MÉTODOS DE ACCIÓN ---

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

    private void cambiarEstadoObra(Obras obra, boolean marcarComoFinalizado) {
        viewModel.cambiarEstadoObra(obra, marcarComoFinalizado);
    }

    private void descargarObraLocal(Obras obra) {
        // 1. Verificación de seguridad de la ruta
        if (obra.getRuta() == null || obra.getRuta().isEmpty()) {
            Toast.makeText(getContext(), "No se encuentra el archivo local de esta obra", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Comprobamos si la obra está finalizada
        // (Usamos el booleano 'getFinalizado' que ya corregimos antes)
        if (obra.getFinalizado()) {
            // Si está finalizada, descargamos sin preguntar
            viewModel.descargarObra(obra);
        } else {
            // 3. Si es un borrador, mostramos el aviso de advertencia
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Obra en borrador")
                    .setMessage("Esta obra aún no ha sido marcada como finalizada. ¿Quieres descargar el archivo de todas formas?")
                    .setNegativeButton("CANCELAR", null)
                    .setPositiveButton("DESCARGAR", (dialog, which) -> {
                        // Si el usuario acepta, procedemos con la descarga
                        viewModel.descargarObra(obra);
                    })
                    .show();
        }
    }

    //TODO
    private void confirmarPublicacion(Obras obra) {
        Intent intent = new Intent(requireContext(), CargarNuevoLibroActivity.class);
        intent.putExtra("urlObra", obra.getRuta());
        intent.putExtra("grupo", false);
        intent.putExtra("obra", obra);
        startActivity(intent);
    }

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