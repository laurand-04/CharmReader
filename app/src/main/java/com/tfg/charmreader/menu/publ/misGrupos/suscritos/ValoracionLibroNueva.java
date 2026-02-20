package com.tfg.charmreader.menu.publ.misGrupos.suscritos;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.tfg.charmreader.R;
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

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            getWindow().getAttributes().setBlurBehindRadius(20);
        }

        setContentView(R.layout.activity_valoracion_libro_nueva);

        inicializarVistas();

        idLibro = getIntent().getIntExtra("idLibro", -1);
        idGrupo = getIntent().getIntExtra("idGrupo", -1);

        btnEnviar.setOnClickListener(v -> prepararEnvio());
        btnBack.setOnClickListener(v -> comprobarYSalir());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                comprobarYSalir();
            }
        });
    }

    private void inicializarVistas() {
        ratingBar = findViewById(R.id.ratingValoracion);
        etReseña = findViewById(R.id.etReseña);
        btnEnviar = findViewById(R.id.btnEnviarValoracion);
        btnBack = findViewById(R.id.btnBackValoracionLibro);
        apiValoracion = API.getInstancia().create(I_ApiValoracion.class);
    }

    private void comprobarYSalir() {
        String comentario = etReseña.getText().toString().trim();
        float estrellas = ratingBar.getRating();

        if (!comentario.isEmpty() || estrellas > 0) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("¿Descartar valoración?")
                    .setMessage("Si sales ahora perderás la reseña que has escrito.")
                    .setNegativeButton("Seguir escribiendo", null)
                    .setPositiveButton("Descartar", (dialog, which) -> finish())
                    .show();
        } else {
            finish();
        }
    }

    private void prepararEnvio() {
        float estrellas = ratingBar.getRating();
        if (estrellas == 0) {
            Toast.makeText(this, "Por favor, selecciona una puntuación", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔥 OBTENER ID LOCALMENTE (Instantáneo y sin red)
        SharedPreferences prefs = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);

        if (idUsuario == -1) {
            Toast.makeText(this, "Error: No se encontró la sesión del usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        btnEnviar.setEnabled(false);
        btnEnviar.setText("PUBLICANDO...");

        enviarValoracionFinal(idUsuario, estrellas);
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
                    setResult(RESULT_OK); // Importante para refrescar la pantalla anterior
                    finish();
                } else {
                    rehabilitarBoton();
                }
            }

            @Override
            public void onFailure(Call<Valoracion> call, Throwable t) {
                rehabilitarBoton();
                Toast.makeText(ValoracionLibroNueva.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rehabilitarBoton() {
        btnEnviar.setEnabled(true);
        btnEnviar.setText("PUBLICAR VALORACIÓN");
    }
}