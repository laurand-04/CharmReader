package com.tfg.charmreader.menu.priv.proximamente;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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

public class BuscadorAPIExterna extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private BookExtAdapter adapter;

    // Lógica para el retardo de búsqueda (Debounce)
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable = null;

    private final I_ApiBook apiBook = API.getInstancia().create(I_ApiBook.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscador_api_externa);

        // 1. Estética de la barra de estado (StatusBar Blanca)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        // 2. Inicialización de vistas
        searchView = findViewById(R.id.searchViewAPI);
        recyclerView = findViewById(R.id.recyclerViewBook);
        progressBar = findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 3. Configuración del botón Atrás (estilo Perfil)
        findViewById(R.id.btnBackBuscador).setOnClickListener(v -> finish());

        // 4. Configuración del Adaptador (API Externa)
        adapter = new BookExtAdapter(new ArrayList<>(), book -> {
            Log.d("OPEN_LIBRARY_DATA", "Seleccionado: " + book.getTitle());
            guardarLibro(book);
        });
        recyclerView.setAdapter(adapter);

        // 5. Configuración del Buscador (Logic Debounce 800ms)
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.trim().length() >= 3) {
                    buscarLibro(query.trim());
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Cancelar cualquier búsqueda programada si el usuario sigue escribiendo
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                String query = newText.trim();
                if (query.length() >= 3) {
                    // Programamos la ejecución para dentro de 800ms
                    searchRunnable = () -> buscarLibro(query);
                    searchHandler.postDelayed(searchRunnable, 800);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
                return true;
            }
        });
    }

    private void buscarLibro(String query) {
        // Mostramos el ProgressBar horizontal justo debajo del buscador
        progressBar.setVisibility(View.VISIBLE);

        APIexterna.getLibroService().buscarLibroPorTitulo(query).enqueue(new Callback<BookResponse>() {
            @Override
            public void onResponse(Call<BookResponse> call, Response<BookResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Book> lista = response.body().getBooks();
                    if (lista != null) {
                        adapter.updateData(lista);
                    }
                }
            }

            @Override
            public void onFailure(Call<BookResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("API_DEBUG", "Error en OpenLibrary: " + t.getMessage());
                Toast.makeText(BuscadorAPIExterna.this, "Error de conexión con OpenLibrary", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarLibro(Book book) {
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                int idUsuario = Utilidades.obtenerIdUsuarioDesdeAPI();
                // Transformamos el objeto de la API externa a nuestro objeto de BD interna
                BookEn bookEn = new BookEn(book, idUsuario);

                Response<BookEn> response = apiBook.anadirBook(bookEn).execute();

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        Toast.makeText(BuscadorAPIExterna.this, "Añadido a Deseos: " + bookEn.getTitulo(), Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    } else {
                        Log.e("DB_ERROR", "Error: " + response.code());
                        Toast.makeText(BuscadorAPIExterna.this, "Error al guardar en tu lista", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                Log.e("Error_Buscador", "Excepción: " + e.getMessage());
            }
        }).start();
    }
}