package com.tfg.charmreader.menu.publ.misGrupos.suscritos;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
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
    private MaterialButton btnEnviar;
    private ImageView btnBack;

    private int idLibro, idGrupo;
    private I_ApiValoracion apiValoracion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Barra de estado blanca con iconos oscuros
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        setContentView(R.layout.activity_valoracion_libro_nueva);

        inicializarVistas();

        idLibro = getIntent().getIntExtra("idLibro", -1);
        idGrupo = getIntent().getIntExtra("idGrupo", -1);

        btnEnviar.setOnClickListener(v -> prepararEnvio());
        btnBack.setOnClickListener(v -> finish());
    }

    private void inicializarVistas() {
        ratingBar = findViewById(R.id.ratingValoracion);
        etReseña = findViewById(R.id.etReseña);
        btnEnviar = findViewById(R.id.btnEnviarValoracion);
        btnBack = findViewById(R.id.btnBackValoracionLibro);
        apiValoracion = API.getInstancia().create(I_ApiValoracion.class);
    }

    private void prepararEnvio() {
        float estrellas = ratingBar.getRating();
        if (estrellas == 0) {
            Toast.makeText(this, "Por favor, selecciona una puntuación", Toast.LENGTH_SHORT).show();
            return;
        }

        btnEnviar.setEnabled(false); // Evitar spam de clics

        Utilidades.obtenerIdUsuarioDesdeAPI(new Utilidades.IdUsuarioCallback() {
            @Override
            public void onIdCargado(int idUsuario) {
                if (idUsuario != -1) {
                    enviarValoracionFinal(idUsuario, estrellas);
                } else {
                    btnEnviar.setEnabled(true);
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
                    btnEnviar.setEnabled(true);
                    Log.e("RETROFIT_ERROR", "Código: " + response.code());
                    Toast.makeText(ValoracionLibroNueva.this, "Error del servidor: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Valoracion> call, Throwable t) {
                btnEnviar.setEnabled(true);
                Toast.makeText(ValoracionLibroNueva.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}