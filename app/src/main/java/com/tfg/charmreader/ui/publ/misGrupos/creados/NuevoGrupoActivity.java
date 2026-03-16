package com.tfg.charmreader.ui.publ.misGrupos.creados;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tfg.charmreader.data.network.API.PlacesClientManager;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import com.tfg.charmreader.databinding.ActivityNuevoGrupoBinding;
import com.tfg.charmreader.viewmodel.publ.misGrupos.creados.NuevoGrupoViewModel;

import java.util.ArrayList;
import java.util.List;

public class NuevoGrupoActivity extends AppCompatActivity {

    private ActivityNuevoGrupoBinding binding;
    private NuevoGrupoViewModel viewModel;
    private Uri uriImagenSeleccionada;

    // Variables para el autocompletado de ubicación gratuito
    private ArrayAdapter<String> placesAdapter;
    private List<PlacesClientManager.UbicacionSimple> sugerenciasActuales = new ArrayList<>();
    private Handler handlerBusqueda = new Handler(Looper.getMainLooper());
    private Runnable runnableBusqueda;

    // Selector de galería
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    uriImagenSeleccionada = result.getData().getData();
                    binding.ivPreviewGrupo.setImageURI(uriImagenSeleccionada);
                    binding.ivPreviewGrupo.setImageTintList(null);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNuevoGrupoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(NuevoGrupoViewModel.class);

        setupUI();
        setupObservers();
    }

    private void setupUI() {
        setupActionBar();
        setupImagePicker();
        setupFrequencyDropdown();
        setupLocationAutocomplete();
        setupActionButtons();
    }

    private void setupImagePicker() {
        binding.cardSeleccionarImagen.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });
    }

    private void setupFrequencyDropdown() {
        String[] opciones = {"Semanal", "Quincenal", "Mensual"};
        ArrayAdapter<String> adapterFrecuencia = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, opciones);
        binding.autoCompleteFrecuencia.setAdapter(adapterFrecuencia);
        binding.autoCompleteFrecuencia.setText(opciones[0], false);
    }

    private void setupLocationAutocomplete() {
        initLocationAdapter();

        binding.etUbicacionGrupo.setThreshold(2);
        binding.etUbicacionGrupo.setDropDownHeight(600);

        binding.etUbicacionGrupo.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) binding.etUbicacionGrupo.showDropDown();
        });

        binding.etUbicacionGrupo.addTextChangedListener(createLocationTextWatcher());

        binding.etUbicacionGrupo.setOnItemClickListener((parent, view, position, id) -> {
            PlacesClientManager.UbicacionSimple seleccion = sugerenciasActuales.get(position);
            binding.etUbicacionGrupo.setText(seleccion.nombre, false);
            Toast.makeText(this, "Ubicación fijada", Toast.LENGTH_SHORT).show();
        });
    }

    // Método auxiliar para no ensuciar setupLocationAutocomplete
    private void initLocationAdapter() {
        placesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>()) {
            @Override
            public android.widget.Filter getFilter() {
                return new android.widget.Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        results.values = sugerenciasActuales;
                        results.count = sugerenciasActuales.size();
                        return results;
                    }
                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        notifyDataSetChanged();
                    }
                };
            }
        };
        binding.etUbicacionGrupo.setAdapter(placesAdapter);
    }

    private TextWatcher createLocationTextWatcher() {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (runnableBusqueda != null) handlerBusqueda.removeCallbacks(runnableBusqueda);

                runnableBusqueda = () -> {
                    if (query.length() >= 2) fetchLocationSuggestions(query);
                };
                handlerBusqueda.postDelayed(runnableBusqueda, 500);
            }
        };
    }

    private void fetchLocationSuggestions(String query) {
        PlacesClientManager.obtenerPredicciones(query, new PlacesClientManager.OnPlacesPredictionsListener() {
            @Override
            public void onSuccess(List<PlacesClientManager.UbicacionSimple> sugerencias) {
                sugerenciasActuales = sugerencias;
                List<String> nombres = new ArrayList<>();
                for (PlacesClientManager.UbicacionSimple u : sugerencias) nombres.add(u.nombre);

                runOnUiThread(() -> {
                    placesAdapter.clear();
                    placesAdapter.addAll(nombres);
                    binding.etUbicacionGrupo.showDropDown();
                });
            }

            @Override public void onError(String error) { Log.e("Ubicacion", error); }
        });
    }

    private void setupActionBar() {
        binding.btnBackNuevoGrupo.setOnClickListener(v -> comprobarYSalir());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { comprobarYSalir(); }
        });
    }

    private void setupActionButtons() {
        binding.btnGuardarGrupo.setOnClickListener(v -> validarYEnviar());
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, loading -> {
            binding.btnGuardarGrupo.setEnabled(!loading);
            binding.btnGuardarGrupo.setText(loading ? "CREANDO..." : "CREAR GRUPO");
        });

        viewModel.getIsSuccess().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "¡Grupo creado con éxito!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });

        viewModel.getMensaje().observe(this, msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    private void validarYEnviar() {
        String nombre = binding.etNombreGrupo.getText().toString().trim();
        String ubicacion = binding.etUbicacionGrupo.getText().toString().trim();

        if (nombre.isEmpty() || ubicacion.isEmpty()) {
            Toast.makeText(this, "Nombre y ubicación son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        int idU = AuthRepository.getInstance(getApplicationContext()).getIdUsuario();

        // Ahora pasamos también la latitud y longitud al ViewModel
        viewModel.crearNuevoGrupo(
                nombre,
                ubicacion,
                binding.etDescripcionGrupo.getText().toString().trim(),
                binding.autoCompleteFrecuencia.getText().toString(),
                uriImagenSeleccionada,
                idU
        );
    }

    private void comprobarYSalir() {
        boolean hayDatos = !binding.etNombreGrupo.getText().toString().trim().isEmpty() || uriImagenSeleccionada != null;
        if (hayDatos) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("¿Descartar grupo?")
                    .setMessage("Perderás los datos introducidos.")
                    .setNegativeButton("Seguir editando", null)
                    .setPositiveButton("Descartar", (d, w) -> finish())
                    .show();
        } else {
            finish();
        }
    }
}