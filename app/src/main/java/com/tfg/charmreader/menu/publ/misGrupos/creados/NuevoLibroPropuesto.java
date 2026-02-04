package com.tfg.charmreader.menu.publ.misGrupos.creados;

import android.app.Activity;
import android.content.Intent;
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
import com.tfg.charmreader.Utilidades;
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
        setContentView(R.layout.activity_buscador_api_externa);

        etQuery = findViewById(R.id.etQuery);
        recyclerView = findViewById(R.id.recyclerViewBook);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BookExtAdapter(new ArrayList<>(), new BookExtAdapter.OnBookClickListener() {
            @Override
            public void onBookClick(Book book) {
                // Al hacer clic, iniciamos el proceso de guardado doble
                guardarLibroYPropuesta(book);
            }
        });

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
        // Obtenemos el ID del grupo que viene de ManejoGrupo
        int idGrupo = getIntent().getIntExtra("idGrupo", -1);

        new Thread(() -> {
            try {
                // 1. Obtener ID del usuario logueado (Sincrónico dentro del hilo)
                int idUsuario = Utilidades.obtenerIdUsuarioDesdeAPI();

                // 2. Mapear Book (Externo) a BookEn (Tu BD)
                BookEn bookEn = new BookEn(book, idUsuario);

                // 3. PASO A: Guardar el libro en la tabla 'books'
                Response<BookEn> responseLibro = apiBook.anadirBook(bookEn).execute();

                if (responseLibro.isSuccessful() && responseLibro.body() != null) {
                    BookEn libroGuardado = responseLibro.body();
                    int idLibroGenerado = libroGuardado.getId();

                    Log.d("API_FLOW", "Libro guardado con ID: " + idLibroGenerado);

                    // 4. PASO B: Si tenemos un ID de grupo válido, crear la propuesta en el catálogo
                    if (idGrupo != -1) {
                        CatalogoLectura propuesta = new CatalogoLectura();
                        propuesta.setIdGrupo(idGrupo);
                        propuesta.setIdBook(idLibroGenerado);
                        propuesta.setEstado(CatalogoLectura.EstadoLectura.PROPUESTO);

                        Response<CatalogoLectura> responseCat = apiCatalogo.añadirLibro(propuesta).execute();

                        if (responseCat.isSuccessful()) {
                            Log.d("API_FLOW", "Propuesta añadida al catálogo del grupo " + idGrupo);
                        }
                    }

                    // 5. Finalizar y avisar al usuario
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Libro propuesto con éxito", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    });
                } else {
                    Log.e("API_ERROR", "Error al guardar libro: " + responseLibro.code());
                }

            } catch (Exception e) {
                Log.e("API_EXCEPTION", "Error en el proceso de guardado: " + e.getMessage());
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
