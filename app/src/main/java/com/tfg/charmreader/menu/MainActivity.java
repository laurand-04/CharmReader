package com.tfg.charmreader.menu;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tfg.charmreader.R;
import com.tfg.charmreader.autentication.Perfil;
import com.tfg.charmreader.interfacesAPI.I_ApiUsuario;
import com.tfg.charmreader.menu.publ.Public;
import com.tfg.charmreader.menu.priv.estanteria.EstanteriaFragment;
import com.tfg.charmreader.menu.priv.futuro.EsperaFragment;
import com.tfg.charmreader.menu.priv.proximamente.ProximamenteFragment;
import com.tfg.charmreader.menu.priv.tusLibros.TusLibrosFragment;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.Usuario;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private View btnCambiarModo;
    private ShapeableImageView btnPerfil;

    // Instancia de la API para obtener los datos del usuario
    private final I_ApiUsuario apiUsuario = API.getInstancia().create(I_ApiUsuario.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Barra de estado blanca estética
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        setContentView(R.layout.activity_main);

        vincularVistas();
        configurarListeners();

        // Fragmento inicial
        if (savedInstanceState == null) {
            loadFragment(new TusLibrosFragment());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refrescamos la foto cada vez que la pantalla vuelve a estar en primer plano
        cargarFotoPerfilActualizada();
    }

    private void vincularVistas() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        btnCambiarModo = findViewById(R.id.btnCambiarModo);
        btnPerfil = findViewById(R.id.btnPerfil);
    }

    private void configurarListeners() {
        // Ir a modo público
        if (btnCambiarModo != null) {
            btnCambiarModo.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, Public.class));
            });
        }

        // Ir al perfil
        if (btnPerfil != null) {
            btnPerfil.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, Perfil.class));
            });
        }

        // Configurar navegación inferior
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.tusLibros) selectedFragment = new TusLibrosFragment();
            else if (itemId == R.id.estanteria) selectedFragment = new EstanteriaFragment();
            else if (itemId == R.id.proximamente) selectedFragment = new ProximamenteFragment();
            else if (itemId == R.id.espera) selectedFragment = new EsperaFragment();

            if (selectedFragment != null) loadFragment(selectedFragment);
            return true;
        });
    }

    private void cargarFotoPerfilActualizada() {
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();

        if (fbUser != null && fbUser.getEmail() != null) {
            apiUsuario.getIdUsuarioPorCorreo(fbUser.getEmail()).enqueue(new Callback<Usuario>() {
                @Override
                public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Usuario u = response.body();

                        // Carga asíncrona de la imagen con Glide
                        if (u.getFoto() != null && !u.getFoto().isEmpty()) {
                            Glide.with(getApplicationContext())
                                    .load(u.getFoto())
                                    .placeholder(R.drawable.ic_person)
                                    .error(R.drawable.ic_person)
                                    .centerCrop()
                                    .into(btnPerfil);
                        } else {
                            btnPerfil.setImageResource(R.drawable.ic_person);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Usuario> call, Throwable t) {
                    // Si hay error de red, mantenemos el icono por defecto
                }
            });
        }
    }

    private void loadFragment(Fragment fragment) {
        Fragment actual = getSupportFragmentManager().findFragmentById(R.id.contenedor_fragmento);
        if (actual != null && actual.getClass().equals(fragment.getClass())) {
            return;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.contenedor_fragmento, fragment)
                .commit();
    }
}