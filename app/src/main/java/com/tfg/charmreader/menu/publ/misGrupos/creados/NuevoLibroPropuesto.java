package com.tfg.charmreader.menu.publ.misGrupos.creados;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_APICatalogo;
import com.tfg.charmreader.interfacesAPI.I_ApiBook;
import com.tfg.charmreader.menu.priv.adapterRecyclerView.BookExtAdapter;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.APIexterna;
import com.tfg.charmreader.objetosBD.Book;
import com.tfg.charmreader.objetosBD.BookEn;
import com.tfg.charmreader.objetosBD.BookResponse;
import com.tfg.charmreader.objetosBD.CatalogoLectura;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NuevoLibroPropuesto extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView recyclerView;
    private BookExtAdapter adapter;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable = null;

    private final I_ApiBook apiBook = API.getInstancia().create(I_ApiBook.class);
    private final I_APICatalogo apiCatalogo = API.getInstancia().create(I_APICatalogo.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscador_api_externa);

        // Vincular vistas según tus IDs del XML
        searchView = findViewById(R.id.searchViewAPI);
        recyclerView = findViewById(R.id.recyclerViewBook);
        layoutEmpty = findViewById(R.id.layoutEmptyBuscador);
        progressBar = findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookExtAdapter(new ArrayList<>(), this::guardarLibroYPropuesta);
        recyclerView.setAdapter(adapter);

        // Configuración del SearchView
        searchView.setIconifiedByDefault(false); // Forzar a que esté expandido
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.trim().length() >= 3) {
                    buscarLibro(query.trim());
                }
                searchView.clearFocus(); // Cerrar teclado al buscar
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                if (newText.trim().length() >= 3) {
                    searchRunnable = () -> buscarLibro(newText.trim());
                    searchHandler.postDelayed(searchRunnable, 800);
                } else if (newText.trim().isEmpty()) {
                    // Si borra el texto, volvemos al estado vacío
                    mostrarEstado(true);
                }
                return true;
            }
        });

        findViewById(R.id.btnBackBuscador).setOnClickListener(v -> finish());
    }

    private void buscarLibro(String query) {
        runOnUiThread(() -> {
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        });

        APIexterna.getLibroService().buscarLibroPorTitulo(query).enqueue(new Callback<BookResponse>() {
            @Override
            public void onResponse(Call<BookResponse> call, Response<BookResponse> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<Book> lista = response.body().getBooks();
                    if (lista != null && !lista.isEmpty()) {
                        mostrarEstado(false);
                        adapter.updateData(lista);
                    } else {
                        mostrarEstado(true);
                    }
                } else {
                    mostrarEstado(true);
                }
            }

            @Override
            public void onFailure(Call<BookResponse> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.e("API_DEBUG", "Fallo: " + t.getMessage());
                mostrarEstado(true);
            }
        });
    }

    // Método para alternar entre la lista y el mensaje de "vacío"
    private void mostrarEstado(boolean isEmpty) {
        if (isEmpty) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void guardarLibroYPropuesta(Book book) {
        int idGrupo = getIntent().getIntExtra("idGrupo", -1);
        SharedPreferences prefs = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);

        if (idUsuario == -1 || idGrupo == -1) {
            Toast.makeText(this, "Error de sesión o de grupo", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                BookEn bookEn = new BookEn(book, idUsuario, false);
                Response<BookEn> responseLibro = apiBook.anadirBook(bookEn).execute();

                if (responseLibro.isSuccessful() && responseLibro.body() != null) {
                    int idLibroGenerado = responseLibro.body().getId();

                    CatalogoLectura propuesta = new CatalogoLectura();
                    propuesta.setIdGrupo(idGrupo);
                    propuesta.setIdBook(idLibroGenerado);
                    propuesta.setEstado(CatalogoLectura.EstadoLectura.PROPUESTO);

                    Response<CatalogoLectura> responseCat = apiCatalogo.añadirLibro(propuesta).execute();

                    if (responseCat.isSuccessful()) {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "¡Libro propuesto al grupo!", Toast.LENGTH_SHORT).show();
                            setResult(Activity.RESULT_OK);
                            finish();
                        });
                    }
                }
            } catch (Exception e) {
                Log.e("API_EXCEPTION", "Error: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "Error al procesar", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}