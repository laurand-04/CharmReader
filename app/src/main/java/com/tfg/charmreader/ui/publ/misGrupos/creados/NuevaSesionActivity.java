package com.tfg.charmreader.ui.publ.misGrupos.creados;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.tfg.charmreader.R;
import com.tfg.charmreader.databinding.ActivityNuevaSesionBinding;
import com.tfg.charmreader.viewmodel.publ.misGrupos.creados.NuevaSesionViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NuevaSesionActivity extends AppCompatActivity {

    private ActivityNuevaSesionBinding binding;
    private NuevaSesionViewModel viewModel;
    private Calendar calendario = Calendar.getInstance();
    private int idGrupo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNuevaSesionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        idGrupo = getIntent().getIntExtra("idGrupo", -1);
        viewModel = new ViewModelProvider(this).get(NuevaSesionViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, loading -> {
            binding.layoutLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.layoutContenido.setAlpha(loading ? 0.3f : 1.0f);
        });

        viewModel.getIsSuccess().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "✅ Sesión programada", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });

        viewModel.getError().observe(this, msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    private void setupListeners() {
        binding.btnBackNuevaSesion.setOnClickListener(v -> finish());

        binding.etFecha.setOnClickListener(v -> mostrarDatePicker());
        binding.etHora.setOnClickListener(v -> mostrarTimePicker());

        binding.btnGuardarSesion.setOnClickListener(v ->
                viewModel.guardarSesion(idGrupo,
                        calendario.getTime(),
                        binding.etHora.getText().toString(),
                        binding.etCapInicio.getText().toString(),
                        binding.etCapFin.getText().toString())
        );
    }

    private void mostrarDatePicker() {
        DatePickerDialog dpd = new DatePickerDialog(this, R.style.DialogPickerTheme, (view, year, month, day) -> {
            calendario.set(year, month, day);
            binding.etFecha.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendario.getTime()));
        }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private void mostrarTimePicker() {
        new TimePickerDialog(this, R.style.DialogPickerTheme, (view, hour, minute) -> {
            binding.etHora.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
        }, 18, 0, true).show();
    }
}