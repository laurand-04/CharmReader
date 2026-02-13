package com.tfg.charmreader.menu.publ.misGrupos.suscritos;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
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

public class ValoracionGrupo extends AppCompatActivity {

    private RatingBar ratingBar;
    private TextInputEditText etReseña;
    private MaterialButton btnEnviar;
    private ImageView btnBack;
    private int idLibro, idGrupo;
    private I_ApiValoracion apiValoracion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Barra de estado moderna
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        setContentView(R.layout.activity_valoracion_grupo);

        inicializarVistas();

        // Recuperamos los datos del Intent
        idLibro = getIntent().getIntExtra("idLibro", -1);
        idGrupo = getIntent().getIntExtra("idGrupo", -1);

        btnEnviar.setOnClickListener(v -> prepararEnvio());
        btnBack.setOnClickListener(v -> finish());
    }

    private void inicializarVistas() {
        ratingBar = findViewById(R.id.ratingValoracion);
        etReseña = findViewById(R.id.etReseña);
        btnEnviar = findViewById(R.id.btnEnviarValoracion);
        btnBack = findViewById(R.id.btnBackValoracion);
        apiValoracion = API.getInstancia().create(I_ApiValoracion.class);
    }

    private void prepararEnvio() {
        if (ratingBar.getRating() == 0) {
            Toast.makeText(this, "Por favor, selecciona una puntuación", Toast.LENGTH_SHORT).show();
            return;
        }

        Utilidades.obtenerIdUsuarioDesdeAPI(idUsuario -> {
            if (idUsuario != -1) {
                enviarDatosAlServidor(idUsuario);
            } else {
                Toast.makeText(ValoracionGrupo.this, "Error: Sesión no válida", Toast.LENGTH_SHORT).show();
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

        if (idLibro != -1) {
            v.setIdReferencia(idLibro);
            v.setTipo(Valoracion.TipoValoracion.LIBRO);
        } else {
            v.setIdReferencia(idGrupo);
            v.setTipo(Valoracion.TipoValoracion.GRUPO);
        }

        btnEnviar.setEnabled(false); // Evitar doble envío

        apiValoracion.crear(v).enqueue(new Callback<Valoracion>() {
            @Override
            public void onResponse(Call<Valoracion> call, Response<Valoracion> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ValoracionGrupo.this, "¡Reseña publicada con éxito!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    btnEnviar.setEnabled(true);
                    Toast.makeText(ValoracionGrupo.this, "Error al publicar: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Valoracion> call, Throwable t) {
                btnEnviar.setEnabled(true);
                Toast.makeText(ValoracionGrupo.this, "Sin conexión al servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }
}