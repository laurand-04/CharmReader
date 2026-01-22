package com.tfg.charmreader.menu;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tfg.charmreader.R;
import com.tfg.charmreader.menu.estanteria.EstanteriaFragment;
import com.tfg.charmreader.menu.tusLibros.TusLibrosFragment;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Cargar fragmento por defecto
        //loadFragment(new TusLibrosFragment());

        // PASO 1: Solo cargamos el fragmento por defecto si es la primera vez que se abre la actividad.
        // Si savedInstanceState NO es nulo, Android ya ha restaurado el fragmento que estaba abierto
        // (por ejemplo, el de Estanterías) y no debemos sobreescribirlo con el de Libros.
        if (savedInstanceState == null) {
            loadFragment(new TusLibrosFragment());
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId(); // Obtener el ID una vez

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
        // Evita recargar el mismo fragmento si ya es el que se está mostrando
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

