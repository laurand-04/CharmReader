package com.tfg.charmreader.ui.publ.misGrupos.creados;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayoutMediator;
import com.tfg.charmreader.R;
import com.tfg.charmreader.data.model.GrupoLectura;
import com.tfg.charmreader.databinding.ActivityManejoGrupoBinding;
import com.tfg.charmreader.ui.publ.fragmentView.ListaLibrosManejoFragment;
import com.tfg.charmreader.viewmodel.publ.misGrupos.creados.ManejoGrupoViewModel;

public class ManejoGrupoActivity extends AppCompatActivity {

    private ActivityManejoGrupoBinding binding;
    private ManejoGrupoViewModel viewModel;
    private GrupoLectura grupo;

    private final ActivityResultLauncher<Intent> editarGrupoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    viewModel.refrescarDatosGrupo(grupo.getIdGrupo());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManejoGrupoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configurarStatusBar();

        grupo = (GrupoLectura) getIntent().getSerializableExtra("objetoGrupo");
        if (grupo == null) { finish(); return; }

        viewModel = new ViewModelProvider(this).get(ManejoGrupoViewModel.class);
        viewModel.setGrupoInicial(grupo);

        setupViewPager();
        setupObservers();
        setupListeners();

        viewModel.refrescarDatosGrupo(grupo.getIdGrupo());
    }

    private void setupObservers() {
        viewModel.getGrupo().observe(this, g -> {
            this.grupo = g;
            binding.tvTituloManejo.setText(g.getNombre());
            Glide.with(this).load(g.getUrl()).placeholder(R.drawable.ic_people).circleCrop().into(binding.ivAvatarGrupo);
        });

        viewModel.getContadorMiembros().observe(this, count ->
                binding.tvSubsManejo.setText(count + " suscriptores"));

        viewModel.getMensaje().observe(this, msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    private void setupListeners() {
        binding.btnBackManejo.setOnClickListener(v -> finish());

        binding.btnCabeceraGrupo.setOnClickListener(v -> {
            Intent i = new Intent(this, SuscritosActivity.class);
            i.putExtra("idGrupo", grupo.getIdGrupo());
            startActivity(i);
        });

        binding.ivEditarGrupo.setOnClickListener(v -> {
            Intent i = new Intent(this, EditarGrupoActivity.class);
            i.putExtra("objetoGrupo", grupo);
            editarGrupoLauncher.launch(i);
        });

        binding.fabAnadirLectura.setOnClickListener(v -> {
            Intent i = new Intent(this, NuevoLibroPropuestoActivity.class);
            i.putExtra("idGrupo", grupo.getIdGrupo());
            startActivity(i);
        });

        binding.fabNuevaSesion.setOnClickListener(v -> {
            Intent i = new Intent(this, GestionSesionesActivity.class);
            i.putExtra("idGrupo", grupo.getIdGrupo());
            startActivity(i);
        });

        binding.fabFinalizarVotacion.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("⚖️ Dictaminar Ganador")
                    .setMessage("¿Deseas cerrar las votaciones oficialmente?")
                    .setPositiveButton("Dictaminar", (d, w) -> viewModel.cerrarVotaciones(grupo.getIdGrupo()))
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }


    private void setupViewPager() {
        binding.viewPagerManejo.setAdapter(new ManejoPagerAdapter(this, grupo.getIdGrupo()));

        new TabLayoutMediator(binding.tabsManejo, binding.viewPagerManejo, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Propuestas"); break;
                case 1: tab.setText("Actual"); break;
                case 2: tab.setText("Finalizadas"); break;
            }
        }).attach();

        binding.viewPagerManejo.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                gestionarVisibilidadFabs(position);
            }
        });
    }

    private void gestionarVisibilidadFabs(int position) {
        binding.fabAnadirLectura.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        binding.fabFinalizarVotacion.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        binding.fabNuevaSesion.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
    }

    private void configurarStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    private static class ManejoPagerAdapter extends FragmentStateAdapter {
        private final int idGrupo;
        public ManejoPagerAdapter(AppCompatActivity activity, int idGrupo) {
            super(activity);
            this.idGrupo = idGrupo;
        }
        @NonNull @Override public Fragment createFragment(int position) {
            return ListaLibrosManejoFragment.newInstance(idGrupo, position);
        }
        @Override public int getItemCount() { return 3; }
    }
}