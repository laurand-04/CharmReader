package com.tfg.charmreader.ui.priv.estanteria;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tfg.charmreader.R;
import com.tfg.charmreader.databinding.ActivityLibrosEstanteriaBinding;
import com.tfg.charmreader.ui.priv.adapterRecyclerView.LibrosAdapter;
import com.tfg.charmreader.data.model.Libro;
import com.tfg.charmreader.data.model.LibrosDeUsuario;
import com.tfg.charmreader.viewmodel.priv.estanteria.LibrosEstanteriaViewModel;

import java.util.List;

public class LibrosEstanteriaActivity extends AppCompatActivity {
    private ActivityLibrosEstanteriaBinding binding;
    private LibrosEstanteriaViewModel viewModel;
    private LibrosAdapter adapter;
    private int idEstanteria;

    private final ActivityResultLauncher<Intent> launcherCargarLibro = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> { if (result.getResultCode() == RESULT_OK) viewModel.cargarLibrosPorEstanteria(this, idEstanteria); }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLibrosEstanteriaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        idEstanteria = getIntent().getIntExtra("idEstanteria", -1);
        viewModel = new ViewModelProvider(this).get(LibrosEstanteriaViewModel.class);

        aplicarColoresDinamicos();
        setupRecyclerView();
        setupObservers();
        setupListeners();

