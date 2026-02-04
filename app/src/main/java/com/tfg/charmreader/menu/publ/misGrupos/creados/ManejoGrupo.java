package com.tfg.charmreader.menu.publ.misGrupos.creados;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_APICatalogo;
import com.tfg.charmreader.interfacesAPI.I_ApiGrupoLectura;
import com.tfg.charmreader.interfacesAPI.I_ApiMiembro;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.GrupoLectura;
import com.tfg.charmreader.objetosBD.Miembro;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManejoGrupo extends AppCompatActivity {

    private GrupoLectura grupo;
    private TextView tvTitulo, tvSubs, tvUbicacion;
    private LinearLayout btnCabecera;
    private ImageButton btnEditar;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton fabAñadir, fabFinalizar;

    private final I_ApiMiembro apiMiembro = API.getInstancia().create(I_ApiMiembro.class);
    private final I_ApiGrupoLectura apiGrupoLectura = API.getInstancia().create(I_ApiGrupoLectura.class);
    private final I_APICatalogo apiCatalogo = API.getInstancia().create(I_APICatalogo.class);

    private final ActivityResultLauncher<Intent> editarGrupoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    refrescarDatosGrupoDesdeAPI();
                }
            });

    private final ActivityResultLauncher<Intent> nuevoLibroLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    configurarViewPager();
                    Toast.makeText(this, "Lista de propuestas actualizada", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manejo_grupo);

        grupo = (GrupoLectura) getIntent().getSerializableExtra("objetoGrupo");
        if (grupo == null) { finish(); return; }

        vincularVistas();
        configurarListeners();
        configurarViewPager();
        actualizarInterfazCabecera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refrescarDatosGrupoDesdeAPI();
    }

    private void vincularVistas() {
        btnCabecera = findViewById(R.id.btnCabeceraGrupo);
        tvTitulo = findViewById(R.id.tvTituloManejo);
        tvSubs = findViewById(R.id.tvSubsManejo);
        tvUbicacion = findViewById(R.id.tvUbicacionManejo);
        btnEditar = findViewById(R.id.ivEditarGrupo);
        tabLayout = findViewById(R.id.tabsManejo);
        viewPager = findViewById(R.id.viewPagerManejo);
        fabAñadir = findViewById(R.id.fabAñadirLectura);
        fabFinalizar = findViewById(R.id.fabFinalizarVotacion);
    }

    private void configurarListeners() {
        btnCabecera.setOnClickListener(v -> {
            Intent intent = new Intent(this, Suscritos.class);
            intent.putExtra("idGrupo", grupo.getIdGrupo());
            startActivity(intent);
        });

        btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditarGrupo.class);
            intent.putExtra("objetoGrupo", grupo);
            editarGrupoLauncher.launch(intent);
        });

        fabAñadir.setOnClickListener(v -> {
            Intent intent = new Intent(this, NuevoLibroPropuesto.class);
            intent.putExtra("idGrupo", grupo.getIdGrupo());
            nuevoLibroLauncher.launch(intent);
        });

        fabFinalizar.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("⚖️ Dictaminar Ganador")
                    .setMessage("¿Deseas cerrar las votaciones oficialmente?\n\n" +
                            "Esta acción moverá el libro más votado a 'Actual' y archivará la lectura presente en el 'Historial'.")
                    .setPositiveButton("Dictaminar", (dialog, which) -> ejecutarCierreVotacion())
                    .setNegativeButton("Cancelar", null)
                    .show(); // Material ya aplica redondeo automáticamente
        });
    }

    private void ejecutarCierreVotacion() {
        // Mostramos un mensaje de espera
        Toast.makeText(this, "Procesando veredicto...", Toast.LENGTH_SHORT).show();

        apiCatalogo.cerrarVotaciones(grupo.getIdGrupo()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Sonido o vibración corta aquí daría un toque genial
                    Toast.makeText(ManejoGrupo.this, "Votación cerrada. ¡Nueva lectura establecida!", Toast.LENGTH_LONG).show();
                    recreate();
                } else {
                    new MaterialAlertDialogBuilder(ManejoGrupo.this)
                            .setTitle("Aviso")
                            .setMessage("No se han encontrado votos suficientes para declarar un ganador.")
                            .setPositiveButton("Entendido", null)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ManejoGrupo.this, "Error de conexión con el tribunal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refrescarDatosGrupoDesdeAPI() {
        apiGrupoLectura.obtenerGrupoPorId(grupo.getIdGrupo()).enqueue(new Callback<GrupoLectura>() {
            @Override
            public void onResponse(Call<GrupoLectura> call, Response<GrupoLectura> response) {
                if (response.isSuccessful() && response.body() != null) {
                    grupo = response.body();
                    actualizarInterfazCabecera();
                }
            }
            @Override public void onFailure(Call<GrupoLectura> call, Throwable t) {}
        });
    }

    private void actualizarInterfazCabecera() {
        tvTitulo.setText(grupo.getNombre());
        tvUbicacion.setText(grupo.getUbicacion());
        cargarContadorMiembros();
    }

    private void cargarContadorMiembros() {
        apiMiembro.obtenerPorGrupo(grupo.getIdGrupo()).enqueue(new Callback<List<Miembro>>() {
            @Override
            public void onResponse(Call<List<Miembro>> call, Response<List<Miembro>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvSubs.setText(response.body().size() + " suscriptores");
                }
            }
            @Override public void onFailure(Call<List<Miembro>> call, Throwable t) {}
        });
    }

    private void configurarViewPager() {
        viewPager.setAdapter(new ManejoPagerAdapter(this, grupo.getIdGrupo()));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Propuestas"); break;
                case 1: tab.setText("Actual"); break;
                case 2: tab.setText("Finalizadas"); break;
            }
        }).attach();

        // 🔹 Mostrar/Ocultar botones flotantes según la pestaña
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    fabAñadir.show();
                    fabFinalizar.show();
                } else {
                    fabAñadir.hide();
                    fabFinalizar.hide();
                }
            }
        });
    }

    private static class ManejoPagerAdapter extends FragmentStateAdapter {
        private final int idGrupo;
        public ManejoPagerAdapter(AppCompatActivity activity, int idGrupo) {
            super(activity);
            this.idGrupo = idGrupo;
        }
        @NonNull @Override public Fragment createFragment(int position) {
            return FragmentListaLibrosManejo.newInstance(idGrupo, position);
        }
        @Override public int getItemCount() { return 3; }
    }
}