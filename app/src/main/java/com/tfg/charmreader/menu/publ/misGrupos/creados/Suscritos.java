package com.tfg.charmreader.menu.publ.misGrupos.creados;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
    private final I_ApiMiembro api = API.getInstancia().create(I_ApiMiembro.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suscritos);

        idGrupo = getIntent().getIntExtra("idGrupo", -1);
        rv = findViewById(R.id.rvSuscritos);
        rv.setLayoutManager(new LinearLayoutManager(this));

        cargarMiembros();
    }

    private void cargarMiembros() {
        api.obtenerPorGrupo(idGrupo).enqueue(new Callback<List<Miembro>>() {
            @Override
            public void onResponse(Call<List<Miembro>> call, Response<List<Miembro>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // El adapter recibe el listener que ahora dispara el diálogo
                    rv.setAdapter(new MiembroAdapter(response.body(), idU -> confirmarEliminacion(idU)));
                }
            }
            @Override public void onFailure(Call<List<Miembro>> call, Throwable t) {
                Toast.makeText(Suscritos.this, "Error al cargar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔹 NUEVO: Diálogo de confirmación
    private void confirmarEliminacion(int idUsuario) {
        new AlertDialog.Builder(this)
                .setTitle("Expulsar miembro")
                .setMessage("¿Estás seguro de que quieres eliminar a este usuario del grupo? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> ejecutarEliminacion(idUsuario))
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void ejecutarEliminacion(int idUsuario) {
        api.salirDeGrupo(idGrupo, idUsuario).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(Suscritos.this, "Usuario expulsado con éxito", Toast.LENGTH_SHORT).show();
                    cargarMiembros(); // Refrescamos la lista
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(Suscritos.this, "Error de red al eliminar", Toast.LENGTH_SHORT).show();
            }
        });
    }
}