package com.tfg.charmreader.menu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.tfg.charmreader.R;
import com.tfg.charmreader.autentication.Perfil;
import com.tfg.charmreader.menu.publ.Public;
import com.tfg.charmreader.menu.priv.estanteria.EstanteriaFragment;
import com.tfg.charmreader.menu.priv.futuro.EsperaFragment;
import com.tfg.charmreader.menu.priv.proximamente.ProximamenteFragment;
import com.tfg.charmreader.menu.priv.tusLibros.TusLibrosFragment;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    MaterialButton btnCambiarModo;
    ImageButton btnPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        btnCambiarModo = findViewById(R.id.btnCambiarModo);
        btnPerfil = findViewById(R.id.btnPerfil);

        // Ir a modo público
        btnCambiarModo.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Public.class);
            startActivity(intent);
        });

        // Ir al perfil
        btnPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Perfil.class);
            startActivity(intent);
        });

        if (savedInstanceState == null) {
            loadFragment(new TusLibrosFragment());
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.tusLibros) {
                selectedFragment = new TusLibrosFragment();
            } else if (itemId == R.id.estanteria) {
                selectedFragment = new EstanteriaFragment();
            } else if (itemId == R.id.proximamente) {
                selectedFragment = new ProximamenteFragment();
            } else if (itemId == R.id.espera) {
                selectedFragment = new EsperaFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        Fragment actual = getSupportFragmentManager().findFragmentById(R.id.contenedor_fragmento);
        if (actual != null && actual.getClass().equals(fragment.getClass())) {
            return;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedor_fragmento, fragment)
                .commit();
    }
}
