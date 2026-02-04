package com.tfg.charmreader.menu.publ.misGrupos.creados;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
    private Button btnGuardar;

    private GrupoLectura grupo;

    private final I_ApiGrupoLectura apiGrupo =
            API.getInstancia().create(I_ApiGrupoLectura.class);

    private final String[] opcionesFrecuencia = {
            "SEMANAL", "QUINCENAL", "MENSUAL"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_grupo);

        grupo = (GrupoLectura) getIntent().getSerializableExtra("objetoGrupo");
        if (grupo == null) {
            finish();
            return;
        }

        inicializarVistas();
        configurarSelector();
        rellenarCampos();

        btnGuardar.setOnClickListener(v -> actualizarGrupo());
    }

    private void inicializarVistas() {
        etNombre = findViewById(R.id.etNombreEdicion);
        etUbicacion = findViewById(R.id.etUbicacionEdicion);
        etDesc = findViewById(R.id.etDescEdicion);
        spinnerFrecuencia = findViewById(R.id.spinnerFrecuencia);
        btnGuardar = findViewById(R.id.btnGuardarEdicion);
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
            spinnerFrecuencia.setText(
                    grupo.getFrecuenciaReunion().name(),
                    false
            );
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

        // 🔄 Actualizamos el objeto local
        grupo.setNombre(nombre);
        grupo.setUbicacion(ubicacion);
        grupo.setDescripcion(descripcion);

        try {
            grupo.setFrecuenciaReunion(
                    GrupoLectura.Frecuencia.valueOf(frecuenciaSeleccionada)
            );
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Frecuencia no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        apiGrupo.actualizar(grupo).enqueue(new Callback<GrupoLectura>() {
            @Override
            public void onResponse(Call<GrupoLectura> call,
                                   Response<GrupoLectura> response) {

                if (response.isSuccessful() && response.body() != null) {

                    GrupoLectura grupoActualizado = response.body();

                    Toast.makeText(
                            EditarGrupo.this,
                            "¡Grupo actualizado!",
                            Toast.LENGTH_SHORT
                    ).show();

                    // 🔥 DEVOLVEMOS EL OBJETO ACTUALIZADO
                    Intent data = new Intent();
                    data.putExtra("grupoActualizado", grupoActualizado);
                    setResult(RESULT_OK, data);

                    finish();

                } else {
                    Toast.makeText(
                            EditarGrupo.this,
                            "Error: " + response.code(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<GrupoLectura> call, Throwable t) {
                Toast.makeText(
                        EditarGrupo.this,
                        "Fallo de red: " + t.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}
