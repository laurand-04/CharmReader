package com.tfg.charmreader.ui.priv.futuro;

import android.app.Activity;
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
import com.tfg.charmreader.databinding.ActivityNuevoFuturoLibroBinding;
import com.tfg.charmreader.viewmodel.priv.futuro.NuevoFuturoViewModel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NuevoFuturoLibroActivity extends AppCompatActivity {

    private ActivityNuevoFuturoLibroBinding binding;
    private NuevoFuturoViewModel viewModel;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNuevoFuturoLibroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        aplicarEfectoBlur();

        viewModel = new ViewModelProvider(this).get(NuevoFuturoViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getFechaSeleccionada().observe(this, date -> {
            if (date != null) binding.etFecha.setText(sdf.format(date));
        });

        viewModel.getIsLoading().observe(this, loading -> {
            binding.layoutLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.layoutContenido.setAlpha(loading ? 0.3f : 1.0f);
        });

        viewModel.getIsSuccess().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "¡Lanzamiento guardado!", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
                finish();
            }
        });

        viewModel.getError().observe(this, msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    private void setupListeners() {
        binding.btnBackNuevoFuturo.setOnClickListener(v -> comprobarYSalir());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() { comprobarYSalir(); }
        });

        binding.etFecha.setOnClickListener(v -> mostrarDatePicker());

        binding.btnGuardar.setOnClickListener(v ->
                viewModel.guardarNuevoLibro(this,
                        binding.etTitulo.getText().toString().trim(),
                        binding.etAutor.getText().toString().trim())
        );
    }

    private void mostrarDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (viewModel.getFechaSeleccionada().getValue() != null) {
            calendar.setTime(viewModel.getFechaSeleccionada().getValue());
        }

        DatePickerDialog dpd = new DatePickerDialog(this, R.style.DialogPickerTheme, (view, year, month, dayOfMonth) -> {
            Calendar seleccion = Calendar.getInstance();
            seleccion.set(year, month, dayOfMonth);
            viewModel.setFecha(seleccion.getTime());
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        dpd.getDatePicker().setMinDate(System.currentTimeMillis());
        dpd.show();
    }

    private void comprobarYSalir() {
        String t = binding.etTitulo.getText().toString().trim();
        String a = binding.etAutor.getText().toString().trim();

        if (!t.isEmpty() || !a.isEmpty() || viewModel.getFechaSeleccionada().getValue() != null) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("¿Descartar lanzamiento?")
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