package com.tfg.charmreader.menu.priv.estanteria;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tfg.charmreader.R;
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
    private LinearLayout layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_libro_estanteria);

        apiLibrosDeUsuario = API.getInstancia().create(I_ApiLibrosDeUsuario.class);
        apiLibro = API.getInstancia().create(I_ApiLibro.class);
        idEstanteriaDestino = getIntent().getIntExtra("idEstanteria", -1);

        rvLibros = findViewById(R.id.recyclerCargarNuevoLibroEstanteria);
        layoutEmpty = findViewById(R.id.layoutEmptyAddLibro);
        rvLibros.setLayoutManager(new LinearLayoutManager(this));
        searchView = findViewById(R.id.searchViewAddLibro);

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) adapter.filtrar(newText);
                return true;
            }
        });

        cargarTodosLosLibrosDelUsuario();
    }

    private void cargarTodosLosLibrosDelUsuario() {
        new Thread(() -> {
            try {
                SharedPreferences prefs = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
                int idUsuarioActual = prefs.getInt("idUsuario", -1);

                if (idUsuarioActual == -1) return;

                // Obtenemos los libros del usuario en la estantería 0
                Response<List<LibrosDeUsuario>> response = apiLibrosDeUsuario
                        .obtenerLibrosDeEstanteria(0, idUsuarioActual)
                        .execute();

                // Si la respuesta es exitosa pero vacía, o no exitosa
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<LibrosDeUsuario> relaciones = response.body();

                    List<Integer> idsLibros = new ArrayList<>();
                    for (LibrosDeUsuario item : relaciones) {
                        idsLibros.add(item.getId().getIdL());
                    }

                    Response<List<Libro>> responseLibros = apiLibro.obtenerLibrosPorIds(idsLibros).execute();

                    if (responseLibros.isSuccessful() && responseLibros.body() != null && !responseLibros.body().isEmpty()) {
                        List<Libro> listaFinal = responseLibros.body();
                        runOnUiThread(() -> {
                            layoutEmpty.setVisibility(View.GONE);
                            rvLibros.setVisibility(View.VISIBLE);
                            adapter = new LibrosAdapter(listaFinal, libro -> anadirLibroAEstanteria(libro));
                            rvLibros.setAdapter(adapter);
                        });
                    } else {
                        mostrarVacio();
                    }
                } else {
                    mostrarVacio();
                }
            } catch (IOException e) {
                Log.e("DEBUG_APP", "Error: " + e.getMessage());
                mostrarVacio();
            }
        }).start();
    }

    private void mostrarVacio() {
        runOnUiThread(() -> {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvLibros.setVisibility(View.GONE);
        });
    }

    private void anadirLibroAEstanteria(Libro libro) {
        new Thread(() -> {
            try {
                SharedPreferences prefs = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
                int idUsuario = prefs.getInt("idUsuario", -1);

                Response<Boolean> response = apiLibrosDeUsuario
                        .asignarLibroAEstanteria(idUsuario, libro.getId(), idEstanteriaDestino)
                        .execute();

                if (response.isSuccessful() && response.body() != null && response.body()) {
                    runOnUiThread(() -> {
                        setResult(RESULT_OK);
                        finish();
                    });
                }
            } catch (IOException e) {
                Log.e("ERROR", "Error al asignar libro", e);
            }
        }).start();
    }
}