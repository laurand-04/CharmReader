package com.tfg.charmreader.menu.estanteria;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tfg.charmreader.R;
import com.tfg.charmreader.Utilidades;
import com.tfg.charmreader.interfacesAPI.I_ApiLibro;
import com.tfg.charmreader.interfacesAPI.I_ApiLibrosDeUsuario;
import com.tfg.charmreader.menu.adapterRecyclerView.LibrosAdapter;
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
    private int idEstanteria;
    private ActivityResultLauncher<Intent> launcherCargarLibro;

    // Guardamos las relaciones para saber qué URL corresponde a qué ID de libro
    private List<LibrosDeUsuario> listaLibrosUsuarioGlobal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // LOG ANTES DE TODO
        Log.d("DEBUG_APP", "--- INICIANDO ONCREATE ---");

        try {
            setContentView(R.layout.activity_nuevo_libro_estanteria);

            rvLibros = findViewById(R.id.recyclerCargarNuevoLibroEstanteria);
            if (rvLibros == null) {
                Log.e("DEBUG_APP", "ERROR: El RecyclerView es NULL. Revisa el ID en el XML.");
            } else {
                rvLibros.setLayoutManager(new LinearLayoutManager(this));
                Log.d("DEBUG_APP", "RecyclerView configurado correctamente");
            }

            apiLibrosDeUsuario = API.getInstancia().create(I_ApiLibrosDeUsuario.class);
            apiLibro = API.getInstancia().create(I_ApiLibro.class);

            idEstanteria = getIntent().getIntExtra("idEstanteria", -1);
            Log.d("DEBUG_APP", "ID Estantería recibido del intent: " + idEstanteria);

            // FORZAMOS LA LLAMADA
            Log.d("DEBUG_APP", "Llamando a cargarDatos(0) ahora mismo...");
            cargarDatos(0);

        } catch (Exception e) {
            Log.e("DEBUG_APP", "CRASH EN ONCREATE: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cargarDatos(int idEstanteriaConsultar) {
        // 1. Log inmediatamente al llamar al método
        Log.d("DEBUG_APP", "PASO 1: Llamando a cargarDatos con ID: " + idEstanteriaConsultar);

        new Thread(() -> {
            try {
                Log.d("DEBUG_APP", "PASO 2: Dentro del hilo secundario");

                // 2. Ejecutar petición
                Response<List<LibrosDeUsuario>> response = apiLibrosDeUsuario
                        .obtenerLibrosDeEstanteria(idEstanteriaConsultar)
                        .execute();

                if (response.isSuccessful() && response.body() != null) {
                    List<LibrosDeUsuario> listaRelaciones = response.body();
                    Log.d("DEBUG_APP", "PASO 3: Respuesta exitosa. Tamaño: " + listaRelaciones.size());

                    if (listaRelaciones.isEmpty()) {
                        Log.w("DEBUG_APP", "OJO: La estantería " + idEstanteriaConsultar + " no tiene libros vinculados.");
                        return;
                    }

                    List<Integer> idsLibros = new ArrayList<>();
                    for (LibrosDeUsuario item : listaRelaciones) {
                        idsLibros.add(item.getId().getIdL());
                    }

                    // 3. Petición de detalles
                    Log.d("DEBUG_APP", "PASO 4: Solicitando detalles de " + idsLibros.size() + " libros");
                    Response<List<Libro>> responseLibros = apiLibro.obtenerLibrosPorIds(idsLibros).execute();

                    if (responseLibros.isSuccessful() && responseLibros.body() != null) {
                        List<Libro> listaFinal = responseLibros.body();
                        Log.d("DEBUG_APP", "PASO 5: Libros recibidos: " + listaFinal.size());

                        runOnUiThread(() -> {
                            adapter = new LibrosAdapter(listaFinal, libro -> anadirLibroAEstanteriaNueva(libro));
                            rvLibros.setAdapter(adapter);
                            Log.d("DEBUG_APP", "PASO 6: Adaptador configurado en UI");
                        });
                    } else {
                        Log.e("DEBUG_APP", "ERROR PASO 4: Error en API Libros. Código: " + responseLibros.code());
                    }

                } else {
                    Log.e("DEBUG_APP", "ERROR PASO 2: Error en API Relaciones. Código: " + response.code());
                }
            } catch (Exception e) {
                // CAPTURA CUALQUIER ERROR (Network, NullPointer, etc.)
                Log.e("DEBUG_APP", "EXCEPCIÓN CRÍTICA: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void anadirLibroAEstanteriaNueva(Libro libro) {
        new Thread(() -> {
            try {
                int idUsuario = Utilidades.obtenerIdUsuarioDesdeAPI();
                // Ejecutamos la asignación a la estantería de destino (idEstanteria de la Activity)
                Response<Boolean> responseAnadir = apiLibrosDeUsuario.asignarLibroAEstanteria(idUsuario, libro.getId(), this.idEstanteria).execute();

                if (responseAnadir.isSuccessful()) {
                    runOnUiThread(() -> {
                        // Si tuvo éxito, cerramos y avisamos que todo ok
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
