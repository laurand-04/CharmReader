package com.tfg.charmreader.menu.priv.proximamente;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_ApiBook;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.BookEn;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProximoLibro extends AppCompatActivity {

    private ImageView ivPortada, btnBack, btnInfoExterna;
    private TextView tvTituloDisplay, tvAutorDisplay;
    private TextInputEditText etResumen, etSubtitulo;
    private AutoCompleteTextView etTema;
    private MaterialButton btnActualizar;

    private I_ApiBook apiService;
    private int idLibro;
    private BookEn libroActual;

    // Enumeración para los géneros literarios
    public enum TemaLibro {
        AVENTURAS, CIENCIA_FICCION, DRAMA, FANTASIA, HISTORICA, HUMOR,
        POLICIACA, ROMANCE, SUSPENSE, TERROR, INFANTIL, JUVENIL,
        BIOGRAFIA, AUTOAYUDA, ENSAYO, OTRO
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proximo_libro);

        // 1. Configuración de la barra de estado (Moderna/Blanca)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        idLibro = getIntent().getIntExtra("idLibro", -1);
        inicializarVistas();
        apiService = API.getInstancia().create(I_ApiBook.class);

        if (idLibro != -1) {
            cargarDatosLibro();
        }

        // 2. Eventos de botones de la cabecera
        btnBack.setOnClickListener(v -> finish());

        btnInfoExterna.setOnClickListener(v -> {
            if (libroActual != null && libroActual.getInfoUrl() != null && !libroActual.getInfoUrl().isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(libroActual.getInfoUrl()));
                startActivity(browserIntent);
            } else {
                Toast.makeText(this, "Información externa no disponible", Toast.LENGTH_SHORT).show();
            }
        });

        // 3. Evento de guardado
        btnActualizar.setOnClickListener(v -> actualizarInformacion());
    }

    private void inicializarVistas() {
        ivPortada = findViewById(R.id.ivDetallePortada);
        tvTituloDisplay = findViewById(R.id.tvDetalleTituloDisplay);
        tvAutorDisplay = findViewById(R.id.tvDetalleAutorDisplay);
        etSubtitulo = findViewById(R.id.etDetalleSubtitulo);
        etTema = findViewById(R.id.etDetalleTema);
        etResumen = findViewById(R.id.etDetalleResumen);
        btnActualizar = findViewById(R.id.btnActualizarLibro);
        btnInfoExterna = findViewById(R.id.btnInfoExterna);
        btnBack = findViewById(R.id.btnBackDetalle);
    }

    private void cargarDatosLibro() {
        apiService.obtenerBookPorId(idLibro).enqueue(new Callback<BookEn>() {
            @Override
            public void onResponse(Call<BookEn> call, Response<BookEn> response) {
                if (response.isSuccessful() && response.body() != null) {
                    libroActual = response.body();
                    configurarInterfaz();
                }
            }
            @Override
            public void onFailure(Call<BookEn> call, Throwable t) {
                Toast.makeText(ProximoLibro.this, "Error de conexión con el servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarInterfaz() {
        // Rellenar textos
        tvTituloDisplay.setText(libroActual.getTitulo());
        tvAutorDisplay.setText(libroActual.getAutor());
        etSubtitulo.setText(libroActual.getSubtitulo());
        etResumen.setText(libroActual.getResumen());

        // Configurar el desplegable de temas
        TemaLibro[] temas = TemaLibro.values();
        String[] nombresTemas = new String[temas.length];
        for (int i = 0; i < temas.length; i++) {
            nombresTemas[i] = temas[i].toString();
        }

        ArrayAdapter<String> adapterTemas = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, nombresTemas);
        etTema.setAdapter(adapterTemas);

        if (libroActual.getTema() != null) {
            etTema.setText(libroActual.getTema().toString(), false);
        }

        // Visibilidad del icono de información según disponibilidad de URL
        if (libroActual.getInfoUrl() == null || libroActual.getInfoUrl().isEmpty()) {
            btnInfoExterna.setVisibility(View.INVISIBLE);
        } else {
            btnInfoExterna.setVisibility(View.VISIBLE);
        }

        // Carga de la imagen de portada
        String urlImagen = "https://covers.openlibrary.org/b/id/" + libroActual.getCoverId() + "-M.jpg";
        Glide.with(this)
                .load(urlImagen)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(ivPortada);
    }

    private void actualizarInformacion() {
        if (libroActual == null) return;

        // Feedback visual al usuario
        btnActualizar.setEnabled(false);
        btnActualizar.setText("Guardando...");

        // Actualizar objeto local
        libroActual.setSubtitulo(etSubtitulo.getText().toString().trim());
        libroActual.setResumen(etResumen.getText().toString().trim());
        libroActual.setTema(libroActual.mapearTema(etTema.getText().toString()));

        // Enviar a la API
        apiService.anadirBook(libroActual).enqueue(new Callback<BookEn>() {
            @Override
            public void onResponse(Call<BookEn> call, Response<BookEn> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProximoLibro.this, "Cambios guardados con éxito", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    rehabilitarBoton();
                }
            }

            @Override
            public void onFailure(Call<BookEn> call, Throwable t) {
                rehabilitarBoton();
                Toast.makeText(ProximoLibro.this, "Error al guardar los datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rehabilitarBoton() {
        btnActualizar.setEnabled(true);
        btnActualizar.setText("Guardar Cambios");
    }
}