package com.tfg.charmreader.admin;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.tfg.charmreader.R;
import com.tfg.charmreader.admin.adapterRecyclerView.GrupoAdapter;
import com.tfg.charmreader.interfacesAPI.I_ApiGrupoLectura;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.GrupoLectura;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GestionGrupos extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GrupoAdapter adapter;
    private List<GrupoLectura> listaGruposLocal = new ArrayList<>();
    private I_ApiGrupoLectura apiGrupo = API.getInstancia().create(I_ApiGrupoLectura.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_grupos); // Asegúrate de crear este XML (es igual al de usuarios)

        recyclerView = findViewById(R.id.rvGruposAdmin);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cargarGrupos();
    }

    private void cargarGrupos() {
        // Llamada a tu endpoint GET /grupos/listar (debes tenerlo en tu API)
        apiGrupo.obtenerGrupos().enqueue(new Callback<List<GrupoLectura>>() {
            @Override
            public void onResponse(Call<List<GrupoLectura>> call, Response<List<GrupoLectura>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaGruposLocal = response.body();
                    adapter = new GrupoAdapter(listaGruposLocal, grupo -> {
                        eliminarGrupoEnAPI(grupo);
                    });
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(GestionGrupos.this, "No hay grupos creados", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<GrupoLectura>> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
                Toast.makeText(GestionGrupos.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void eliminarGrupoEnAPI(GrupoLectura grupo) {
        // Llamada al endpoint DELETE /grupos/eliminar/{id} que creamos en Spring Boot
        apiGrupo.eliminarGrupo(grupo.getIdGrupo()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    int pos = listaGruposLocal.indexOf(grupo);
                    if (pos != -1) {
                        listaGruposLocal.remove(pos);
                        adapter.notifyItemRemoved(pos);
                        Toast.makeText(GestionGrupos.this, "Grupo eliminado con éxito", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(GestionGrupos.this, "Error al eliminar el grupo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(GestionGrupos.this, "Fallo en la red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}