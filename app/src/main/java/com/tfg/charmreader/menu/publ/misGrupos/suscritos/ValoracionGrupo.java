package com.tfg.charmreader.menu.publ.misGrupos.suscritos;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.tfg.charmreader.R;
import com.tfg.charmreader.Utilidades;
import com.tfg.charmreader.interfacesAPI.I_ApiValoracion;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.Valoracion;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ValoracionGrupo extends AppCompatActivity {

    private RatingBar ratingBar;
    private TextInputEditText etReseña;
    private Button btnEnviar;
    private int idLibro, idGrupo;
    private I_ApiValoracion apiValoracion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valoracion_grupo);

        ratingBar = findViewById(R.id.ratingValoracion);
        etReseña = findViewById(R.id.etReseña);
        btnEnviar = findViewById(R.id.btnEnviarValoracion);
        apiValoracion = API.getInstancia().create(I_ApiValoracion.class);

        // Recuperamos los datos del Intent
        idLibro = getIntent().getIntExtra("idLibro", -1);
        idGrupo = getIntent().getIntExtra("idGrupo", -1);

        btnEnviar.setOnClickListener(v -> prepararEnvio());
    }

    private void prepararEnvio() {
        if (ratingBar.getRating() == 0) {
            Toast.makeText(this, "Por favor, selecciona las estrellas", Toast.LENGTH_SHORT).show();
            return;
        }

        // Usamos tu método asíncrono de Utilidades para el ID de usuario
        Utilidades.obtenerIdUsuarioDesdeAPI(new Utilidades.IdUsuarioCallback() {
            @Override
            public void onIdCargado(int idUsuario) {
                if (idUsuario != -1) {
                    enviarDatosAlServidor(idUsuario);
                } else {
                    Toast.makeText(ValoracionGrupo.this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void enviarDatosAlServidor(int idUsuario) {
        String comentario = etReseña.getText().toString().trim();
        int estrellas = (int) ratingBar.getRating();

        Valoracion v = new Valoracion();
        v.setIdUsuario(idUsuario);
        v.setIdGrupo(idGrupo);
        v.setCalificacion(estrellas);
        v.setDescripcion(comentario);

        // LÓGICA CLAVE: Diferenciar si es reseña de LIBRO o de GRUPO
        if (idLibro != -1) {
            v.setIdReferencia(idLibro);
            v.setTipo(Valoracion.TipoValoracion.LIBRO);
        } else {
            // Si idLibro es -1, la referencia es el propio grupo
            v.setIdReferencia(idGrupo);
            v.setTipo(Valoracion.TipoValoracion.GRUPO);
        }

        apiValoracion.crear(v).enqueue(new Callback<Valoracion>() {
            @Override
            public void onResponse(Call<Valoracion> call, Response<Valoracion> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ValoracionGrupo.this, "¡Valoración enviada!", Toast.LENGTH_SHORT).show();
                    finish(); // Cerramos y volvemos
                } else {
                    Log.e("API_ERROR", "Código: " + response.code());
                    Toast.makeText(ValoracionGrupo.this, "Error servidor: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Valoracion> call, Throwable t) {
                Toast.makeText(ValoracionGrupo.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}