package com.tfg.charmreader.menu.priv.estanteria;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import com.tfg.charmreader.interfacesAPI.I_ApiLibrosDeUsuario;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.LibrosDeUsuario;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ValoracionLibro extends AppCompatActivity {

    private RatingBar ratingBar;
    private TextInputEditText etDescription;
    private MaterialButton btnSubmit;
    private ImageView btnBack; // Cambiado a ImageView para la X

    private I_ApiLibrosDeUsuario apiService;
    private int idUsuario, idLibro;
    private LibrosDeUsuario libroActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Ajuste de teclado y desenfoque (Android 12+)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            getWindow().getAttributes().setBlurBehindRadius(20);
        }

        setContentView(R.layout.activity_valoracion_libro);

        idUsuario = getIntent().getIntExtra("idU", -1);
        idLibro = getIntent().getIntExtra("idL", -1);

        inicializarVistas();
        configurarRetrofit();
        cargarDatosLibro();

        // Listeners para cerrar con confirmación
        btnBack.setOnClickListener(v -> comprobarYSalir());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                comprobarYSalir();
            }
        });

        btnSubmit.setOnClickListener(v -> actualizarValoracion());
    }

    private void inicializarVistas() {
        ratingBar = findViewById(R.id.ratingBar);
        etDescription = findViewById(R.id.etDescription);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBack = findViewById(R.id.btnBackValoracion);
    }

    private void configurarRetrofit() {
        apiService = API.getInstancia().create(I_ApiLibrosDeUsuario.class);
    }

    private void comprobarYSalir() {
        String desc = etDescription.getText().toString().trim();
        float stars = ratingBar.getRating();

        // Si ha interactuado con la vista, preguntamos antes de salir
        if (!desc.isEmpty() || stars > 0) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("¿Descartar cambios?")
                    .setMessage("Si sales ahora perderás la valoración introducida.")
                    .setNegativeButton("Seguir editando", (dialog, which) -> dialog.dismiss())
                    .setPositiveButton("Descartar", (dialog, which) -> finish())
                    .show();
        } else {
            finish();
        }
    }

    private void cargarDatosLibro() {
        apiService.getLibrodeUsuario(idUsuario, idLibro).enqueue(new Callback<LibrosDeUsuario>() {
            @Override
            public void onResponse(Call<LibrosDeUsuario> call, Response<LibrosDeUsuario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    libroActual = response.body();
                    ratingBar.setRating((float) libroActual.getValoracion());
                    etDescription.setText(libroActual.getDescripcion());
                }
            }
            @Override
            public void onFailure(Call<LibrosDeUsuario> call, Throwable t) {
                Toast.makeText(ValoracionLibro.this, "Error al conectar con la API", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarValoracion() {
        if (libroActual == null) return;

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Guardando...");

        libroActual.setValoracion((double) ratingBar.getRating());
        libroActual.setDescripcion(etDescription.getText().toString());

        apiService.guardarProgreso(libroActual).enqueue(new Callback<LibrosDeUsuario>() {
            @Override
            public void onResponse(Call<LibrosDeUsuario> call, Response<LibrosDeUsuario> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ValoracionLibro.this, "¡Valoración guardada!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Guardar Valoración");
                    Toast.makeText(ValoracionLibro.this, "Error al guardar", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<LibrosDeUsuario> call, Throwable t) {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Guardar Valoración");
                Log.e("API_ERROR", t.getMessage());
            }
        });
    }
}