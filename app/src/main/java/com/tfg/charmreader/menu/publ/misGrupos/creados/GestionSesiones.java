package com.tfg.charmreader.menu.publ.misGrupos.creados;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_ApiSesion;
import com.tfg.charmreader.menu.publ.adapterReclyclerView.SesionAdapter;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.Sesion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GestionSesiones extends AppCompatActivity {

    private int idGrupo;
    private RecyclerView rvSesiones;
    private LinearLayout layoutEmpty;
    private FloatingActionButton fabNueva;
    private ShapeableImageView btnBack;
    private SesionAdapter adapter;
    private List<Sesion> listaSesiones = new ArrayList<>();

    private final I_ApiSesion apiSesion = API.getInstancia().create(I_ApiSesion.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_sesiones);

        idGrupo = getIntent().getIntExtra("idGrupo", -1);
        if (idGrupo == -1) { finish(); return; }

        vincularVistas();
        configurarRecyclerView();

        btnBack.setOnClickListener(v -> finish());
        fabNueva.setOnClickListener(v -> {
            Intent intent = new Intent(this, NuevaSesion.class);
            intent.putExtra("idGrupo", idGrupo);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarSesiones();
    }

    private void vincularVistas() {
        rvSesiones = findViewById(R.id.rvSesiones);
        layoutEmpty = findViewById(R.id.layoutEmptySesiones);
        fabNueva = findViewById(R.id.fabNuevaSesionDetalle);
        btnBack = findViewById(R.id.btnBackSesiones);
    }

    private void configurarRecyclerView() {
        rvSesiones.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SesionAdapter(listaSesiones, this::confirmarEliminacion);
        rvSesiones.setAdapter(adapter);
    }

    private void cargarSesiones() {
        apiSesion.verSesiones(idGrupo).enqueue(new Callback<List<Sesion>>() {
            @Override
            public void onResponse(Call<List<Sesion>> call, Response<List<Sesion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaSesiones.clear();
                    listaSesiones.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    actualizarInterfaz(listaSesiones.isEmpty());
                } else {
                    actualizarInterfaz(true);
                }
            }

            @Override
            public void onFailure(Call<List<Sesion>> call, Throwable t) {
                actualizarInterfaz(true);
                Toast.makeText(GestionSesiones.this, "Error al conectar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmarEliminacion(Sesion s) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar Sesión")
                .setMessage("¿Deseas cancelar esta reunión definitivamente?")
                .setNegativeButton("No", null)
                .setPositiveButton("Sí, eliminar", (dialog, which) -> ejecutarEliminacion(s))
                .show();
    }

    private void ejecutarEliminacion(Sesion s) {

        // ✅ Formateamos la fecha como yyyy-MM-dd
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String fechaFormateada = sdf.format(s.getFecha());

        apiSesion.eliminarSesion(
                s.getIdGrupo(),
                fechaFormateada,   // ✅ ahora enviamos String
                s.getHora()
        ).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(GestionSesiones.this,
                            "✅ Sesión eliminada",
                            Toast.LENGTH_SHORT).show();
                    cargarSesiones();
                } else {
                    Toast.makeText(GestionSesiones.this,
                            "❌ Error servidor: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(GestionSesiones.this,
                        "Error de red",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarInterfaz(boolean vacio) {
        layoutEmpty.setVisibility(vacio ? View.VISIBLE : View.GONE);
        rvSesiones.setVisibility(vacio ? View.GONE : View.VISIBLE);
    }
}