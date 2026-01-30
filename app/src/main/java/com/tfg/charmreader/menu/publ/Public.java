package com.tfg.charmreader.menu.publ;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.tfg.charmreader.R;
import com.tfg.charmreader.menu.publ.explorar.ExplorarFragment;

public class Public extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public);

        // Configuración del botón de encabezado
        MaterialButton btnIrPrivado = findViewById(R.id.btnIrPrivado);
        btnIrPrivado.setOnClickListener(v -> {
            // Si viniste desde MainActivity con un Intent, finish() te devuelve allí
            finish();
            // Opcional: Si quieres asegurar el salto
            // Intent intent = new Intent(Public.this, MainActivity.class);
            // startActivity(intent);
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Carga inicial: Si es la primera vez que se abre, cargamos Explorar
        if (savedInstanceState == null) {
            loadFragment(new ExplorarFragment());
        }

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

    // Tu función optimizada de MainActivity
    private void loadFragment(Fragment fragment) {
        // Buscamos si ya hay un fragmento en el contenedor
        Fragment actual = getSupportFragmentManager().findFragmentById(R.id.contenedor_fragmentos);

        // Si el fragmento que queremos cargar es el mismo que ya está puesto, no hacemos nada
        if (actual != null && actual.getClass().equals(fragment.getClass())) {
            return;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedor_fragmentos, fragment)
                .commit();
    }
}