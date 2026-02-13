package com.tfg.charmreader.admin;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.tfg.charmreader.R;
import com.tfg.charmreader.admin.adapterRecyclerView.UsuarioAdapter;
import com.tfg.charmreader.interfacesAPI.I_ApiUsuario;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.Usuario;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GestionUsuarios extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UsuarioAdapter adapter;
    private ImageView btnBack;
    private List<Usuario> listaUsuariosLocal;
    private I_ApiUsuario apiUsuario = API.getInstancia().create(I_ApiUsuario.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Barra de estado blanca
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        setContentView(R.layout.activity_gestion_usuarios);

        vincularVistas();
        cargarUsuarios();
    }

    private void vincularVistas() {
        recyclerView = findViewById(R.id.rvUsuariosAdmin);
        btnBack = findViewById(R.id.btnBackGestionUsuarios);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        btnBack.setOnClickListener(v -> finish());
    }

    private void cargarUsuarios() {
        apiUsuario.obtenerUsuarios().enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaUsuariosLocal = response.body();

                    // Configuramos el adaptador con el listener de borrado
                    adapter = new UsuarioAdapter(listaUsuariosLocal, usuario -> {
                        ejecutarBorradoEnAPI(usuario);
                    });

                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(GestionUsuarios.this, "No hay usuarios registrados", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
                Toast.makeText(GestionUsuarios.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ejecutarBorradoEnAPI(Usuario usuario) {
        apiUsuario.eliminarUsuario(usuario.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    int pos = listaUsuariosLocal.indexOf(usuario);
                    if (pos != -1) {
                        listaUsuariosLocal.remove(pos);
                        adapter.notifyItemRemoved(pos);
                        Toast.makeText(GestionUsuarios.this, "Usuario eliminado", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(GestionUsuarios.this, "Fallo al eliminar usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }
}