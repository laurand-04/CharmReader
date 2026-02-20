package com.tfg.charmreader.menu.priv.proximamente;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tfg.charmreader.R;
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

    private LinearLayout layoutEmpty;
    private ImageView ivEmptyIcon;
    private TextView tvEmptyTitle, tvEmptySubtitle;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable = null;

    private final I_ApiBook apiBook = API.getInstancia().create(I_ApiBook.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscador_api_externa);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        vincularVistas();
        configurarRecyclerView();

        findViewById(R.id.btnBackBuscador).setOnClickListener(v -> finish());
        configurarBuscador();
    }

    private void vincularVistas() {
        searchView = findViewById(R.id.searchViewAPI);
        recyclerView = findViewById(R.id.recyclerViewBook);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmptyBuscador);
        ivEmptyIcon = findViewById(R.id.ivEmptyIconBuscador);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitleBuscador);
        tvEmptySubtitle = findViewById(R.id.tvEmptySubtitleBuscador);
    }

    private void configurarRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookExtAdapter(new ArrayList<>(), this::guardarLibro);
        recyclerView.setAdapter(adapter);
    }

    private void configurarBuscador() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.trim().length() >= 3) buscarLibro(query.trim());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                String query = newText.trim();
                if (query.length() >= 3) {
                    searchRunnable = () -> buscarLibro(query);
                    searchHandler.postDelayed(searchRunnable, 800);
                } else if (query.isEmpty()) {
                    mostrarEstadoVacio(true, true);
                    adapter.updateData(new ArrayList<>());
                }
                return true;
            }
        });
    }

    private void buscarLibro(String query) {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        APIexterna.getLibroService().buscarLibroPorTitulo(query).enqueue(new Callback<BookResponse>() {
            @Override
            public void onResponse(Call<BookResponse> call, Response<BookResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Book> lista = response.body().getBooks();
                    if (lista != null && !lista.isEmpty()) {
                        mostrarEstadoVacio(false, false);
                        adapter.updateData(lista);
                    } else {
                        mostrarEstadoVacio(true, false);
                    }
                } else {
                    mostrarEstadoVacio(true, false);
                }
            }

            @Override
            public void onFailure(Call<BookResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                mostrarEstadoVacio(true, false);
            }
        });
    }

    private void mostrarEstadoVacio(boolean mostrar, boolean esInicio) {
        if (mostrar) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            if (esInicio) {
                ivEmptyIcon.setImageResource(R.drawable.ic_search);
                tvEmptyTitle.setText("Encuentra tu próxima lectura");
                tvEmptySubtitle.setText("Busca libros por título o autor en la biblioteca global de OpenLibrary.");
            } else {
                ivEmptyIcon.setImageResource(R.drawable.ic_libro);
                tvEmptyTitle.setText("Sin resultados");
                tvEmptySubtitle.setText("No hemos encontrado libros que coincidan. Prueba con otros términos.");
            }
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void guardarLibro(Book book) {
        // 1. Obtener ID de SharedPreferences de forma instantánea
        SharedPreferences prefs = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);

        if (idUsuario == -1) {
            Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // 2. Usamos el hilo solo para la subida (Retrofit .execute() es bloqueante)
        new Thread(() -> {
            try {
                BookEn bookEn = new BookEn(book, idUsuario);
                Response<BookEn> response = apiBook.anadirBook(bookEn).execute();

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        Toast.makeText(this, "Añadido a Deseos", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Error al guardar en el servidor", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                Log.e("Error_Buscador", "Excepción: " + e.getMessage());
            }
        }).start();
    }
}