package com.tfg.charmreader.menu.publ.misGrupos.suscritos;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_ApiValoracion;
import com.tfg.charmreader.menu.publ.adapterReclyclerView.ValoracionAdapter;
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
    private ImageView btnBack;
    private ValoracionAdapter valoracionAdapter;
    private final I_ApiValoracion apiValoracion = API.getInstancia().create(I_ApiValoracion.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Barra de estado blanca
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        setContentView(R.layout.activity_valoraciones_libro);

        idLibro = getIntent().getIntExtra("idLibro", -1);
        idGrupo = getIntent().getIntExtra("idGrupo", -1);

        if (idLibro == -1) {
            finish();
            return;
        }

        inicializarVistas();

        cargarComentariosLibro();
    }

    private void inicializarVistas() {
        rvComentarios = findViewById(R.id.rvComentariosLibro);
        fabAdd = findViewById(R.id.fabAddReseña);
        btnBack = findViewById(R.id.btnBackValoraciones);

        rvComentarios.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, ValoracionLibroNueva.class);
            intent.putExtra("idLibro", idLibro);
            intent.putExtra("idGrupo", idGrupo);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarComentariosLibro();
    }

    private void cargarComentariosLibro() {
        apiValoracion.verValoraciones(Valoracion.TipoValoracion.LIBRO, idLibro).enqueue(new Callback<List<Valoracion>>() {
            @Override
            public void onResponse(Call<List<Valoracion>> call, Response<List<Valoracion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // El adapter usará el diseño de CardView con 4dp de elevación que ya creamos
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