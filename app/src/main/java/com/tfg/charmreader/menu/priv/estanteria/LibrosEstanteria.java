package com.tfg.charmreader.menu.priv.estanteria;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class LibrosEstanteria extends AppCompatActivity {
    private RecyclerView rvLibros;
    private LibrosAdapter adapter;
    private I_ApiLibrosDeUsuario apiLibrosDeUsuario;
    private I_ApiLibro apiLibro;
    private int idEstanteria;
    private ActivityResultLauncher<Intent> launcherCargarLibro;

    // Guardamos las relaciones para saber qué URL corresponde a qué ID de libro
    private List<LibrosDeUsuario> listaLibrosUsuarioGlobal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_libros_estanteria);

        // 1. INICIALIZACIÓN CRÍTICA: Primero las APIs para evitar el NullPointerException
        apiLibrosDeUsuario = API.getInstancia().create(I_ApiLibrosDeUsuario.class);
        apiLibro = API.getInstancia().create(I_ApiLibro.class);

        // 2. Enlazar vistas y configurar RecyclerView
        TextView tvTitulo = findViewById(R.id.tvTituloEstanteria);
        rvLibros = findViewById(R.id.recyclerLibrosEstanteria);
        rvLibros.setLayoutManager(new LinearLayoutManager(this));

        // 3. Obtener datos del Intent
        idEstanteria = getIntent().getIntExtra("idEstanteria", -1);
        String nombreEstanteria = getIntent().getStringExtra("Nombre");

        if (nombreEstanteria != null) {
            tvTitulo.setText("Libros de: " + nombreEstanteria);
        }

        // 4. Configurar el Launcher para refrescar la lista al volver de añadir libros
        launcherCargarLibro = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        cargarDatos(idEstanteria);
                    }
                }
        );

        // 5. Configurar el FAB (CORREGIDO: Solo una vez y a la Activity correcta)
        FloatingActionButton fab = findViewById(R.id.fab_add_librosEstanteria);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                Intent intent = new Intent(LibrosEstanteria.this, CargarNuevoLibroEstanteria.class);
                intent.putExtra("idEstanteria", idEstanteria);
                launcherCargarLibro.launch(intent);
            });
        }

        // 6. Cargar los datos
        cargarDatos(idEstanteria);
    }

    private void cargarDatos(int idEstanteriaConsultar) {
        if (idEstanteriaConsultar == -1) {
            Log.e("LibrosEstanteria", "ID de estantería no válido");
            return;
        }

        new Thread(() -> {
            try {
                // Petición de las relaciones libro-usuario de esta estantería
                Response<List<LibrosDeUsuario>> response = apiLibrosDeUsuario.obtenerLibrosDeEstanteria(idEstanteriaConsultar).execute();

                if (response.isSuccessful() && response.body() != null) {
                    this.listaLibrosUsuarioGlobal = response.body();

                    if (this.listaLibrosUsuarioGlobal.isEmpty()) {
                        runOnUiThread(() -> {
                            adapter = new LibrosAdapter(new ArrayList<>(), null);
                            rvLibros.setAdapter(adapter);
                        });
                        return;
                    }

                    // Extraer IDs para obtener los detalles de los libros (Título, autor, etc)
                    List<Integer> idsLibros = new ArrayList<>();
                    for (LibrosDeUsuario item : this.listaLibrosUsuarioGlobal) {
                        idsLibros.add(item.getId().getIdL());
                    }

                    Response<List<Libro>> responseLibros = apiLibro.obtenerLibrosPorIds(idsLibros).execute();

                    if (responseLibros.isSuccessful() && responseLibros.body() != null) {
                        List<Libro> listaLibrosFinal = responseLibros.body();

                        runOnUiThread(() -> {
                            // Configuramos el adaptador con la lógica de click para abrir la valoración/visor
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
                }
            } catch (IOException e) {
                Log.e("LibrosEstanteria", "Error de red: " + e.getMessage());
            }
        }).start();
    }
}