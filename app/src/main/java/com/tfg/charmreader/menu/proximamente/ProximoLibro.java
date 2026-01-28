package com.tfg.charmreader.menu.proximamente;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_ApiBook;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.BookEn;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProximoLibro extends AppCompatActivity {

    private ImageView ivPortada;
    private TextInputEditText etTitulo, etAutor, etTema, etResumen;
    private Button btnActualizar;
    private I_ApiBook apiService;
    private int idLibro;
    private BookEn libroActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proximo_libro); // Tu XML con ScrollView

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
                    rellenarFormularioYBloquearCampos();
                }
            }
            @Override
            public void onFailure(Call<BookEn> call, Throwable t) {
                Toast.makeText(ProximoLibro.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rellenarFormularioYBloquearCampos() {
        // 1. Rellenar los textos
        etTitulo.setText(libroActual.getTitulo());
        etAutor.setText(libroActual.getAutor());
        etTema.setText(libroActual.getTema());
        etResumen.setText(libroActual.getResumen());

        // 2. Lógica de bloqueo/desbloqueo según tus condiciones

        // El Título siempre suele venir de la API, pero por si acaso:
        etTitulo.setEnabled(libroActual.getTitulo() == null || libroActual.getTitulo().isEmpty());

        // Si el autor es "Desconocido" o nulo, se puede editar
        boolean autorEditable = libroActual.getAutor() == null || libroActual.getAutor().equals("Desconocido");
        etAutor.setEnabled(autorEditable);

        // Si el tema es "Sin tema" o nulo, se puede editar
        boolean temaEditable = libroActual.getTema() == null || libroActual.getTema().equals("Sin tema");
        etTema.setEnabled(temaEditable);

        // Si el resumen es "Sin resumen" o nulo, se puede editar
        boolean resumenEditable = libroActual.getResumen() == null || libroActual.getResumen().equals("Sin resumen");
        etResumen.setEnabled(resumenEditable);

        // Cargar imagen
        String urlImagen = "https://covers.openlibrary.org/b/id/" + libroActual.getCoverId() + "-M.jpg";
        Glide.with(this).load(urlImagen).placeholder(android.R.drawable.ic_menu_gallery).into(ivPortada);
    }

    private void actualizarInformacion() {
        if (libroActual == null) return;

        // Solo guardamos lo que hay en los campos (si estaban habilitados, se habrán cambiado)
        libroActual.setTitulo(etTitulo.getText().toString().trim());
        libroActual.setAutor(etAutor.getText().toString().trim());
        libroActual.setTema(etTema.getText().toString().trim());
        libroActual.setResumen(etResumen.getText().toString().trim());

        Log.d("DEBUG_SAVE", "ID del libro que se va a enviar: " + libroActual.getId());

        apiService.anadirBook(libroActual).enqueue(new Callback<BookEn>() {
            @Override
            public void onResponse(Call<BookEn> call, Response<BookEn> response) {
                if (response.isSuccessful()) {
                    // Loguea el cuerpo de la respuesta para ver si el ID devuelto es el mismo
                    Log.d("API_SUCCESS", "Libro devuelto por servidor ID: " + response.body().getId());
                    Toast.makeText(ProximoLibro.this, "Cambios guardados", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    // Si entra aquí, la API rechazó el guardado (Error 400, 500, etc.)
                    Log.e("API_ERROR", "Código: " + response.code() + " Mensaje: " + response.message());
                    Toast.makeText(ProximoLibro.this, "Error del servidor al guardar", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<BookEn> call, Throwable t) {
                Log.e("ProximamenteFragment", "Fallo en el almacenamiento de informacion: ");
                Toast.makeText(ProximoLibro.this, "Fallo al actualizar", Toast.LENGTH_SHORT).show();
            }
        });
    }
}