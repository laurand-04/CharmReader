package com.tfg.charmreader.menu.publ;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import com.tfg.charmreader.menu.publ.explorar.ExplorarFragment;
import com.tfg.charmreader.menu.publ.misGrupos.MisGruposFragment;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.Usuario;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Public extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ShapeableImageView btnPerfil;

    // Instancia de la API para obtener los datos del usuario (Nombre/Foto)
    private final I_ApiUsuario apiUsuario = API.getInstancia().create(I_ApiUsuario.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Barra de estado blanca
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        setContentView(R.layout.activity_public);

        vincularVistas();
        configurarListeners();

        // Fragmento inicial
        if (savedInstanceState == null) {
            loadFragment(new ExplorarFragment());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cada vez que volvemos a la pantalla (ej. tras editar perfil), refrescamos la foto
        cargarDatosUsuarioCabecera();
    }

    private void vincularVistas() {
        btnPerfil = findViewById(R.id.btnPerfilPublico);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void configurarListeners() {
        // Regresar a Privado
        TextView btnIrPrivado = findViewById(R.id.btnIrPrivado);
        if (btnIrPrivado != null) {
            btnIrPrivado.setOnClickListener(v -> finish());
        }

        // Ir al perfil
        if (btnPerfil != null) {
            btnPerfil.setOnClickListener(v -> {
                Intent intent = new Intent(Public.this, Perfil.class);
                startActivity(intent);
            });
        }

        // Navegación Bottom Bar
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_explorar) {
                selectedFragment = new ExplorarFragment();
            } else if (itemId == R.id.nav_mis_grupos) {
                selectedFragment = new MisGruposFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });
    }

    private void cargarDatosUsuarioCabecera() {
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();

        if (fbUser != null && fbUser.getEmail() != null) {
            // Buscamos al usuario en nuestra BD usando su correo
            apiUsuario.getIdUsuarioPorCorreo(fbUser.getEmail()).enqueue(new Callback<Usuario>() {
                @Override
                public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Usuario u = response.body();

                        // Si el usuario tiene una foto configurada, la cargamos con Glide
                        if (u.getFoto() != null && !u.getFoto().isEmpty()) {
                            Glide.with(getApplicationContext())
                                    .load(u.getFoto())
                                    .placeholder(R.drawable.ic_person)
                                    .error(R.drawable.ic_person)
                                    .centerCrop()
                                    .into(btnPerfil);
                        } else {
                            // Si no hay foto, aseguramos que aparezca el icono por defecto
                            btnPerfil.setImageResource(R.drawable.ic_person);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Usuario> call, Throwable t) {
                    // Si falla la red, mantenemos el icono por defecto
                }
            });
        }
    }

    private void loadFragment(Fragment fragment) {
        Fragment actual = getSupportFragmentManager().findFragmentById(R.id.contenedor_fragmentos);
        if (actual != null && actual.getClass().equals(fragment.getClass())) {
            return;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.contenedor_fragmentos, fragment)
                .commit();
    }
}