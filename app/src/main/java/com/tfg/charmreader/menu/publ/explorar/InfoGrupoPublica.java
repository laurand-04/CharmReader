package com.tfg.charmreader.menu.publ.explorar;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.tfg.charmreader.R;
import com.tfg.charmreader.Utilidades;
import com.tfg.charmreader.interfacesAPI.I_APICatalogo;
import com.tfg.charmreader.interfacesAPI.I_ApiBook;
import com.tfg.charmreader.interfacesAPI.I_ApiMiembro;
import com.tfg.charmreader.interfacesAPI.I_ApiValoracion;
import com.tfg.charmreader.menu.priv.adapterRecyclerView.BookIntAdapter;
import com.tfg.charmreader.menu.publ.adapterReclyclerView.ValoracionAdapter;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.BookEn;
import com.tfg.charmreader.objetosBD.CatalogoLectura;
import com.tfg.charmreader.objetosBD.GrupoLectura;
import com.tfg.charmreader.objetosBD.Miembro;
import com.tfg.charmreader.objetosBD.Valoracion;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InfoGrupoPublica extends AppCompatActivity {

    private GrupoLectura grupo;
    private TextView tvNombre, tvUbicacion, tvDescription;
    private ImageView ivFotoGrupo, btnBack;
    private TabLayout tabLayout;
    private RecyclerView rvLibros, rvComentarios;
    private BookIntAdapter bookAdapter;
    private ValoracionAdapter valoracionAdapter;
    private MaterialButton btnAccionUnirse, btnAccionAbandonar;

    private final I_ApiMiembro apiMiembro = API.getInstancia().create(I_ApiMiembro.class);
    private final I_APICatalogo apiCatalogo = API.getInstancia().create(I_APICatalogo.class);
    private final I_ApiValoracion apiValoracion = API.getInstancia().create(I_ApiValoracion.class);

    private int idUsuarioLogueado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        setContentView(R.layout.activity_info_grupo_publica);

        grupo = (GrupoLectura) getIntent().getSerializableExtra("objetoGrupo");
        if (grupo == null) {
            finish();
            return;
        }

        inicializarVistas();
        configurarTabs();
        cargarDatosGrupo();
        obtenerIdUsuarioAsincrono();
    }

    private void inicializarVistas() {
        tvNombre = findViewById(R.id.tvNombreDetalle);
        tvUbicacion = findViewById(R.id.tvUbicacionDetalle);
        tvDescription = findViewById(R.id.tvDescDetalle);
        ivFotoGrupo = findViewById(R.id.ivDetalleGrupoFoto);
        btnBack = findViewById(R.id.btnBackInfoPublica);

        btnAccionUnirse = findViewById(R.id.btnAccionUnirse);
        btnAccionAbandonar = findViewById(R.id.btnAccionAbandonar);

        tabLayout = findViewById(R.id.tabLayout);
        rvLibros = findViewById(R.id.rvLibrosLeidos);
        rvComentarios = findViewById(R.id.rvComentariosGrupo);

        rvLibros.setLayoutManager(new LinearLayoutManager(this));
        rvComentarios.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());
    }

    private void obtenerIdUsuarioAsincrono() {
        Utilidades.obtenerIdUsuarioDesdeAPI(idUsuario -> {
            idUsuarioLogueado = idUsuario;
            runOnUiThread(this::continuarCargando);
        });
    }

    private void continuarCargando() {
        verificarSuscripcion();
        cargarLibrosLeidos();
        cargarComentarios();
    }

    private void cargarDatosGrupo() {
        tvNombre.setText(grupo.getNombre());
        tvUbicacion.setText(grupo.getUbicacion());
        tvDescription.setText(grupo.getDescripcion());

        if (grupo.getUrl() != null && !grupo.getUrl().isEmpty()) {
            Glide.with(this)
                    .load(grupo.getUrl())
                    .placeholder(R.drawable.ic_people)
                    .error(R.drawable.ic_people)
                    .centerCrop()
                    .into(ivFotoGrupo);
        }
    }

    private void verificarSuscripcion() {
        apiMiembro.obtenerPorGrupo(grupo.getIdGrupo()).enqueue(new Callback<List<Miembro>>() {
            @Override
            public void onResponse(Call<List<Miembro>> call, Response<List<Miembro>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean esMiembro = false;
                    for (Miembro m : response.body()) {
                        if (m.getIdUsuario() == idUsuarioLogueado) {
                            esMiembro = true;
                            break;
                        }
                    }
                    actualizarBotones(esMiembro);
                }
            }
            @Override public void onFailure(Call<List<Miembro>> call, Throwable t) {}
        });
    }

    private void actualizarBotones(boolean esMiembro) {
        if (esMiembro) {
            btnAccionUnirse.setVisibility(View.GONE);
            btnAccionAbandonar.setVisibility(View.VISIBLE);
            btnAccionAbandonar.setOnClickListener(v -> salirDelGrupo());
        } else {
            btnAccionUnirse.setVisibility(View.VISIBLE);
            btnAccionAbandonar.setVisibility(View.GONE);
            btnAccionUnirse.setOnClickListener(v -> unirseAlGrupo());
        }
    }

    private void unirseAlGrupo() {
        Miembro nuevoMiembro = new Miembro(grupo.getIdGrupo(), idUsuarioLogueado);
        apiMiembro.unirse(nuevoMiembro).enqueue(new Callback<Miembro>() {
            @Override
            public void onResponse(Call<Miembro> call, Response<Miembro> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(InfoGrupoPublica.this, "¡Te has unido!", Toast.LENGTH_SHORT).show();
                    actualizarBotones(true);
                }
            }
            @Override public void onFailure(Call<Miembro> call, Throwable t) {}
        });
    }

    private void salirDelGrupo() {
        apiMiembro.salirDeGrupo(grupo.getIdGrupo(), idUsuarioLogueado).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful() || response.code() == 204) {
                    Toast.makeText(InfoGrupoPublica.this, "Has salido del grupo", Toast.LENGTH_SHORT).show();
                    actualizarBotones(false);
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void cargarLibrosLeidos() {
        final List<BookEn> librosDetallados = new ArrayList<>();
        bookAdapter = new BookIntAdapter(librosDetallados, b -> { });
        rvLibros.setAdapter(bookAdapter);

        apiCatalogo.verCatalogo(grupo.getIdGrupo()).enqueue(new Callback<List<CatalogoLectura>>() {
            @Override
            public void onResponse(Call<List<CatalogoLectura>> call, Response<List<CatalogoLectura>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    I_ApiBook apiBook = API.getInstancia().create(I_ApiBook.class);
                    for (CatalogoLectura item : response.body()) {
                        if (item.getEstado() == CatalogoLectura.EstadoLectura.FINALIZADO) {
                            apiBook.obtenerBookPorId(item.getIdBook()).enqueue(new Callback<BookEn>() {
                                @Override
                                public void onResponse(Call<BookEn> call, Response<BookEn> rb) {
                                    if (rb.isSuccessful() && rb.body() != null) {
                                        librosDetallados.add(rb.body());
                                        bookAdapter.notifyItemInserted(librosDetallados.size() - 1);
                                    }
                                }
                                @Override public void onFailure(Call<BookEn> call, Throwable t) { }
                            });
                        }
                    }
                }
            }
            @Override public void onFailure(Call<List<CatalogoLectura>> call, Throwable t) { }
        });
    }

    private void cargarComentarios() {
        apiValoracion.verValoraciones(Valoracion.TipoValoracion.GRUPO, grupo.getIdGrupo()).enqueue(new Callback<List<Valoracion>>() {
            @Override
            public void onResponse(Call<List<Valoracion>> call, Response<List<Valoracion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    valoracionAdapter = new ValoracionAdapter(response.body(), null);
                    rvComentarios.setAdapter(valoracionAdapter);
                }
            }
            @Override public void onFailure(Call<List<Valoracion>> call, Throwable t) { }
        });
    }

    private void configurarTabs() {
        if (tabLayout.getTabCount() == 0) {
            tabLayout.addTab(tabLayout.newTab().setText("Libros"));
            tabLayout.addTab(tabLayout.newTab().setText("Reseñas"));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    rvLibros.setVisibility(View.VISIBLE);
                    rvComentarios.setVisibility(View.GONE);
                } else {
                    rvLibros.setVisibility(View.GONE);
                    rvComentarios.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });
    }
}