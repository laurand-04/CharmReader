package com.tfg.charmreader.menu.publ.explorar;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.tfg.charmreader.R;
import com.tfg.charmreader.Utilidades;
import com.tfg.charmreader.interfacesAPI.I_ApiGrupoLectura;
import com.tfg.charmreader.interfacesAPI.I_ApiMiembro;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.GrupoLectura;
import com.tfg.charmreader.objetosBD.Miembro;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NuevoGrupo extends AppCompatActivity {

    private EditText etNombreGrupo, etUbicacionGrupo, etDescripcionGrupo, etUrlImagenGrupo;
    // Cambiamos Spinner por AutoCompleteTextView para que coincida con tu XML
    private AutoCompleteTextView autoCompleteFrecuencia;
    private Button btnGuardarGrupo;

    private final I_ApiGrupoLectura apiGrupo = API.getInstancia().create(I_ApiGrupoLectura.class);
    private final I_ApiMiembro apiMiembro = API.getInstancia().create(I_ApiMiembro.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_grupo);

        vincularVistas();
        configurarFrecuencia(); // Método para llenar las opciones

        btnGuardarGrupo.setOnClickListener(v -> validarYGuardar());
    }

    private void vincularVistas() {
        etNombreGrupo = findViewById(R.id.etNombreGrupo);
        etUbicacionGrupo = findViewById(R.id.etUbicacionGrupo);
        etDescripcionGrupo = findViewById(R.id.etDescripcionGrupo);
        etUrlImagenGrupo = findViewById(R.id.etUrlImagenGrupo);
        // Ahora usamos el ID que pusiste en el XML
        autoCompleteFrecuencia = findViewById(R.id.autoCompleteFrecuencia);
        btnGuardarGrupo = findViewById(R.id.btnGuardarGrupo);
    }

    private void configurarFrecuencia() {
        // Opciones para el desplegable
        String[] opciones = {"Semanal", "Quincenal", "Mensual"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, opciones);
        autoCompleteFrecuencia.setAdapter(adapter);

        // Ponemos un valor por defecto para que no esté vacío
        autoCompleteFrecuencia.setText(opciones[0], false);
    }

    private void validarYGuardar() {
        limpiarErrores();

        String nombre = etNombreGrupo.getText().toString().trim();
        String ubicacion = etUbicacionGrupo.getText().toString().trim();
        String desc = etDescripcionGrupo.getText().toString().trim();
        String url = etUrlImagenGrupo.getText().toString().trim();
        // Obtenemos el texto directamente del AutoComplete
        String frecuenciaTxt = autoCompleteFrecuencia.getText().toString();

        boolean error = false;
        if (nombre.isEmpty()) {
            marcarError(etNombreGrupo, "El nombre es obligatorio");
            error = true;
        }
        if (ubicacion.isEmpty()) {
            marcarError(etUbicacionGrupo, "La ubicación es obligatoria");
            error = true;
        }

        if (error) return;

        btnGuardarGrupo.setEnabled(false);
        btnGuardarGrupo.setText("CREANDO...");

        GrupoLectura.Frecuencia frecuenciaEnum = GrupoLectura.stringToFrecuencia(frecuenciaTxt);

        Utilidades.obtenerIdUsuarioDesdeAPI(new Utilidades.IdUsuarioCallback() {
            @Override
            public void onIdCargado(int idUsuario) {
                if (idUsuario == -1) {
                    restaurarBoton();
                    return;
                }

                GrupoLectura nuevo = new GrupoLectura(nombre, ubicacion, desc, frecuenciaEnum, url, idUsuario);

                apiGrupo.crearGrupo(nuevo).enqueue(new Callback<GrupoLectura>() {
                    @Override
                    public void onResponse(Call<GrupoLectura> call, Response<GrupoLectura> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            autoSuscribirCreador(response.body().getIdGrupo(), idUsuario);
                        } else {
                            restaurarBoton();
                            Toast.makeText(NuevoGrupo.this, "Error al crear grupo", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<GrupoLectura> call, Throwable t) {
                        restaurarBoton();
                        Toast.makeText(NuevoGrupo.this, "Error de red", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void autoSuscribirCreador(int idGrupo, int idUsuario) {
        Miembro admin = new Miembro(idGrupo, idUsuario);
        apiMiembro.unirse(admin).enqueue(new Callback<Miembro>() {
            @Override
            public void onResponse(Call<Miembro> call, Response<Miembro> response) {
                Toast.makeText(NuevoGrupo.this, "¡Grupo creado!", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onFailure(Call<Miembro> call, Throwable t) {
                finish();
            }
        });
    }

    private void marcarError(EditText et, String msj) {
        try {
            View parent = (View) et.getParent().getParent();
            if (parent instanceof TextInputLayout) {
                ((TextInputLayout) parent).setError(msj);
            }
        } catch (Exception e) {
            et.setError(msj);
        }
    }

    private void limpiarErrores() {
        marcarError(etNombreGrupo, null);
        marcarError(etUbicacionGrupo, null);
    }

    private void restaurarBoton() {
        runOnUiThread(() -> {
            btnGuardarGrupo.setEnabled(true);
            btnGuardarGrupo.setText("CREAR GRUPO");
        });
    }
}