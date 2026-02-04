package com.tfg.charmreader.menu.priv.proximamente;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.tfg.charmreader.R;
import com.tfg.charmreader.Utilidades;
import com.tfg.charmreader.interfacesAPI.I_ApiBook;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.BookEn;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProximoLibro extends AppCompatActivity {

    private ImageView ivPortada;
    private TextInputEditText etTitulo, etAutor, etResumen, etSubtitulo;
    private AutoCompleteTextView etTema;
    private Button btnActualizar;
    private I_ApiBook apiService;
    private int idLibro;
    private BookEn libroActual;

    // Movido fuera o asegúrate de que coincida con el de tu backend
    public enum TemaLibro {
        AVENTURAS, CIENCIA_FICCION, DRAMA, FANTASIA,
        HISTORICA, HUMOR, POLICIACA, ROMANCE,
        SUSPENSE, TERROR, INFANTIL, JUVENIL,
        BIOGRAFIA, AUTOAYUDA, ENSAYO, OTRO
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proximo_libro);

        idLibro = getIntent().getIntExtra("idLibro", -1);
        inicializarVistas();
        apiService = API.getInstancia().create(I_ApiBook.class);

        if (idLibro != -1) {
            cargarDatosLibro();
        }

        btnActualizar.setOnClickListener(v -> actualizarInformacion());
    }

    private void inicializarVistas() {
        ivPortada = findViewById(R.id.ivDetallePortada);
        etTitulo = findViewById(R.id.etDetalleTitulo);
        etAutor = findViewById(R.id.etDetalleAutor);
        etSubtitulo = findViewById(R.id.etDetalleSubtitulo);
        etTema = findViewById(R.id.etDetalleTema);
        etResumen = findViewById(R.id.etDetalleResumen);
        btnActualizar = findViewById(R.id.btnActualizarLibro);
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
            @Override public void onFailure(Call<BookEn> call, Throwable t) {
                Toast.makeText(ProximoLibro.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarInterfaz() {
        etTitulo.setText(libroActual.getTitulo());
        etAutor.setText(libroActual.getAutor());
        etSubtitulo.setText(libroActual.getSubtitulo());
        etResumen.setText(libroActual.getResumen());

        // --- CONFIGURACIÓN DEL ADAPTADOR ---
        TemaLibro[] temas = TemaLibro.values();
        String[] nombresTemas = new String[temas.length];
        for (int i = 0; i < temas.length; i++) {
            nombresTemas[i] = temas[i].toString();
        }

        // Cambia R.layout.list_item por android.R.layout.simple_dropdown_item_1line
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, nombresTemas);

        etTema.setAdapter(adapter);

        // --- SOLUCIÓN AL PROBLEMA DEL FILTRO ---
        // Al poner filter: false, evitamos que AutoCompleteTextView esconda el resto de opciones
        if (libroActual.getTema() != null) {
            etTema.setText(libroActual.getTema().toString(), false);
        }

        String urlImagen = "https://covers.openlibrary.org/b/id/" + libroActual.getCoverId() + "-M.jpg";
        Glide.with(this).load(urlImagen).placeholder(android.R.drawable.ic_menu_gallery).into(ivPortada);
    }

    private void actualizarInformacion() {
        if (libroActual == null) return;

        libroActual.setSubtitulo(etSubtitulo.getText().toString().trim());
        libroActual.setResumen(etResumen.getText().toString().trim());

        String temaSeleccionado = etTema.getText().toString();
        // Asegúrate de que mapearTema en tu objeto BookEn maneje bien los Strings
        libroActual.setTema(libroActual.mapearTema(temaSeleccionado));

        apiService.anadirBook(libroActual).enqueue(new Callback<BookEn>() {
            @Override
            public void onResponse(Call<BookEn> call, Response<BookEn> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProximoLibro.this, "Libro actualizado", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }
            }
            @Override public void onFailure(Call<BookEn> call, Throwable t) {
                Toast.makeText(ProximoLibro.this, "Fallo al guardar", Toast.LENGTH_SHORT).show();
            }
        });
    }
}