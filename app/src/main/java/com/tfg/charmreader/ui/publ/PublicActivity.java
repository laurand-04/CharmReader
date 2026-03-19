package com.tfg.charmreader.ui.publ;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.tfg.charmreader.R;
import com.tfg.charmreader.databinding.ActivityPublicBinding;
import com.tfg.charmreader.ui.autentication.perfil.PerfilActivity;
import com.tfg.charmreader.ui.publ.fragmentView.ExplorarFragment;
import com.tfg.charmreader.ui.publ.fragmentView.MisGruposFragment;
import com.tfg.charmreader.viewmodel.publ.misGrupos.PublicViewModel;

public class PublicActivity extends AppCompatActivity {

    private ActivityPublicBinding binding;
    private PublicViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPublicBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configurarStatusBar();
        viewModel = new ViewModelProvider(this).get(PublicViewModel.class);
        setupNavigation();
        setupObservers();

        if (savedInstanceState == null) {
            loadFragment(new ExplorarFragment());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Sincronizamos datos cada vez que se vuelve a la pantalla
        viewModel.cargarDatosUsuario(getApplicationContext());
    }

    private void setupObservers() {
        viewModel.getUsuario().observe(this, usuario -> {
            if (usuario != null && usuario.getFoto() != null && !usuario.getFoto().isEmpty()) {
                Glide.with(this)
                        .load(usuario.getFoto())
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .centerCrop()
                        .into(binding.btnPerfilPublico);
            } else {
                binding.btnPerfilPublico.setImageResource(R.drawable.ic_person);
            }
            Log.d("CargandoFotoUsuario", "setupObservers: " + usuario);
        });
    }

    private void setupNavigation() {
        // Regresar a Privado (Estantería)
        binding.btnIrPrivado.setOnClickListener(v -> finish());

        // Ir al perfil
        binding.btnPerfilPublico.setOnClickListener(v -> {
            startActivity(new Intent(this, PerfilActivity.class));
        });

        // Bottom Navigation reactivo
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_explorar) fragment = new ExplorarFragment();
            else if (id == R.id.nav_mis_grupos) fragment = new MisGruposFragment();

            if (fragment != null) loadFragment(fragment);
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        Fragment actual = getSupportFragmentManager().findFragmentById(R.id.contenedor_fragmentos);
        if (actual != null && actual.getClass().equals(fragment.getClass())) return;

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.contenedor_fragmentos, fragment)
                .commit();
    }

    private void configurarStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }
}