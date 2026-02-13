package com.tfg.charmreader.menu.publ.misGrupos.creados;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_ApiMiembro;
import com.tfg.charmreader.menu.publ.adapterReclyclerView.MiembroAdapter;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.Miembro;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Suscritos extends AppCompatActivity {
    private int idGrupo;
    private RecyclerView rv;
    private ImageView btnBack;
    private final I_ApiMiembro api = API.getInstancia().create(I_ApiMiembro.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Barra de estado blanca
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        setContentView(R.layout.activity_suscritos);

        idGrupo = getIntent().getIntExtra("idGrupo", -1);

        rv = findViewById(R.id.rvSuscritos);
        btnBack = findViewById(R.id.btnBackSuscritos);

        rv.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());

        cargarMiembros();
    }

    private void cargarMiembros() {
        api.obtenerPorGrupo(idGrupo).enqueue(new Callback<List<Miembro>>() {
            @Override
            public void onResponse(Call<List<Miembro>> call, Response<List<Miembro>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // El adapter recibe el idUsuario para la confirmación
                    rv.setAdapter(new MiembroAdapter(response.body(), idU -> confirmarEliminacion(idU)));
                }
            }
            @Override public void onFailure(Call<List<Miembro>> call, Throwable t) {
                Toast.makeText(Suscritos.this, "Error al cargar miembros", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmarEliminacion(int idUsuario) {
        // Usamos MaterialAlertDialogBuilder para mantener la estética de la app
        new MaterialAlertDialogBuilder(this)
                .setTitle("⚠️ Expulsar miembro")
                .setMessage("¿Estás seguro de que quieres eliminar a este usuario del grupo? Perderá el acceso a las lecturas actuales.")
                .setPositiveButton("Expulsar", (dialog, which) -> ejecutarEliminacion(idUsuario))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void ejecutarEliminacion(int idUsuario) {
        api.salirDeGrupo(idGrupo, idUsuario).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(Suscritos.this, "Usuario expulsado con éxito", Toast.LENGTH_SHORT).show();
                    cargarMiembros();
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(Suscritos.this, "Error de red al eliminar", Toast.LENGTH_SHORT).show();
            }
        });
    }
}