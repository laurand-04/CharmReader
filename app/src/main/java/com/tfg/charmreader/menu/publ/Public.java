package com.tfg.charmreader.menu.publ;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;
import com.tfg.charmreader.R;
import com.tfg.charmreader.autentication.Perfil;
import com.tfg.charmreader.menu.publ.explorar.ExplorarFragment;
import com.tfg.charmreader.menu.publ.misGrupos.MisGruposFragment;

public class Public extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ShapeableImageView btnPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public);

        // Barra de estado blanca
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        // 1. INICIALIZAR VISTAS (Corregido con los IDs de tu XML)
        btnPerfil = findViewById(R.id.btnPerfilPublico); // ID corregido
        TextView btnIrPrivado = findViewById(R.id.btnIrPrivado);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 2. ACCIÓN: Regresar a Privado
        if (btnIrPrivado != null) {
            btnIrPrivado.setOnClickListener(v -> finish());
        }

        // 3. ACCIÓN: Ir al perfil (Coincidiendo con MainActivity)
        if (btnPerfil != null) {
            btnPerfil.setOnClickListener(v -> {
                Intent intent = new Intent(Public.this, Perfil.class);
                startActivity(intent);
            });
        }

        // Fragmento inicial
        if (savedInstanceState == null) {
            loadFragment(new ExplorarFragment());
        }

        // Configuración Bottom Navigation
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