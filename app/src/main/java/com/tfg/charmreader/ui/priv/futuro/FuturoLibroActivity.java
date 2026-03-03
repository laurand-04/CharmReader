package com.tfg.charmreader.ui.priv.futuro;

import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tfg.charmreader.R;
import com.tfg.charmreader.data.model.LibrosSinEstrenar;
import com.tfg.charmreader.databinding.ActivityFuturoLibroBinding;
import com.tfg.charmreader.viewmodel.priv.futuro.FuturoViewModel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class FuturoLibroActivity extends AppCompatActivity {

    private ActivityFuturoLibroBinding binding;
    private FuturoViewModel viewModel;
    private LibrosSinEstrenar libroRecibido;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFuturoLibroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        aplicarEfectoBlur();

        libroRecibido = (LibrosSinEstrenar) getIntent().getSerializableExtra("libro_seleccionado");
        if (libroRecibido == null) { finish(); return; }

        viewModel = new ViewModelProvider(this).get(FuturoViewModel.class);

        cargarDatosIniciales();
        setupObservers();
        setupListeners();
    }

    private void cargarDatosIniciales() {
        binding.etDetalleTitulo.setText(libroRecibido.getId().getNombre());
        binding.etDetalleAutor.setText(libroRecibido.getAutor());
        if (libroRecibido.getFechaPublicacion() != null) {
            viewModel.setFecha(libroRecibido.getFechaPublicacion());
        }
    }

    private void setupObservers() {
        viewModel.getFechaSeleccionada().observe(this, date -> {
            if (date != null) binding.etDetalleFecha.setText(sdf.format(date));
        });

        viewModel.getIsLoading().observe(this, loading -> {
            binding.layoutLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.layoutContenido.setAlpha(loading ? 0.3f : 1.0f);
        });

        viewModel.getIsSuccess().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Lanzamiento actualizado", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });

        viewModel.getError().observe(this, msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    private void setupListeners() {
        binding.btnBackDetalleFuturo.setOnClickListener(v -> comprobarYSalir());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() { comprobarYSalir(); }
        });

        binding.etDetalleFecha.setOnClickListener(v -> mostrarCalendario());

        binding.btnActualizar.setOnClickListener(v ->
                viewModel.guardarCambios(libroRecibido,
                        binding.etDetalleTitulo.getText().toString().trim(),
                        binding.etDetalleAutor.getText().toString().trim())
        );
    }

    private void mostrarCalendario() {
        Calendar cal = Calendar.getInstance();
        if (viewModel.getFechaSeleccionada().getValue() != null) {
            cal.setTime(viewModel.getFechaSeleccionada().getValue());
        }

        DatePickerDialog dpd = new DatePickerDialog(this, R.style.DialogPickerTheme, (view, year, month, dayOfMonth) -> {
            Calendar seleccion = Calendar.getInstance();
            seleccion.set(year, month, dayOfMonth);
            viewModel.setFecha(seleccion.getTime());
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        dpd.getDatePicker().setMinDate(System.currentTimeMillis());
        dpd.show();
    }

    private void comprobarYSalir() {
        if (viewModel.haHabidoCambios(libroRecibido,
                binding.etDetalleTitulo.getText().toString().trim(),
                binding.etDetalleAutor.getText().toString().trim())) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("¿Descartar cambios?")
                    .setMessage("Tienes cambios sin guardar.")
                    .setNegativeButton("Seguir editando", null)
                    .setPositiveButton("Descartar", (d, w) -> finish())
                    .show();
        } else {
            finish();
        }
    }

    private void aplicarEfectoBlur() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            getWindow().getAttributes().setBlurBehindRadius(20);
        }
    }
}