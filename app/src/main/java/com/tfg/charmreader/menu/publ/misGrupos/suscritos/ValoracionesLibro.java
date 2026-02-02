package com.tfg.charmreader.menu.publ.misGrupos.suscritos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_ApiValoracion;
import com.tfg.charmreader.menu.publ.adapterReclyclerView.ValoracionAdapter;
import com.tfg.charmreader.menu.publ.misGrupos.suscritos.ValoracionLibroNueva;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.Valoracion;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ValoracionesLibro extends AppCompatActivity {

    private int idLibro, idGrupo;
    private RecyclerView rvComentarios;
    private FloatingActionButton fabAdd;
    private ValoracionAdapter valoracionAdapter;
    private final I_ApiValoracion apiValoracion = API.getInstancia().create(I_ApiValoracion.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valoraciones_libro);

        // Recuperamos los IDs necesarios para la navegación
        idLibro = getIntent().getIntExtra("idLibro", -1);
        idGrupo = getIntent().getIntExtra("idGrupo", -1);

        if (idLibro == -1) {
            finish();
            return;
        }

        rvComentarios = findViewById(R.id.rvComentariosLibro);
        fabAdd = findViewById(R.id.fabAddReseña);

        rvComentarios.setLayoutManager(new LinearLayoutManager(this));

        // Botón para ir a la pantalla de escribir reseña (ValoracionLibro)
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, ValoracionLibroNueva.class);
            intent.putExtra("idLibro", idLibro);
            intent.putExtra("idGrupo", idGrupo);
            startActivity(intent);
        });

        cargarComentariosLibro();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refrescamos al volver de la otra actividad para mostrar la nueva reseña
        cargarComentariosLibro();
    }

    private void cargarComentariosLibro() {
        apiValoracion.verValoraciones(Valoracion.TipoValoracion.LIBRO, idLibro).enqueue(new Callback<List<Valoracion>>() {
            @Override
            public void onResponse(Call<List<Valoracion>> call, Response<List<Valoracion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Reutilizamos tu adapter actual
                    valoracionAdapter = new ValoracionAdapter(response.body(), null);
                    rvComentarios.setAdapter(valoracionAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<Valoracion>> call, Throwable t) {
                Toast.makeText(ValoracionesLibro.this, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }
}