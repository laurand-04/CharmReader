package com.tfg.charmreader.admin;

import android.os.Bundle;
import android.util.Log;
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
    private I_ApiUsuario apiUsuario = API.getInstancia().create(I_ApiUsuario.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_usuarios);

        recyclerView = findViewById(R.id.rvUsuariosAdmin);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cargarUsuarios();
    }

    private List<Usuario> listaUsuariosLocal; // Para manejar el borrado visual

    private void cargarUsuarios() {
        apiUsuario.obtenerUsuarios().enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaUsuariosLocal = response.body();

                    // Configuramos el adaptador con la interfaz de borrado
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
                Toast.makeText(GestionUsuarios.this, "Fallo de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ejecutarBorradoEnAPI(Usuario usuario) {
        apiUsuario.eliminarUsuario(usuario.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Borrado visual
                    int pos = listaUsuariosLocal.indexOf(usuario);
                    if (pos != -1) {
                        listaUsuariosLocal.remove(pos);
                        adapter.notifyItemRemoved(pos);
                        Toast.makeText(GestionUsuarios.this, "Usuario eliminado correctamente", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(GestionUsuarios.this, "Error al eliminar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void borrarUsuario(Usuario usuario) {
        // 1. Llamada a la API de Spring Boot
        apiUsuario.eliminarUsuario(usuario.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // 2. Si la API borra con éxito, quitamos de la lista local
                    int posicion = listaUsuariosLocal.indexOf(usuario);
                    listaUsuariosLocal.remove(posicion);

                    // 3. Avisamos al adaptador para que haga la animación de borrado
                    adapter.notifyItemRemoved(posicion);

                    Toast.makeText(GestionUsuarios.this, "Usuario eliminado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(GestionUsuarios.this, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }
}