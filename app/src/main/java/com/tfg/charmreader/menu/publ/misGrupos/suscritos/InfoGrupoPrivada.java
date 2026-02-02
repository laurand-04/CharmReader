package com.tfg.charmreader.menu.publ.misGrupos.suscritos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_APICatalogo;
import com.tfg.charmreader.interfacesAPI.I_ApiSesion;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.BookEn;
import com.tfg.charmreader.objetosBD.GrupoLectura;
import com.tfg.charmreader.objetosBD.Sesion;

import java.text.SimpleDateFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InfoGrupoPrivada extends AppCompatActivity {

    private TextView tvLibroActual, tvAutorActual, tvFechaSesion, tvHoraSesion, tvCapitulosSesion;
    private ImageView ivPortadaActual;
    private MaterialCardView cardLecturaActual;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton fabReseña;

    private GrupoLectura grupo;
    private BookEn libroActualObj;

    private I_APICatalogo apiLecturas = API.getInstancia().create(I_APICatalogo.class);
    private I_ApiSesion apiSesion = API.getInstancia().create(I_ApiSesion.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_grupo_privada);

        // 1. Inicializar vistas
        tvLibroActual = findViewById(R.id.tvLibroActual);
        tvAutorActual = findViewById(R.id.tvAutorActual);
        tvFechaSesion = findViewById(R.id.tvFechaSesion);
        tvHoraSesion = findViewById(R.id.tvHoraSesion);
        tvCapitulosSesion = findViewById(R.id.tvCapitulosSesion);
        ivPortadaActual = findViewById(R.id.ivPortadaActual);
        cardLecturaActual = findViewById(R.id.cardLecturaActual);
        tabLayout = findViewById(R.id.tabsLecturas);
        viewPager = findViewById(R.id.viewPagerLecturas);
        fabReseña = findViewById(R.id.fabResenaGrupo);

        grupo = (GrupoLectura) getIntent().getSerializableExtra("objetoGrupo");

        if (grupo != null) {
            cargarLecturaActual(grupo.getIdGrupo());
            cargarProximaSesion(grupo.getIdGrupo());
        }

        cardLecturaActual.setOnClickListener(v -> {
            if (libroActualObj != null) {
                Intent intent = new Intent(this, com.tfg.charmreader.menu.publ.misGrupos.suscritos.LibroActual.class);
                intent.putExtra("libroSeleccionado", libroActualObj);
                startActivity(intent);
            }
        });

        fabReseña.setOnClickListener(v -> {
            if (grupo != null) {
                Intent intent = new Intent(this, ValoracionGrupo.class);
                intent.putExtra("idGrupo", grupo.getIdGrupo());
                intent.putExtra("idLibro", -1); // Siempre -1 para que sea valoración de GRUPO
                startActivity(intent);
            }
        });

        configurarViewPager();
    }

    private void cargarLecturaActual(int idGrupo) {
        apiLecturas.obtenerLibroActual(idGrupo).enqueue(new Callback<BookEn>() {
            @Override
            public void onResponse(Call<BookEn> call, Response<BookEn> response) {
                if (response.isSuccessful() && response.body() != null) {
                    libroActualObj = response.body();
                    tvLibroActual.setText(libroActualObj.getTitulo());
                    tvAutorActual.setText(libroActualObj.getAutor());

                    if (libroActualObj.getCoverId() != null) {
                        String url = "https://covers.openlibrary.org/b/id/" + libroActualObj.getCoverId() + "-M.jpg";
                        Glide.with(InfoGrupoPrivada.this).load(url).placeholder(R.drawable.ic_libro).into(ivPortadaActual);
                    }
                }
            }
            @Override public void onFailure(Call<BookEn> call, Throwable t) {}
        });
    }

    private void cargarProximaSesion(int idGrupo) {
        apiSesion.obtenerProximaSesion(idGrupo).enqueue(new Callback<Sesion>() {
            @Override
            public void onResponse(Call<Sesion> call, Response<Sesion> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Sesion s = response.body();
                    if (s.getFecha() != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        tvFechaSesion.setText(sdf.format(s.getFecha()));
                    }
                    if (s.getHora() != null) {
                        String hora = s.getHora().length() >= 5 ? s.getHora().substring(0, 5) : s.getHora();
                        tvHoraSesion.setText(hora + "h");
                    }
                    tvCapitulosSesion.setText("Cap. " + s.getCapituloInicio() + " al " + s.getCapituloFinalizacion());
                }
            }
            @Override public void onFailure(Call<Sesion> call, Throwable t) {}
        });
    }

    private void configurarViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Mediator para los nombres de las pestañas
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Propuestas" : "Historial");
        }).attach();

        // Opcional: Si quieres que el botón cambie de color o icono según la pestaña
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Aquí podrías ocultar el botón si en Historial no quisieras que apareciera,
                // pero como tú quieres que esté en ambos para valorar el grupo, lo dejamos así.
            }
        });
    }

    private static class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull AppCompatActivity activity) { super(activity); }
        @NonNull @Override public Fragment createFragment(int position) {
            return (position == 0) ? new FragmentPropuestas() : new FragmentHistorial();
        }
        @Override public int getItemCount() { return 2; }
    }
}