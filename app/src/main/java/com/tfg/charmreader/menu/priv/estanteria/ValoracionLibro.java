package com.tfg.charmreader.menu.priv.estanteria;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
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
    private ShapeableImageView btnBack;

    private I_ApiLibrosDeUsuario apiService;
    private int idUsuario, idLibro;
    private LibrosDeUsuario libroActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valoracion_libro);

        // Estilo de barra de estado
        configurarVentana();

        idUsuario = getIntent().getIntExtra("idU", -1);
        idLibro = getIntent().getIntExtra("idL", -1);

        inicializarVistas();
        configurarRetrofit();
        cargarDatosLibro();

        btnBack.setOnClickListener(v -> finish());
        btnSubmit.setOnClickListener(v -> actualizarValoracion());
    }

    private void configurarVentana() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }
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

        btnSubmit.setEnabled(false); // Evitar doble click
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