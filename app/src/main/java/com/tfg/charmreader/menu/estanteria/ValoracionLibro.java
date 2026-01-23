package com.tfg.charmreader.menu.estanteria;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

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
    private Button btnSubmit;

    private I_ApiLibrosDeUsuario apiService;
    private int idUsuario, idLibro;
    private LibrosDeUsuario libroActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valoracion_libro);

        // 1. Obtener IDs del Intent
        idUsuario = getIntent().getIntExtra("idU", -1);
        idLibro = getIntent().getIntExtra("idL", -1);

        // 2. Inicializar Retrofit y Vistas
        inicializarVistas();
        configurarRetrofit();

        // 3. Cargar datos actuales del libro
        cargarDatosLibro();

        btnSubmit.setOnClickListener(v -> actualizarValoracion());
    }

    private void inicializarVistas() {
        ratingBar = findViewById(R.id.ratingBar);
        etDescription = findViewById(R.id.etDescription);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    private void configurarRetrofit() {
        /*Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("TU_URL_BASE_AQUI") // Ejemplo: http://10.0.2.2:8080/
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(I_ApiLibrosDeUsuario.class);*/
        apiService = API.getInstancia().create(I_ApiLibrosDeUsuario.class);
    }

    private void cargarDatosLibro() {
        apiService.getLibrodeUsuario(idUsuario, idLibro).enqueue(new Callback<LibrosDeUsuario>() {
            @Override
            public void onResponse(Call<LibrosDeUsuario> call, Response<LibrosDeUsuario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    libroActual = response.body();
                    // Opcional: Rellenar la UI si ya tenía valoración previa
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

        // Modificar el objeto con los nuevos datos
        libroActual.setValoracion((double) ratingBar.getRating());
        libroActual.setDescripcion(etDescription.getText().toString());

        // Enviar mediante guardarProgreso (@POST)
        apiService.guardarProgreso(libroActual).enqueue(new Callback<LibrosDeUsuario>() {
            @Override
            public void onResponse(Call<LibrosDeUsuario> call, Response<LibrosDeUsuario> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ValoracionLibro.this, "Valoración guardada", Toast.LENGTH_SHORT).show();
                    finish(); // Cerrar pantalla al terminar
                } else {
                    Toast.makeText(ValoracionLibro.this, "Error al guardar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LibrosDeUsuario> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
            }
        });
    }
}