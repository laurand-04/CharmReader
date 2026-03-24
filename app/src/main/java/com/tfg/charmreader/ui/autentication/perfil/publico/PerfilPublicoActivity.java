package com.tfg.charmreader.ui.autentication.perfil.publico;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.tfg.charmreader.R;
import com.tfg.charmreader.data.model.Usuario;
import com.tfg.charmreader.databinding.ActivityPerfilPublicoBinding;

public class PerfilPublicoActivity extends AppCompatActivity {

    private ActivityPerfilPublicoBinding binding;
    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPerfilPublicoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        usuario = (Usuario) getIntent().getSerializableExtra("objetoUsuario");

        binding.btnBackPerfilPublico.setOnClickListener(v -> finish());

        if (usuario != null) {
            cargarDatosUsuario();
            setupTabs();
        }
    }

    private void cargarDatosUsuario() {
        binding.tvPerfilNombrePublico.setText(usuario.getNombre());
        // Asumiendo que tienes getDescripcion() en tu modelo Usuario
        binding.tvPerfilDescripcionPublica.setText(usuario.getDescripcion() != null ? usuario.getDescripcion() : "Sin descripción");

        if (usuario.getFoto() != null && !usuario.getFoto().isEmpty()) {
            Glide.with(this)
                    .load(usuario.getFoto())
                    .circleCrop()
                    .into(binding.ivPerfilFotoPublica);
        }
    }

    private void setupTabs() {
        binding.tabLayoutPerfil.addTab(binding.tabLayoutPerfil.newTab().setText("Últimas Lecturas"));
        binding.tabLayoutPerfil.addTab(binding.tabLayoutPerfil.newTab().setText("Obras Publicadas"));

        // Cargar primer fragmento por defecto
        cambiarFragment(new UltimasLecturasFragment(usuario.getId()));

        binding.tabLayoutPerfil.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    cambiarFragment(new UltimasLecturasFragment(usuario.getId()));
                } else {
                    cambiarFragment(ObrasPublicadasFragment.newInstance(usuario.getId()));
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void cambiarFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerPerfil, fragment)
                .commit();
    }
}
