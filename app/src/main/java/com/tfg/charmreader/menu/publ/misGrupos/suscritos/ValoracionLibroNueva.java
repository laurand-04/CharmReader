package com.tfg.charmreader.menu.publ.misGrupos.suscritos;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RatingBar;
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

public class ValoracionLibroNueva extends AppCompatActivity {

    private RatingBar ratingBar;
    private TextInputEditText etReseña;
    private Button btnEnviar;

    private int idLibro, idGrupo;
    private I_ApiValoracion apiValoracion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valoracion_libro_nueva);

        ratingBar = findViewById(R.id.ratingValoracion);
        etReseña = findViewById(R.id.etReseña);
        btnEnviar = findViewById(R.id.btnEnviarValoracion);
        apiValoracion = API.getInstancia().create(I_ApiValoracion.class);

        idLibro = getIntent().getIntExtra("idLibro", -1);
        idGrupo = getIntent().getIntExtra("idGrupo", -1);

        btnEnviar.setOnClickListener(v -> prepararEnvio());
    }

    private void prepararEnvio() {
        float estrellas = ratingBar.getRating();
        if (estrellas == 0) {
            Toast.makeText(this, "Por favor, selecciona una puntuación", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔹 CAMBIO AQUÍ: Ahora usamos Utilidades.IdUsuarioCallback
        Utilidades.obtenerIdUsuarioDesdeAPI(new Utilidades.IdUsuarioCallback() {
            @Override
            public void onIdCargado(int idUsuario) {
                if (idUsuario != -1) {
                    enviarValoracionFinal(idUsuario, estrellas);
                } else {
                    // Esto se ejecuta si falla la red o el usuario no existe
                    Toast.makeText(ValoracionLibroNueva.this, "Error: No se pudo identificar al usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void enviarValoracionFinal(int idUsuario, float estrellas) {
        String comentario = etReseña.getText().toString().trim();

        Valoracion nuevaValoracion = new Valoracion();
        nuevaValoracion.setIdUsuario(idUsuario);
        nuevaValoracion.setIdReferencia(idLibro);
        nuevaValoracion.setIdGrupo(idGrupo);
        nuevaValoracion.setCalificacion((int) estrellas);
        nuevaValoracion.setDescripcion(comentario);
        nuevaValoracion.setTipo(Valoracion.TipoValoracion.LIBRO);

        apiValoracion.crear(nuevaValoracion).enqueue(new Callback<Valoracion>() {
            @Override
            public void onResponse(Call<Valoracion> call, Response<Valoracion> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ValoracionLibroNueva.this, "¡Reseña publicada!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.e("RETROFIT_ERROR", "Código: " + response.code());
                    Toast.makeText(ValoracionLibroNueva.this, "Error del servidor: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Valoracion> call, Throwable t) {
                Toast.makeText(ValoracionLibroNueva.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}