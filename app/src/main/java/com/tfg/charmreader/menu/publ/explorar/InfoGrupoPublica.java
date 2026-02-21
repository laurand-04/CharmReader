package com.tfg.charmreader.menu.publ.explorar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_APICatalogo;
import com.tfg.charmreader.interfacesAPI.I_ApiBook;
import com.tfg.charmreader.interfacesAPI.I_ApiMiembro;
import com.tfg.charmreader.interfacesAPI.I_ApiValoracion;
import com.tfg.charmreader.menu.priv.adapterRecyclerView.BookIntAdapter;
import com.tfg.charmreader.menu.publ.adapterReclyclerView.ValoracionAdapter;
import com.tfg.charmreader.menu.publ.misGrupos.creados.ManejoGrupo;
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
    private MaterialButton btnAccionUnirse, btnAccionAbandonar, btnGestionar;
    private MaterialCardView cardFoto;

    private LinearLayout layoutEmpty;
    private ImageView ivEmptyIcon;
    private TextView tvEmptyTitle, tvEmptySubtitle;

    private final List<BookEn> listaLibrosGlobal = new ArrayList<>();
    private List<Valoracion> listaResenasGlobal = new ArrayList<>();

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

        SharedPreferences prefs = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        idUsuarioLogueado = prefs.getInt("idUsuario", -1);

        if (idUsuarioLogueado == -1) {
            Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        inicializarVistas();
        configurarTabs();
        cargarDatosGrupo();
        continuarCargando();
    }

    private void inicializarVistas() {
        tvNombre = findViewById(R.id.tvNombreDetalle);
        tvUbicacion = findViewById(R.id.tvUbicacionDetalle);
        tvDescription = findViewById(R.id.tvDescDetalle);
        ivFotoGrupo = findViewById(R.id.ivDetalleGrupoFoto);
        btnBack = findViewById(R.id.btnBackInfoPublica);

        btnAccionUnirse = findViewById(R.id.btnAccionUnirse);
        btnAccionAbandonar = findViewById(R.id.btnAccionAbandonar);
        btnGestionar = findViewById(R.id.btnGestionarPublico);
        cardFoto = findViewById(R.id.cardFotoPerfil);

        tabLayout = findViewById(R.id.tabLayout);
        rvLibros = findViewById(R.id.rvLibrosLeidos);
        rvComentarios = findViewById(R.id.rvComentariosGrupo);
        layoutEmpty = findViewById(R.id.layoutEmptyGrupo);
        ivEmptyIcon = findViewById(R.id.ivEmptyIcon);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptySubtitle = findViewById(R.id.tvEmptySubtitle);

        rvLibros.setLayoutManager(new LinearLayoutManager(this));
        rvComentarios.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());

        btnGestionar.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManejoGrupo.class);
            intent.putExtra("objetoGrupo", grupo);
            startActivity(intent);
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
            Glide.with(this).load(grupo.getUrl()).placeholder(R.drawable.ic_people).centerCrop().into(ivFotoGrupo);
        }
    }

    private void verificarSuscripcion() {
        if (grupo.getIdUsuario() == idUsuarioLogueado) {
            actualizarBotones(true);
            return;
        }

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
        if (grupo.getIdUsuario() == idUsuarioLogueado) {
            // MODO ADMINISTRADOR
            btnAccionUnirse.setVisibility(View.GONE);
            btnAccionAbandonar.setVisibility(View.GONE);
            btnGestionar.setVisibility(View.VISIBLE);
            cardFoto.setStrokeWidth(4); // Resalte visual en la foto
            return;
        }

        // MODO USUARIO NORMAL
        btnGestionar.setVisibility(View.GONE);
        cardFoto.setStrokeWidth(0);

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
        listaLibrosGlobal.clear();
        bookAdapter = new BookIntAdapter(listaLibrosGlobal, b -> { });
        rvLibros.setAdapter(bookAdapter);

        apiCatalogo.verCatalogo(grupo.getIdGrupo()).enqueue(new Callback<List<CatalogoLectura>>() {
            @Override
            public void onResponse(Call<List<CatalogoLectura>> call, Response<List<CatalogoLectura>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    I_ApiBook apiBook = API.getInstancia().create(I_ApiBook.class);
                    List<CatalogoLectura> filtrados = new ArrayList<>();
                    for(CatalogoLectura c : response.body()){
                        if(c.getEstado() == CatalogoLectura.EstadoLectura.FINALIZADO) filtrados.add(c);
                    }

                    if(filtrados.isEmpty()){
                        actualizarEstadoVacio();
                        return;
                    }

                    for (CatalogoLectura item : filtrados) {
                        apiBook.obtenerBookPorId(item.getIdBook()).enqueue(new Callback<BookEn>() {
                            @Override
                            public void onResponse(Call<BookEn> call, Response<BookEn> rb) {
                                if (rb.isSuccessful() && rb.body() != null) {
                                    listaLibrosGlobal.add(rb.body());
                                    bookAdapter.notifyItemInserted(listaLibrosGlobal.size() - 1);
                                    actualizarEstadoVacio();
                                }
                            }
                            @Override public void onFailure(Call<BookEn> call, Throwable t) { }
                        });
                    }
                } else {
                    actualizarEstadoVacio();
                }
            }
            @Override public void onFailure(Call<List<CatalogoLectura>> call, Throwable t) { actualizarEstadoVacio(); }
        });
    }

    private void cargarComentarios() {
        apiValoracion.verValoraciones(Valoracion.TipoValoracion.GRUPO, grupo.getIdGrupo()).enqueue(new Callback<List<Valoracion>>() {
            @Override
            public void onResponse(Call<List<Valoracion>> call, Response<List<Valoracion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaResenasGlobal = response.body();
                    valoracionAdapter = new ValoracionAdapter(listaResenasGlobal, null);
                    rvComentarios.setAdapter(valoracionAdapter);
                }
                actualizarEstadoVacio();
            }
            @Override public void onFailure(Call<List<Valoracion>> call, Throwable t) { actualizarEstadoVacio(); }
        });
    }

    private void actualizarEstadoVacio() {
        runOnUiThread(() -> {
            int tabActiva = tabLayout.getSelectedTabPosition();

            if (tabActiva == 0) { // Libros
                rvComentarios.setVisibility(View.GONE);
                if (listaLibrosGlobal.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    rvLibros.setVisibility(View.GONE);
                    ivEmptyIcon.setImageResource(R.drawable.ic_libro);
                    tvEmptyTitle.setText("¡Estantería vacía!");
                    tvEmptySubtitle.setText("Sin lecturas finalizadas.");
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                    rvLibros.setVisibility(View.VISIBLE);
                }
            } else { // Reseñas
                rvLibros.setVisibility(View.GONE);
                if (listaResenasGlobal.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    rvComentarios.setVisibility(View.GONE);
                    ivEmptyIcon.setImageResource(R.drawable.ic_people);
                    tvEmptyTitle.setText("Sin opiniones");
                    tvEmptySubtitle.setText("Sé el primero en dejar tu reseña.");
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                    rvComentarios.setVisibility(View.VISIBLE);
                }
            }
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
                actualizarEstadoVacio();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });
    }
}