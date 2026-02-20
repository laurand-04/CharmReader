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

        // 1. Efecto Blur (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            getWindow().getAttributes().setBlurBehindRadius(20);
        }

        setContentView(R.layout.activity_valoracion_grupo);

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
        btnBack = findViewById(R.id.btnBackValoracion);
        apiValoracion = API.getInstancia().create(I_ApiValoracion.class);
    }

    private void comprobarYSalir() {
        String comentario = etReseña.getText().toString().trim();
        float estrellas = ratingBar.getRating();

        if (!comentario.isEmpty() || estrellas > 0) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("¿Descartar reseña?")
                    .setMessage("Si sales ahora perderás lo que has escrito.")
                    .setNegativeButton("Seguir escribiendo", null)
                    .setPositiveButton("Descartar", (dialog, which) -> finish())
                    .show();
        } else {
            finish();
        }
    }

    private void prepararEnvio() {
        if (ratingBar.getRating() == 0) {
            Toast.makeText(this, "Por favor, selecciona una puntuación", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔥 OBTENER ID LOCALMENTE (Instantáneo)
        SharedPreferences prefs = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);

        if (idUsuario == -1) {
            Toast.makeText(this, "Error: Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        btnEnviar.setEnabled(false);
        btnEnviar.setText("PUBLICANDO...");

        enviarDatosAlServidor(idUsuario);
    }

    private void enviarDatosAlServidor(int idUsuario) {
        String comentario = etReseña.getText().toString().trim();
        int estrellas = (int) ratingBar.getRating();

        Valoracion v = new Valoracion();
        v.setIdUsuario(idUsuario);
        v.setIdGrupo(idGrupo);
        v.setCalificacion(estrellas);
        v.setDescripcion(comentario);

        // Si idLibro es -1, es una reseña del grupo. Si no, es de un libro específico del grupo.
        if (idLibro != -1) {
            v.setIdReferencia(idLibro);
            v.setTipo(Valoracion.TipoValoracion.LIBRO);
        } else {
            v.setIdReferencia(idGrupo);
            v.setTipo(Valoracion.TipoValoracion.GRUPO);
        }

        apiValoracion.crear(v).enqueue(new Callback<Valoracion>() {
            @Override
            public void onResponse(Call<Valoracion> call, Response<Valoracion> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ValoracionGrupo.this, "¡Reseña publicada!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // Para que la pantalla anterior se refresque si es necesario
                    finish();
                } else {
                    rehabilitarBoton();
                }
            }

            @Override
            public void onFailure(Call<Valoracion> call, Throwable t) {
                rehabilitarBoton();
                Toast.makeText(ValoracionGrupo.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rehabilitarBoton() {
        btnEnviar.setEnabled(true);
        btnEnviar.setText("PUBLICAR RESEÑA");
    }
}