package com.tfg.charmreader.menu.publ.misGrupos.creados;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

    private EditText etQuery;
    private RecyclerView recyclerView;
    private BookExtAdapter adapter;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable = null;

    private final I_ApiBook apiBook = API.getInstancia().create(I_ApiBook.class);
    private final I_APICatalogo apiCatalogo = API.getInstancia().create(I_APICatalogo.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Nota: Asegúrate de que el layout activity_buscador_api_externa tenga los IDs correctos
        setContentView(R.layout.activity_buscador_api_externa);

        etQuery = findViewById(R.id.searchViewAPI);
        recyclerView = findViewById(R.id.recyclerViewBook);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BookExtAdapter(new ArrayList<>(), this::guardarLibroYPropuesta);

        recyclerView.setAdapter(adapter);

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

    private void guardarLibroYPropuesta(Book book) {
        // 1. Obtener IDs necesarios (Grupo del intent e Usuario de SharedPreferences)
        int idGrupo = getIntent().getIntExtra("idGrupo", -1);

        SharedPreferences prefs = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);

        if (idUsuario == -1 || idGrupo == -1) {
            Toast.makeText(this, "Error de sesión o de grupo", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Proceso de red en hilo secundario
        new Thread(() -> {
            try {
                // Mapear Book externo a tu objeto de BD
                BookEn bookEn = new BookEn(book, idUsuario);

                // PASO A: Guardar el libro en la tabla 'books' (u obtenerlo si ya existe)
                Response<BookEn> responseLibro = apiBook.anadirBook(bookEn).execute();

                if (responseLibro.isSuccessful() && responseLibro.body() != null) {
                    int idLibroGenerado = responseLibro.body().getId();

                    // PASO B: Crear la propuesta en el catálogo del grupo
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
                    } else {
                        Log.e("API_ERROR", "Error al añadir al catálogo: " + responseCat.code());
                    }
                }
            } catch (Exception e) {
                Log.e("API_EXCEPTION", "Error en el guardado: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "Error al procesar el libro", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void buscarLibro(String query) {
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
                Log.e("API_DEBUG", "Fallo en búsqueda: " + t.getMessage());
            }
        });
    }
}