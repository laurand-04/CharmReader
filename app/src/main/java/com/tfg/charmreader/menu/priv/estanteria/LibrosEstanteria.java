package com.tfg.charmreader.menu.priv.estanteria;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
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

public class LibrosEstanteria extends AppCompatActivity {
    private RecyclerView rvLibros;
    private LibrosAdapter adapter;
    private I_ApiLibrosDeUsuario apiLibrosDeUsuario;
    private I_ApiLibro apiLibro;
    private int idEstanteria;
    private ActivityResultLauncher<Intent> launcherCargarLibro;
    private List<LibrosDeUsuario> listaLibrosUsuarioGlobal;
    private TextView tvCantidad;
    private LinearLayout layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_libros_estanteria);

        apiLibrosDeUsuario = API.getInstancia().create(I_ApiLibrosDeUsuario.class);
        apiLibro = API.getInstancia().create(I_ApiLibro.class);

        TextView tvTitulo = findViewById(R.id.tvTituloEstanteria);
        tvCantidad = findViewById(R.id.tvCantidadLibrosEstanteria);
        ShapeableImageView btnBack = findViewById(R.id.btnBack);
        View viewColor = findViewById(R.id.viewEstanteriaColor);
        MaterialCardView statusColorContainer = findViewById(R.id.statusColorContainer);
        layoutEmpty = findViewById(R.id.layoutEmptyLibrosEstanteria);

        SearchView searchView = findViewById(R.id.searchViewLibrosEst);
        FloatingActionButton fab = findViewById(R.id.fab_add_librosEstanteria);

        rvLibros = findViewById(R.id.recyclerLibrosEstanteria);
        rvLibros.setLayoutManager(new LinearLayoutManager(this));

        idEstanteria = getIntent().getIntExtra("idEstanteria", -1);
        String nombreEstanteria = getIntent().getStringExtra("Nombre");
        String colorPastelRecibido = getIntent().getStringExtra("Color");

        if (nombreEstanteria != null) {
            tvTitulo.setText(nombreEstanteria);
        }

        btnBack.setOnClickListener(v -> finish());

        if (colorPastelRecibido != null && !colorPastelRecibido.isEmpty()) {
            int colorFuerte = obtenerColorFuerte(colorPastelRecibido);
            viewColor.getBackground().setColorFilter(colorFuerte, PorterDuff.Mode.SRC_IN);
            statusColorContainer.setStrokeColor(ColorStateList.valueOf(colorFuerte));
            btnBack.setStrokeColor(ColorStateList.valueOf(colorFuerte));
            tvCantidad.setTextColor(colorFuerte);
            fab.setBackgroundTintList(ColorStateList.valueOf(colorFuerte));
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) adapter.filtrar(newText);
                return true;
            }
        });

        launcherCargarLibro = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        cargarDatos(idEstanteria);
                    }
                }
        );

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(LibrosEstanteria.this, CargarNuevoLibroEstanteria.class);
            intent.putExtra("idEstanteria", idEstanteria);
            launcherCargarLibro.launch(intent);
        });

        cargarDatos(idEstanteria);
    }

    private int obtenerColorFuerte(String pastel) {
        switch (pastel.toUpperCase()) {
            case "#F3E5F5": return Color.parseColor("#664FA4");
            case "#E3F2FD": return Color.parseColor("#1976D2");
            case "#E8F5E9": return Color.parseColor("#388E3C");
            case "#FFF3E0": return Color.parseColor("#F57C00");
            case "#FFEBEE": return Color.parseColor("#C2185B");
            default: return Color.parseColor("#664FA4");
        }
    }

    private void cargarDatos(int idEstanteriaConsultar) {
        if (idEstanteriaConsultar == -1) return;
        SharedPreferences prefs = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        int idUsuarioActual = prefs.getInt("idUsuario", -1);
        new Thread(() -> {
            try {
                Response<List<LibrosDeUsuario>> response = apiLibrosDeUsuario.obtenerLibrosDeEstanteria(idEstanteriaConsultar, idUsuarioActual).execute();

                if (response.isSuccessful() && response.body() != null) {
                    this.listaLibrosUsuarioGlobal = response.body();

                    if (this.listaLibrosUsuarioGlobal.isEmpty()) {
                        runOnUiThread(() -> mostrarEstadoVacio(true));
                        return;
                    }

                    List<Integer> idsLibros = new ArrayList<>();
                    for (LibrosDeUsuario item : this.listaLibrosUsuarioGlobal) {
                        idsLibros.add(item.getId().getIdL());
                    }

                    Response<List<Libro>> responseLibros = apiLibro.obtenerLibrosPorIds(idsLibros).execute();

                    if (responseLibros.isSuccessful() && responseLibros.body() != null) {
                        List<Libro> listaLibrosFinal = responseLibros.body();

                        runOnUiThread(() -> {
                            mostrarEstadoVacio(false);
                            int total = listaLibrosFinal.size();
                            tvCantidad.setText((total == 1) ? "1 libro" : total + " libros");

                            adapter = new LibrosAdapter(listaLibrosFinal, libro -> {
                                if (listaLibrosUsuarioGlobal != null) {
                                    for (LibrosDeUsuario ldu : listaLibrosUsuarioGlobal) {
                                        if (ldu.getId().getIdL() == libro.getId()) {
                                            Intent intent = new Intent(LibrosEstanteria.this, ValoracionLibro.class);
                                            intent.putExtra("URL_LIBRO", ldu.getRuta());
                                            intent.putExtra("idL", ldu.getId().getIdL());
                                            intent.putExtra("idU", ldu.getId().getIdU());
                                            startActivity(intent);
                                            return;
                                        }
                                    }
                                }
                            });
                            rvLibros.setAdapter(adapter);
                        });
                    }
                } else {
                    runOnUiThread(() -> mostrarEstadoVacio(true));
                }
            } catch (IOException e) {
                Log.e("LibrosEstanteria", "Error de red: " + e.getMessage());
                runOnUiThread(() -> mostrarEstadoVacio(true));
            }
        }).start();
    }

    private void mostrarEstadoVacio(boolean estaVacio) {
        if (estaVacio) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvLibros.setVisibility(View.GONE);
            tvCantidad.setText("0 libros");
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvLibros.setVisibility(View.VISIBLE);
        }
    }
}