package com.tfg.charmreader.ui.publ.misGrupos.suscritos;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayoutMediator;
import com.tfg.charmreader.R;
import com.tfg.charmreader.data.model.BookEn;
import com.tfg.charmreader.data.model.GrupoLectura;
import com.tfg.charmreader.data.model.Sesion;
import com.tfg.charmreader.databinding.ActivityInfoGrupoPrivadaBinding;
import com.tfg.charmreader.ui.publ.fragmentView.HistorialFragment;
import com.tfg.charmreader.ui.publ.fragmentView.PropuestasFragment;
import com.tfg.charmreader.viewmodel.publ.misGrupos.suscritos.InfoGrupoPrivadaViewModel;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class InfoGrupoPrivadaActivity extends AppCompatActivity {

    private ActivityInfoGrupoPrivadaBinding binding;
    private InfoGrupoPrivadaViewModel viewModel;
    private GrupoLectura grupo;
    private BookEn libroActualObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInfoGrupoPrivadaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configurarStatusBar();

        grupo = (GrupoLectura) getIntent().getSerializableExtra("objetoGrupo");
        if (grupo == null) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(InfoGrupoPrivadaViewModel.class);

        setupUI();
        setupObservers();
        viewModel.cargarDatos(grupo.getIdGrupo());
    }

    private void setupUI() {
        binding.tvNombreGrupoPrivada.setText(grupo.getNombre());
        binding.tvUbicacionGrupoPrivada.setText(grupo.getUbicacion());

        Glide.with(this).load(grupo.getUrl()).placeholder(R.drawable.ic_people).circleCrop().into(binding.ivFotoGrupoPrivada);

        binding.btnBackPrivada.setOnClickListener(v -> finish());

        binding.cardLecturaActual.setOnClickListener(v -> {
            if (libroActualObj != null) {
                Intent i = new Intent(this, LibroActualActivity.class);
                i.putExtra("libroSeleccionado", libroActualObj);
                i.putExtra("grupo", grupo);
                startActivity(i);
            }
        });

        binding.fabResenaGrupo.setOnClickListener(v -> {
            Intent i = new Intent(this, ResenasGrupoPrivadaActivity.class);
            i.putExtra("objetoGrupo", grupo);
            startActivity(i);
        });

        setupViewPager();
    }

    private void setupObservers() {
        viewModel.getLibroActual().observe(this, libro -> {
            if (libro != null) {
                this.libroActualObj = libro;
                binding.tvLibroActual.setText(libro.getTitulo());
                binding.tvAutorActual.setText(libro.getAutor());
                String url = "https://covers.openlibrary.org/b/id/" + libro.getCoverId() + "-M.jpg";
                Glide.with(this).load(url).placeholder(R.drawable.ic_libro).into(binding.ivPortadaActual);
            }
        });

        viewModel.getProximaSesion().observe(this, sesion -> {
            if (sesion != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                binding.tvFechaSesion.setText(sdf.format(sesion.getFecha()));
                binding.tvHoraSesion.setText(sesion.getHora().substring(0, 5) + "h");
                binding.tvCapitulosSesion.setText("Cap. " + sesion.getCapituloInicio() + " al " + sesion.getCapituloFinalizacion());
            }
        });
    }

    private void setupViewPager() {
        binding.viewPagerLecturas.setAdapter(new ViewPagerAdapter(this));
        new TabLayoutMediator(binding.tabsLecturas, binding.viewPagerLecturas, (tab, position) -> {
            tab.setText(position == 0 ? "Propuestas" : "Historial");
        }).attach();
    }

    private void configurarStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    private static class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return (position == 0) ? new PropuestasFragment() : new HistorialFragment();
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}