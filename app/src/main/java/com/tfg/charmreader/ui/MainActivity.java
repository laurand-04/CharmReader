package com.tfg.charmreader.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.tfg.charmreader.R;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import com.tfg.charmreader.databinding.ActivityMainBinding;
import com.tfg.charmreader.ui.autentication.PerfilActivity;
import com.tfg.charmreader.ui.publ.PublicActivity;
import com.tfg.charmreader.ui.priv.fragmentView.EstanteriaFragment;
import com.tfg.charmreader.ui.priv.fragmentView.EsperaFragment;
import com.tfg.charmreader.ui.priv.fragmentView.ProximamenteFragment;
import com.tfg.charmreader.ui.priv.fragmentView.TusLibrosFragment;
import com.tfg.charmreader.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AuthRepository.getInstance(this).getIdUsuario();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.setContext(this);
        configurarListeners();
        suscribirObservadores();
        // Fragmento inicial
        if (savedInstanceState == null) {
            loadFragment(new TusLibrosFragment());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.refreshUserProfile();
    }

    private void suscribirObservadores() {
        // La Activity "observa" cambios. Si la foto cambia en la BD, la UI se entera sola.
        viewModel.getUserPhotoUrl().observe(this, photoUrl -> {
            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .centerCrop()
                    .into(binding.btnPerfil);
        });
    }

    private void configurarListeners() {
        // Ir a modo público
        binding.btnCambiarModo.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, PublicActivity.class));
        });


        binding.btnPerfil.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, PerfilActivity.class));
        });


        // Configurar navegación inferior
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.tusLibros) {
                selectedFragment = new TusLibrosFragment();
                Log.d("MainActivity_bottomNavigation", "configurarListeners: TusLibrosFragment");
            } else if (itemId == R.id.estanteria) {
                selectedFragment = new EstanteriaFragment();
                Log.d("MainActivity_bottomNavigation", "configurarListeners: EstanteriaFragment");
            } else if (itemId == R.id.proximamente) {
                selectedFragment = new ProximamenteFragment();
                Log.d("MainActivity_bottomNavigation", "configurarListeners: ProximamenteFragment");
            } else if (itemId == R.id.espera) {
                selectedFragment = new EsperaFragment();
                Log.d("MainActivity_bottomNavigation", "configurarListeners: EsperaFragment");
            }

            loadFragment(selectedFragment);
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        if (fragment == null) return;

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