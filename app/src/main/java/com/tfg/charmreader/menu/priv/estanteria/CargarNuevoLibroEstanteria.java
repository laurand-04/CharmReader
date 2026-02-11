package com.tfg.charmreader.menu.priv.estanteria;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tfg.charmreader.R;
import com.tfg.charmreader.Utilidades;
import com.tfg.charmreader.interfacesAPI.I_ApiLibro;
import com.tfg.charmreader.interfacesAPI.I_ApiLibrosDeUsuario;
import com.tfg.charmreader.menu.priv.adapterRecyclerView.LibrosAdapter;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.Libro;
import com.tfg.charmreader.objetosBD.LibrosDeUsuario;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Response;

public class CargarNuevoLibroEstanteria extends AppCompatActivity {
    private RecyclerView rvLibros;
    private LibrosAdapter adapter;
    private I_ApiLibrosDeUsuario apiLibrosDeUsuario;
    private I_ApiLibro apiLibro;
    private int idEstanteriaDestino;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_libro_estanteria);

        // 1. Inicializar vistas y APIs
        apiLibrosDeUsuario = API.getInstancia().create(I_ApiLibrosDeUsuario.class);
        apiLibro = API.getInstancia().create(I_ApiLibro.class);

        idEstanteriaDestino = getIntent().getIntExtra("idEstanteria", -1);

        rvLibros = findViewById(R.id.recyclerCargarNuevoLibroEstanteria);
        rvLibros.setLayoutManager(new LinearLayoutManager(this));

        searchView = findViewById(R.id.searchViewAddLibro);

        // Botón cerrar
        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        // 2. Configurar buscador
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) adapter.filtrar(newText);
                return true;
            }
        });

        // 3. Cargar todos los libros del usuario (idEstanteria 0 o general)
        cargarTodosLosLibrosDelUsuario();
    }

    private void cargarTodosLosLibrosDelUsuario() {
        new Thread(() -> {
            try {
                // Obtenemos los libros del usuario (usualmente idEstanteria 0 es la principal)
                Response<List<LibrosDeUsuario>> response = apiLibrosDeUsuario.obtenerLibrosDeEstanteria(0).execute();

                if (response.isSuccessful() && response.body() != null) {
                    List<LibrosDeUsuario> relaciones = response.body();
                    List<Integer> idsLibros = new ArrayList<>();
                    for (LibrosDeUsuario item : relaciones) {
                        idsLibros.add(item.getId().getIdL());
                    }

                    Response<List<Libro>> responseLibros = apiLibro.obtenerLibrosPorIds(idsLibros).execute();

                    if (responseLibros.isSuccessful() && responseLibros.body() != null) {
                        List<Libro> listaFinal = responseLibros.body();
                        runOnUiThread(() -> {
                            // Al hacer clic, añadimos a la estantería
                            adapter = new LibrosAdapter(listaFinal, libro -> anadirLibroAEstanteria(libro));
                            rvLibros.setAdapter(adapter);
                        });
                    }
                }
            } catch (IOException e) {
                Log.e("DEBUG_APP", "Error: " + e.getMessage());
            }
        }).start();
    }

    private void anadirLibroAEstanteria(Libro libro) {
        new Thread(() -> {
            try {
                int idUsuario = Utilidades.obtenerIdUsuarioDesdeAPI();
                // Asignamos el libro a la estantería que recibimos por Intent
                Response<Boolean> response = apiLibrosDeUsuario.asignarLibroAEstanteria(idUsuario, libro.getId(), idEstanteriaDestino).execute();

                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        setResult(RESULT_OK);
                        finish();
                    });
                }
            } catch (IOException e) {
                Log.e("ERROR", "Error al asignar", e);
            }
        }).start();
    }
}