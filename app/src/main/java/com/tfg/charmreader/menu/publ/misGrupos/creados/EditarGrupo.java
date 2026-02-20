package com.tfg.charmreader.menu.publ.misGrupos.creados;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_ApiGrupoLectura;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.GrupoLectura;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarGrupo extends AppCompatActivity {

    private TextInputEditText etNombre, etUbicacion, etDesc;
    private AutoCompleteTextView spinnerFrecuencia;
    private MaterialButton btnGuardar;
    private ImageView btnBack;

    private GrupoLectura grupo;

    private final I_ApiGrupoLectura apiGrupo =
            API.getInstancia().create(I_ApiGrupoLectura.class);

    private final String[] opcionesFrecuencia = {
            "SEMANAL", "QUINCENAL", "MENSUAL"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Forzar scroll cuando el teclado se abre
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        // Barra de estado blanca
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        setContentView(R.layout.activity_editar_grupo);

        grupo = (GrupoLectura) getIntent().getSerializableExtra("objetoGrupo");
        if (grupo == null) {
            finish();
            return;
        }

        inicializarVistas();
        configurarSelector();
        rellenarCampos();

        // 2. Listeners de salida con confirmación
        btnBack.setOnClickListener(v -> comprobarYSalir());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                comprobarYSalir();
            }
        });

        btnGuardar.setOnClickListener(v -> actualizarGrupo());
    }

    private void inicializarVistas() {
        etNombre = findViewById(R.id.etNombreEdicion);
        etUbicacion = findViewById(R.id.etUbicacionEdicion);
        etDesc = findViewById(R.id.etDescEdicion);
        spinnerFrecuencia = findViewById(R.id.spinnerFrecuencia);
        btnGuardar = findViewById(R.id.btnGuardarEdicion);
        btnBack = findViewById(R.id.btnBackEditar);
    }

    // Método para verificar cambios antes de cerrar
    private void comprobarYSalir() {
        String n = etNombre.getText().toString().trim();
        String u = etUbicacion.getText().toString().trim();
        String d = etDesc.getText().toString().trim();
        String f = spinnerFrecuencia.getText().toString();

        // Comprobamos si alguno de los campos es diferente al objeto original
        boolean haCambiado = !n.equals(grupo.getNombre()) ||
                !u.equals(grupo.getUbicacion()) ||
                !d.equals(grupo.getDescripcion()) ||
                !f.equals(grupo.getFrecuenciaReunion().name());

        if (haCambiado) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("¿Descartar cambios?")
                    .setMessage("Has realizado modificaciones. Si sales ahora, no se guardarán.")
                    .setNegativeButton("Seguir editando", (dialog, which) -> dialog.dismiss())
                    .setPositiveButton("Descartar", (dialog, which) -> finish())
                    .show();
        } else {
            finish();
        }
    }

    private void configurarSelector() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                opcionesFrecuencia
        );
        spinnerFrecuencia.setAdapter(adapter);
    }

    private void rellenarCampos() {
        etNombre.setText(grupo.getNombre());
        etUbicacion.setText(grupo.getUbicacion());
        etDesc.setText(grupo.getDescripcion());

        if (grupo.getFrecuenciaReunion() != null) {
            spinnerFrecuencia.setText(grupo.getFrecuenciaReunion().name(), false);
        }
    }

    private void actualizarGrupo() {
        String nombre = etNombre.getText().toString().trim();
        String ubicacion = etUbicacion.getText().toString().trim();
        String descripcion = etDesc.getText().toString().trim();
        String frecuenciaSeleccionada = spinnerFrecuencia.getText().toString();

        if (nombre.isEmpty()) {
            Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGuardar.setEnabled(false);
        btnGuardar.setText("GUARDANDO...");

        grupo.setNombre(nombre);
        grupo.setUbicacion(ubicacion);
        grupo.setDescripcion(descripcion);

        try {
            grupo.setFrecuenciaReunion(GrupoLectura.Frecuencia.valueOf(frecuenciaSeleccionada));
        } catch (IllegalArgumentException e) {
            rehabilitarBoton();
            Toast.makeText(this, "Selecciona una frecuencia válida", Toast.LENGTH_SHORT).show();
            return;
        }

        apiGrupo.actualizar(grupo).enqueue(new Callback<GrupoLectura>() {
            @Override
            public void onResponse(Call<GrupoLectura> call, Response<GrupoLectura> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(EditarGrupo.this, "¡Grupo actualizado!", Toast.LENGTH_SHORT).show();
                    Intent data = new Intent();
                    data.putExtra("grupoActualizado", response.body());
                    setResult(RESULT_OK, data);
                    finish();
                } else {
                    rehabilitarBoton();
                }
            }

            @Override
            public void onFailure(Call<GrupoLectura> call, Throwable t) {
                rehabilitarBoton();
                Toast.makeText(EditarGrupo.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rehabilitarBoton() {
        btnGuardar.setEnabled(true);
        btnGuardar.setText("GUARDAR CAMBIOS");
    }
}