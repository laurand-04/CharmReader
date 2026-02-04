package com.tfg.charmreader.menu.publ.explorar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import com.tfg.charmreader.R;
import com.tfg.charmreader.Utilidades;
import com.tfg.charmreader.interfacesAPI.I_APICatalogo;
import com.tfg.charmreader.interfacesAPI.I_ApiMiembro;
import com.tfg.charmreader.interfacesAPI.I_ApiValoracion;
import com.tfg.charmreader.interfacesAPI.I_ApiBook;
import com.tfg.charmreader.menu.priv.adapterRecyclerView.BookIntAdapter;
import com.tfg.charmreader.menu.publ.adapterReclyclerView.ValoracionAdapter;
import com.tfg.charmreader.objetosBD.*;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InfoGrupoPublica extends AppCompatActivity {

    private GrupoLectura grupo;
    private TextView tvNombre, tvUbicacion, tvDescription;
    private TabLayout tabLayout;
    private RecyclerView rvLibros, rvComentarios;
    private BookIntAdapter bookAdapter;
    private ValoracionAdapter valoracionAdapter;
    private Button btnSuscribirse, btnDesubscribirse;

    private final I_ApiMiembro apiMiembro = API.getInstancia().create(I_ApiMiembro.class);
    private final I_APICatalogo apiCatalogo = API.getInstancia().create(I_APICatalogo.class);
    private final I_ApiValoracion apiValoracion = API.getInstancia().create(I_ApiValoracion.class);

    private int idUsuarioLogueado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_grupo_publica);

        grupo = (GrupoLectura) getIntent().getSerializableExtra("objetoGrupo");
        if (grupo == null) {
            finish();
            return;
        }

        inicializarVistas();
        configurarTabs();
        cargarDatosGrupo();

        // PASO 1: Obtener el ID de forma asíncrona para evitar el crash
        obtenerIdUsuarioAsincrono();
    }

    private void obtenerIdUsuarioAsincrono() {
        // Llamamos al método y le pasamos lo que queremos que haga cuando el ID llegue
        Utilidades.obtenerIdUsuarioDesdeAPI(new Utilidades.IdUsuarioCallback() {
            @Override
            public void onIdCargado(int idUsuario) {
                idUsuarioLogueado = idUsuario;

                // IMPORTANTE: Solo cuando ya tenemos el ID, disparamos las demás peticiones
                // que dependen de ese ID (como verificarSuscripcion)
                runOnUiThread(() -> {
                    continuarCargando();
                });
            }
        });
    }

    private void continuarCargando() {
        verificarSuscripcion();
        cargarLibrosLeidos();
        cargarComentarios();
    }

    private void inicializarVistas() {
        tvNombre = findViewById(R.id.tvNombreDetalle);
        tvUbicacion = findViewById(R.id.tvUbicacionDetalle);
        tvDescription = findViewById(R.id.tvDescDetalle);
        tabLayout = findViewById(R.id.tabLayout);
        rvLibros = findViewById(R.id.rvLibrosLeidos);
        rvComentarios = findViewById(R.id.rvComentariosGrupo);
        btnSuscribirse = findViewById(R.id.btnSuscribirse);
        btnDesubscribirse = findViewById(R.id.btnDesubscribirse);

        rvLibros.setLayoutManager(new LinearLayoutManager(this));
        rvComentarios.setLayoutManager(new LinearLayoutManager(this));
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
            btnSuscribirse.setVisibility(View.GONE);
            btnDesubscribirse.setVisibility(View.VISIBLE);
            btnDesubscribirse.setOnClickListener(v -> salirDelGrupo());
        } else {
            btnSuscribirse.setVisibility(View.VISIBLE);
            btnDesubscribirse.setVisibility(View.GONE);
            btnSuscribirse.setOnClickListener(v -> unirseAlGrupo());
        }
    }

    private void unirseAlGrupo() {
        Miembro nuevoMiembro = new Miembro(grupo.getIdGrupo(), idUsuarioLogueado);
        apiMiembro.unirse(nuevoMiembro).enqueue(new Callback<Miembro>() {
            @Override
            public void onResponse(Call<Miembro> call, Response<Miembro> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(InfoGrupoPublica.this, "¡Bienvenido al grupo!", Toast.LENGTH_SHORT).show();
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
        // 1. Inicializamos la lista y el adapter de una vez (vacío al principio)
        final List<BookEn> librosDetallados = new ArrayList<>();
        if (bookAdapter == null) {
            bookAdapter = new BookIntAdapter(librosDetallados, b -> {
                // Acción al pulsar el libro si lo deseas
            });
            rvLibros.setAdapter(bookAdapter);
        }

        apiCatalogo.verCatalogo(grupo.getIdGrupo()).enqueue(new Callback<List<CatalogoLectura>>() {
            @Override
            public void onResponse(Call<List<CatalogoLectura>> call, Response<List<CatalogoLectura>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CatalogoLectura> listaCatalogo = response.body();

                    // Filtramos solo los finalizados
                    for (CatalogoLectura item : listaCatalogo) {
                        if (item.getEstado() == CatalogoLectura.EstadoLectura.FINALIZADO) {

                            // Pedimos los detalles del libro
                            I_ApiBook apiBook = API.getInstancia().create(I_ApiBook.class);
                            apiBook.obtenerBookPorId(item.getIdBook()).enqueue(new Callback<BookEn>() {
                                @Override
                                public void onResponse(Call<BookEn> call, Response<BookEn> rb) {
                                    if (rb.isSuccessful() && rb.body() != null) {
                                        // Añadimos a la lista y NOTIFICAMOS al adapter
                                        librosDetallados.add(rb.body());
                                        bookAdapter.notifyItemInserted(librosDetallados.size() - 1);
                                    }
                                }
                                @Override public void onFailure(Call<BookEn> call, Throwable t) {
                                    Log.e("API_BOOK", "Error al obtener libro: " + t.getMessage());
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<CatalogoLectura>> call, Throwable t) {
                Toast.makeText(InfoGrupoPublica.this, "Error al cargar catálogo", Toast.LENGTH_SHORT).show();
            }
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
            @Override public void onFailure(Call<List<Valoracion>> call, Throwable t) {}
        });
    }

    private void cargarDatosGrupo() {
        tvNombre.setText(grupo.getNombre());
        tvUbicacion.setText(grupo.getUbicacion());
        tvDescription.setText(grupo.getDescripcion());
    }

    private void configurarTabs() {
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
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
}