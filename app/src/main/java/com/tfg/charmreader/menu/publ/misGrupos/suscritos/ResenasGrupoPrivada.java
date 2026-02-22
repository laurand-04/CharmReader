package com.tfg.charmreader.menu.publ.misGrupos.suscritos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_APICatalogo; // Ajusta según donde tengas el método de reseñas
import com.tfg.charmreader.interfacesAPI.I_ApiValoracion;
import com.tfg.charmreader.menu.publ.adapterReclyclerView.ValoracionAdapter;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.GrupoLectura;
import com.tfg.charmreader.objetosBD.Valoracion;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResenasGrupoPrivada extends AppCompatActivity {

    private RecyclerView rvResenas;
    private FloatingActionButton fabAdd;
    private ImageView btnBack;
    private LinearLayout layoutEmpty; // Asegúrate de añadir esto en tu XML si quieres mensaje de "No hay reseñas"

    private GrupoLectura grupo;
    private ValoracionAdapter adapter;
    private List<Valoracion> listaValoraciones = new ArrayList<>();

    // Suponiendo que las reseñas se obtienen de I_APICatalogo o una similar
    private final I_ApiValoracion apiValoracion = API.getInstancia().create(I_ApiValoracion.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resenas_grupo_privada);

        grupo = (GrupoLectura) getIntent().getSerializableExtra("objetoGrupo");
        if (grupo == null) { finish(); return; }

        vincularVistas();
        configurarRecyclerView();

        btnBack.setOnClickListener(v -> finish());

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, ValoracionGrupo.class);
            intent.putExtra("idGrupo", grupo.getIdGrupo());
            intent.putExtra("idLibro", -1);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarResenas(); // Recargar al volver de escribir una reseña
    }

    private void vincularVistas() {
        rvResenas = findViewById(R.id.rvResenasGrupo);
        fabAdd = findViewById(R.id.fabNuevaResena);
        btnBack = findViewById(R.id.btnBackResenas);
        layoutEmpty = findViewById(R.id.layoutEmptyResenas); // Opcional
    }

    private void configurarRecyclerView() {
        rvResenas.setLayoutManager(new LinearLayoutManager(this));
        // Usamos el adapter que ya tienes. El listener puede abrir el detalle si quieres.
        adapter = new ValoracionAdapter(listaValoraciones, valoracion -> {
            // Acción al pulsar una reseña (opcional)
        });
        rvResenas.setAdapter(adapter);
    }

    private void cargarResenas() {
        // Llamada a la API para obtener reseñas del grupo (idLibro = -1 suele ser la marca para grupos)
        apiValoracion.verValoraciones(Valoracion.TipoValoracion.GRUPO,grupo.getIdGrupo()).enqueue(new Callback<List<Valoracion>>() {
            @Override
            public void onResponse(Call<List<Valoracion>> call, Response<List<Valoracion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaValoraciones.clear();
                    listaValoraciones.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    actualizarInterfaz(listaValoraciones.isEmpty());
                }
            }

            @Override
            public void onFailure(Call<List<Valoracion>> call, Throwable t) {
                Toast.makeText(ResenasGrupoPrivada.this, "Error al cargar reseñas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarInterfaz(boolean vacio) {
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(vacio ? View.VISIBLE : View.GONE);
            rvResenas.setVisibility(vacio ? View.GONE : View.VISIBLE);
        }
    }
}