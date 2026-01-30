package com.tfg.charmreader.menu.priv.proximamente;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log; // Añadido para debugging
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tfg.charmreader.R;
import com.tfg.charmreader.Utilidades;
import com.tfg.charmreader.interfacesAPI.I_ApiBook;
import com.tfg.charmreader.menu.priv.adapterRecyclerView.BookExtAdapter;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.APIexterna;
import com.tfg.charmreader.objetosBD.Book;
import com.tfg.charmreader.objetosBD.BookEn;
import com.tfg.charmreader.objetosBD.BookResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.widget.Toast; // Para avisar al usuario

public class BuscadorAPIExterna extends AppCompatActivity {

    private EditText etQuery;
    private RecyclerView recyclerView;
    private BookExtAdapter adapter;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable = null;

    private I_ApiBook apiBook = API.getInstancia().create(I_ApiBook.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscador_api_externa);

        etQuery = findViewById(R.id.etQuery);
        recyclerView = findViewById(R.id.recyclerViewBook);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // --- CAMBIO CLAVE: Implementamos la interfaz aquí ---
        adapter = new BookExtAdapter(new ArrayList<>(), new BookExtAdapter.OnBookClickListener() {
            @Override
            public void onBookClick(Book book) {
                Log.d("OPEN_LIBRARY_DATA", "========== INICIO INFO LIBRO ==========");
                Log.d("OPEN_LIBRARY_DATA", "Título: " + book.getTitle());
                Log.d("OPEN_LIBRARY_DATA", "Subtítulo: " + book.getSubtitle());
                Log.d("OPEN_LIBRARY_DATA", "Año Publicación: " + book.getPublishYear());
                Log.d("OPEN_LIBRARY_DATA", "ID Portada: " + (book.getCoverId() != null ? book.getCoverId() : "N/A"));

// Información que viene en listas (usando tus métodos de conversión)
                Log.d("OPEN_LIBRARY_DATA", "Autores (String): " + book.getFirstAuthor());
                Log.d("OPEN_LIBRARY_DATA", "Temas (String): " + book.getSubjects());
                Log.d("OPEN_LIBRARY_DATA", "Resumen/Primera Frase: " + book.getFirstSentence());
                Log.d("OPEN_LIBRARY_DATA", "=========== FIN INFO LIBRO ===========");
                guardarLibro(book);
            }
        });

        recyclerView.setAdapter(adapter);

        // ... resto de tu código del TextWatcher (se mantiene igual) ...
        etQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.length() >= 3) {
                    searchRunnable = () -> buscarLibro(query);
                    searchHandler.postDelayed(searchRunnable, 800);
                }
            }
        });
    }

    // --- NUEVO MÉTODO: Aquí guardas la info ---
    private void guardarLibro(Book book) {
        // LOG DE PRUEBA: Si aquí sale null, el problema es la clase Book
        Log.d("FLUJO_DATOS", "ID Portada en objeto Book original: " + book.getCoverId());
        new Thread(() -> {
            try {
                int idUsuario = Utilidades.obtenerIdUsuarioDesdeAPI();
                // Asegúrate de que este método exista en tu clase Book y transforme las listas a String
                BookEn bookEn = new BookEn(book, idUsuario);
                // LOG DE PRUEBA: Si aquí sale null, el problema es el constructor de BookEn
                Log.d("FLUJO_DATOS", "ID Portada en objeto BookEn: " + bookEn.getCoverId());

                Response<BookEn> response = apiBook.anadirBook(bookEn).execute();

                if (response.isSuccessful()) {
                    Log.d("DB_SUCCESS", "Libro guardado: " + response.code());
                    runOnUiThread(() -> {
                        Toast.makeText(BuscadorAPIExterna.this, "Guardado: " + bookEn.getTitulo(), Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    });
                } else {
                    Log.e("DB_ERROR", "Error en servidor: " + response.code());
                }
            } catch (Exception e) {
                Log.e("Error BuscadorAPIExterna", "Error en guardarLibro: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void buscarLibro(String query) {
        // ... tu código de Retrofit (se mantiene igual) ...
        APIexterna.getLibroService().buscarLibroPorTitulo(query).enqueue(new Callback<BookResponse>() {
            @Override
            public void onResponse(Call<BookResponse> call, Response<BookResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Book> lista = response.body().getBooks();
                    if (lista != null) {
                        adapter.updateData(lista);
                    }
                }
            }
            @Override
            public void onFailure(Call<BookResponse> call, Throwable t) {
                Log.e("API_DEBUG", "Error: " + t.getMessage());
            }
        });
    }
}