        viewModel.cargarLibrosPorEstanteria(this, idEstanteria);
    }

    private void setupObservers() {
        viewModel.getLibros().observe(this, libros -> {
            actualizarInterfaz(libros);
            adapter = new LibrosAdapter(libros, this::abrirValoracion);
            adapter.setSoloPendientes(false);
            adapter.setOnItemLongClickListener(this::mostrarDialogoQuitar);
            adapter.setData(libros, viewModel.getRelaciones().getValue());
            binding.recyclerLibrosEstanteria.setAdapter(adapter);
        });

        viewModel.getIsLoading().observe(this, loading -> {
            binding.layoutLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.layoutContenido.setAlpha(loading ? 0.4f : 1.0f);
        });

        viewModel.getMensaje().observe(this, msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.statusColorContainer.setOnClickListener(v -> mostrarDialogoColores());

        // --- FILTROS DE PROGRESO ---
        binding.chipGroupFiltersEst.setOnCheckedChangeListener((group, checkedId) -> {
            if (adapter == null) return;

            if (checkedId == binding.chipSinEmpezarEst.getId()) {
                adapter.filtrarPorEstado("SIN_EMPEZAR");
            } else if (checkedId == binding.chipEmpezadosEst.getId()) {
                adapter.filtrarPorEstado("EMPEZADOS");
            } else if (checkedId == binding.chipTerminadosEst.getId()) {
                adapter.filtrarPorEstado("TERMINADOS");
            } else {
                adapter.filtrarPorEstado("TODOS");
            }
        });

        // --- FILTRO DE AUTOR ---
        binding.chipFilterAutorEst.setOnClickListener(v -> mostrarMenuAutores());

        binding.fabAddLibrosEstanteria.setOnClickListener(v -> {
            Intent intent = new Intent(this, NuevoLibroEstanteriaActivity.class);
            intent.putExtra("idEstanteria", idEstanteria);
            launcherCargarLibro.launch(intent);
        });

        binding.searchViewLibrosEst.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { return false; }
            @Override public boolean onQueryTextChange(String n) {
                if (adapter != null) adapter.filtrar(n);
                return true;
            }
        });

        binding.tvTituloEstanteria.setOnClickListener(v -> mostrarDialogoNombre());
    }

    private void mostrarMenuAutores() {
        if (adapter == null) return;

        List<String> autores = adapter.getListaAutoresUnicos();
        if (autores.isEmpty()) {
            Toast.makeText(this, "No hay autores en esta estantería", Toast.LENGTH_SHORT).show();
            return;
        }

        androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(this, binding.chipFilterAutorEst);

        for (int i = 0; i < autores.size(); i++) {
            popup.getMenu().add(0, i, i, autores.get(i));
        }
        popup.getMenu().add(1, 999, autores.size(), "--- Limpiar Autor ---");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 999) {
                binding.chipFilterAutorEst.setText("Autor");
                adapter.filtrarPorAutor(null);
            } else {
                String seleccionado = item.getTitle().toString();
                binding.chipFilterAutorEst.setText(seleccionado);
                adapter.filtrarPorAutor(seleccionado);
            }
            return true;
        });
        popup.show();
    }

    private void mostrarDialogoColores() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_cambiar_color, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Fondo transparente para que se vean los bordes redondeados del CardView
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        LinearLayout contenedor = dialogView.findViewById(R.id.contenedorColoresDialog);
        MaterialButton btnGuardar = dialogView.findViewById(R.id.btnGuardarColor);
        ImageView btnClose = dialogView.findViewById(R.id.btnCloseDialog);
        ImageView ivPreview = dialogView.findViewById(R.id.ivIconoPreviewDialog);

        // Cerrar al pulsar la X
        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        if (contenedor != null) {
            for (int i = 0; i < contenedor.getChildCount(); i++) {
                View colorCircle = contenedor.getChildAt(i);
                colorCircle.setOnClickListener(v -> {
                    if (v.getTag() != null) {
                        String colorPastel = v.getTag().toString();
                        int fuerte = obtenerColorFuerte(colorPastel);

                        // Actualizar UI de la actividad en tiempo real
                        aplicarColoresDinamicosManual(colorPastel);

                        // Actualizar el icono de preview del propio diálogo
                        if (ivPreview != null) {
                            ivPreview.getBackground().setColorFilter(fuerte, PorterDuff.Mode.SRC_IN);
                            ivPreview.setColorFilter(fuerte);
                        }

                        // Guardar la elección en el tag del botón
                        btnGuardar.setTag(colorPastel);
                        btnGuardar.setBackgroundTintList(ColorStateList.valueOf(fuerte));
                    }
                });
            }
        }

        btnGuardar.setOnClickListener(v -> {
            if (v.getTag() != null) {
                String colorFinal = v.getTag().toString();
                viewModel.actualizarColorEstanteria(this, idEstanteria, colorFinal);
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void aplicarColoresDinamicosManual(String colorPastel) {
        int fuerte = obtenerColorFuerte(colorPastel);
        ColorStateList csl = ColorStateList.valueOf(fuerte);

        binding.viewEstanteriaColor.getBackground().setColorFilter(fuerte, PorterDuff.Mode.SRC_IN);
        binding.statusColorContainer.setStrokeColor(csl);
        binding.btnBack.setStrokeColor(csl);
        binding.tvCantidadLibrosEstanteria.setTextColor(fuerte);
        binding.fabAddLibrosEstanteria.setBackgroundTintList(csl);

        // Pintar los chips de filtro
        binding.chipSinEmpezarEst.setChipIconTint(csl);
        binding.chipEmpezadosEst.setChipIconTint(csl);
        binding.chipTerminadosEst.setChipIconTint(csl);
        binding.chipFilterAutorEst.setChipIconTint(csl);
        binding.chipFilterAutorEst.setTextColor(csl);
    }

    private void abrirValoracion(Libro libro) {
        if (viewModel.getRelaciones().getValue() == null) return;
        for (LibrosDeUsuario ldu : viewModel.getRelaciones().getValue()) {
            if (ldu.getId().getIdL() == libro.getId()) {
                Intent intent = new Intent(this, ValoracionLibroActivity.class);
                intent.putExtra("OBJETO_LIBRO_USUARIO", ldu);
                intent.putExtra("URL_LIBRO", ldu.getRuta());
                intent.putExtra("idL", ldu.getId().getIdL());
                intent.putExtra("idU", ldu.getId().getIdU());
                startActivity(intent);
                return;
            }
        }
    }

    private void actualizarInterfaz(List<Libro> libros) {
        boolean vacio = libros.isEmpty();
        binding.layoutEmptyLibrosEstanteria.setVisibility(vacio ? View.VISIBLE : View.GONE);
        binding.recyclerLibrosEstanteria.setVisibility(vacio ? View.GONE : View.VISIBLE);
        int total = libros.size();
        binding.tvCantidadLibrosEstanteria.setText((total == 1) ? "1 libro" : total + " libros");
    }

    private void mostrarDialogoQuitar(Libro libro) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Quitar de la estantería")
                .setMessage("¿Deseas quitar '" + libro.getNombre() + "'?")
                .setNegativeButton("CANCELAR", null)
                .setPositiveButton("QUITAR", (d, w) -> viewModel.desvincularLibro(this, libro.getId(), idEstanteria))
                .show();
    }

    private void mostrarDialogoNombre() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_editar_nombre_estanteria, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        com.google.android.material.textfield.TextInputEditText etNombre = dialogView.findViewById(R.id.etNuevoNombre);
        MaterialButton btnGuardar = dialogView.findViewById(R.id.btnGuardarNombre);
        ImageView btnClose = dialogView.findViewById(R.id.btnCloseDialog);
        ImageView ivIcono = dialogView.findViewById(R.id.ivIconoDialog);

        // Configurar color actual en el diálogo
        int colorFuerte = obtenerColorFuerte(getIntent().getStringExtra("Color"));
        ivIcono.getBackground().setColorFilter(colorFuerte, PorterDuff.Mode.SRC_IN);
        btnGuardar.setBackgroundTintList(ColorStateList.valueOf(colorFuerte));

        // Poner el nombre actual en el edit text
        etNombre.setText(binding.tvTituloEstanteria.getText().toString());

        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnGuardar.setOnClickListener(v -> {
            String nuevoNombre = etNombre.getText().toString().trim();
            if (!nuevoNombre.isEmpty()) {
                binding.tvTituloEstanteria.setText(nuevoNombre); // Cambio visual inmediato
                viewModel.actualizarNombreEstanteria(this, idEstanteria, nuevoNombre);
                dialog.dismiss();
            } else {
                etNombre.setError("El nombre no puede estar vacío");
            }
        });

        dialog.show();
    }

    private void aplicarColoresDinamicos() {
        String nombre = getIntent().getStringExtra("Nombre");
        String colorPastel = getIntent().getStringExtra("Color");
        if (nombre != null) binding.tvTituloEstanteria.setText(nombre);
        aplicarColoresDinamicosManual(colorPastel != null ? colorPastel : "#F3E5F5");
    }

    private int obtenerColorFuerte(String pastel) {
        if (pastel == null) return Color.parseColor("#664FA4");
        switch (pastel.toUpperCase()) {
            case "#F3E5F5": return Color.parseColor("#664FA4");
            case "#E3F2FD": return Color.parseColor("#1976D2");
            case "#E8F5E9": return Color.parseColor("#388E3C");
            case "#FFF3E0": return Color.parseColor("#F57C00");
            case "#FFEBEE": return Color.parseColor("#C2185B");
            default: return Color.parseColor("#664FA4");
        }
    }

    private void setupRecyclerView() {
        binding.recyclerLibrosEstanteria.setLayoutManager(new LinearLayoutManager(this));
    }
}