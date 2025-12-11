package com.tfg.charmreader.menu;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tfg.charmreader.R;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Cargar fragmento por defecto
        loadFragment(new TusLibrosFragment());

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
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedor_fragmento, fragment)
                .commit();
    }
}

