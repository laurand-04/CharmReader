package com.tfg.charmreader.ui.publ.explorar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.tfg.charmreader.R;
import com.tfg.charmreader.data.model.GrupoLectura;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import com.tfg.charmreader.databinding.ActivityInfoGrupoPublicaBinding;
import com.tfg.charmreader.ui.priv.adapterRecyclerView.BookIntAdapter;
import com.tfg.charmreader.ui.publ.adapterReclyclerView.ValoracionAdapter;
import com.tfg.charmreader.ui.publ.misGrupos.creados.ManejoGrupoActivity;
import com.tfg.charmreader.viewmodel.publ.explorar.InfoGrupoPublicoViewModel;


public class InfoGrupoPublicaActivity extends AppCompatActivity {

    private ActivityInfoGrupoPublicaBinding binding;
    private InfoGrupoPublicoViewModel viewModel;
    private GrupoLectura grupo;
    private int idUsuarioLogueado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInfoGrupoPublicaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configurarStatusBar();

        grupo = (GrupoLectura) getIntent().getSerializableExtra("objetoGrupo");
        idUsuarioLogueado = AuthRepository.getInstance(getApplicationContext()).getIdUsuario();

        if (grupo == null || idUsuarioLogueado == -1) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(InfoGrupoPublicoViewModel.class);

        setupUI();
        setupObservers();

        viewModel.verificarPertenencia(grupo.getIdGrupo(), idUsuarioLogueado);
        viewModel.cargarContenido(grupo.getIdGrupo());
    }

    private void setupUI() {
        binding.tvNombreDetalle.setText(grupo.getNombre());
        binding.tvUbicacionDetalle.setText(grupo.getUbicacion());
        binding.tvDescDetalle.setText(grupo.getDescripcion());

        if (grupo.getUrl() != null && !grupo.getUrl().isEmpty()) {
            Glide.with(this).load(grupo.getUrl()).placeholder(R.drawable.ic_people).centerCrop().into(binding.ivDetalleGrupoFoto);
        }

        binding.rvLibrosLeidos.setLayoutManager(new LinearLayoutManager(this));
        binding.rvComentariosGrupo.setLayoutManager(new LinearLayoutManager(this));

        binding.btnBackInfoPublica.setOnClickListener(v -> finish());

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Libros"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Reseñas"));

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                actualizarEstadoVacio();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void setupObservers() {
        viewModel.getEsMiembro().observe(this, miembro -> {
            boolean soyAdmin = grupo.getIdUsuario() == idUsuarioLogueado;

            binding.btnGestionarPublico.setVisibility(soyAdmin ? View.VISIBLE : View.GONE);
            binding.cardFotoPerfil.setStrokeWidth(soyAdmin ? 4 : 0);

            if (!soyAdmin) {
                binding.btnAccionUnirse.setVisibility(miembro ? View.GONE : View.VISIBLE);
                binding.btnAccionAbandonar.setVisibility(miembro ? View.VISIBLE : View.GONE);
            } else {
                binding.btnAccionUnirse.setVisibility(View.GONE);
                binding.btnAccionAbandonar.setVisibility(View.GONE);
            }
        });

        viewModel.getLibrosFinalizados().observe(this, libros -> {
            BookIntAdapter adapter = new BookIntAdapter(libros, b -> {
            });
            binding.rvLibrosLeidos.setAdapter(adapter);
            actualizarEstadoVacio();
        });

        viewModel.getReseñas().observe(this, reseñas -> {
            ValoracionAdapter adapter = new ValoracionAdapter(reseñas, null);
            binding.rvComentariosGrupo.setAdapter(adapter);
            actualizarEstadoVacio();
        });

        viewModel.getSnackbarMessage().observe(this, msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());

        binding.btnAccionUnirse.setOnClickListener(v -> viewModel.unirseAlGrupo(grupo.getIdGrupo(), idUsuarioLogueado));
        binding.btnAccionAbandonar.setOnClickListener(v -> viewModel.salirDelGrupo(grupo.getIdGrupo(), idUsuarioLogueado));
        binding.btnGestionarPublico.setOnClickListener(v -> {
            Intent i = new Intent(this, ManejoGrupoActivity.class);
            i.putExtra("objetoGrupo", grupo);
            startActivity(i);
        });
    }

    private void actualizarEstadoVacio() {
        int tab = binding.tabLayout.getSelectedTabPosition();
        boolean vacio;

        if (tab == 0) { // Libros
            binding.rvComentariosGrupo.setVisibility(View.GONE);
            vacio = viewModel.getLibrosFinalizados().getValue().isEmpty();
            binding.rvLibrosLeidos.setVisibility(vacio ? View.GONE : View.VISIBLE);
            if (vacio) {
                binding.ivEmptyIcon.setImageResource(R.drawable.ic_libro);
                binding.tvEmptyTitle.setText("¡Estantería vacía!");
            }
        } else { // Reseñas
            binding.rvLibrosLeidos.setVisibility(View.GONE);
            vacio = viewModel.getReseñas().getValue().isEmpty();
            binding.rvComentariosGrupo.setVisibility(vacio ? View.GONE : View.VISIBLE);
            if (vacio) {
                binding.ivEmptyIcon.setImageResource(R.drawable.ic_people);
                binding.tvEmptyTitle.setText("Sin opiniones");
            }
        }
        binding.layoutEmptyGrupo.setVisibility(vacio ? View.VISIBLE : View.GONE);
    }

    private void configurarStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }
}