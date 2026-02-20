package com.tfg.charmreader.menu.publ.misGrupos;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_ApiMiembro;
import com.tfg.charmreader.interfacesAPI.I_ApiGrupoLectura;
import com.tfg.charmreader.menu.publ.adapterReclyclerView.GrupoLecturaAdapter;
import com.tfg.charmreader.menu.publ.misGrupos.creados.ManejoGrupo;
import com.tfg.charmreader.menu.publ.misGrupos.suscritos.InfoGrupoPrivada;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.GrupoLectura;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MisGruposFragment extends Fragment {

    private RecyclerView rvMisGrupos;
    private GrupoLecturaAdapter adapter;
    private TabLayout tabLayout;
    private List<GrupoLectura> listaSuscritos = new ArrayList<>();
    private List<GrupoLectura> listaCreados = new ArrayList<>();
    private List<GrupoLectura> listaActual = new ArrayList<>();

    private LinearLayout layoutEmpty;
    private ImageView ivEmptyIcon;
    private TextView tvEmptyTitle, tvEmptySubtitle;

    private final I_ApiGrupoLectura apiGrupo = API.getInstancia().create(I_ApiGrupoLectura.class);
    private final I_ApiMiembro apiMiembro = API.getInstancia().create(I_ApiMiembro.class);
    private int idUsuario;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mis_grupos, container, false);

        // 1. Obtener ID localmente (Sin esperas ni red)
        if (getContext() != null) {
            SharedPreferences prefs = getContext().getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
            idUsuario = prefs.getInt("idUsuario", -1);
        }

        inicializarVistas(view);
        configurarTabs();
        configurarBuscador(view);

        // 2. Cargar datos si tenemos el ID
        if (idUsuario > 0) {
            cargarDatos();
        }

        return view;
    }

    private void inicializarVistas(View v) {
        rvMisGrupos = v.findViewById(R.id.rvMisGrupos);
        tabLayout = v.findViewById(R.id.tabLayoutMisGrupos);
        layoutEmpty = v.findViewById(R.id.layoutEmptyMisGrupos);
        ivEmptyIcon = v.findViewById(R.id.ivEmptyIconMisGrupos);
        tvEmptyTitle = v.findViewById(R.id.tvEmptyTitleMisGrupos);
        tvEmptySubtitle = v.findViewById(R.id.tvEmptySubtitleMisGrupos);

        rvMisGrupos.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new GrupoLecturaAdapter(new ArrayList<>(), grupo -> {
            Intent intent;
            if (tabLayout.getSelectedTabPosition() == 0) {
                intent = new Intent(getActivity(), InfoGrupoPrivada.class);
            } else {
                intent = new Intent(getActivity(), ManejoGrupo.class);
            }
            intent.putExtra("objetoGrupo", grupo);
            startActivity(intent);
        });

        rvMisGrupos.setAdapter(adapter);
    }

    private void configurarTabs() {
        if (tabLayout.getTabCount() == 0) {
            tabLayout.addTab(tabLayout.newTab().setText("Suscritos"));
            tabLayout.addTab(tabLayout.newTab().setText("Creados"));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mostrarLista(tab.getPosition() == 0 ? listaSuscritos : listaCreados);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void cargarDatos() {
        if (idUsuario <= 0) return;

        // Cargar Grupos Suscritos
        apiMiembro.obtenerGruposDondeEsMiembro(idUsuario).enqueue(new Callback<List<GrupoLectura>>() {
            @Override
            public void onResponse(Call<List<GrupoLectura>> call, Response<List<GrupoLectura>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaSuscritos = response.body();
                    if (isAdded() && tabLayout.getSelectedTabPosition() == 0) {
                        mostrarLista(listaSuscritos);
                    }
                }
            }
            @Override public void onFailure(Call<List<GrupoLectura>> call, Throwable t) {
                if (isAdded() && tabLayout.getSelectedTabPosition() == 0) actualizarEstadoVacio(true);
            }
        });

        // Cargar Grupos Creados
        apiGrupo.obtenerGruposPorAdmin(idUsuario).enqueue(new Callback<List<GrupoLectura>>() {
            @Override
            public void onResponse(Call<List<GrupoLectura>> call, Response<List<GrupoLectura>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaCreados = response.body();
                    if (isAdded() && tabLayout.getSelectedTabPosition() == 1) {
                        mostrarLista(listaCreados);
                    }
                }
            }
            @Override public void onFailure(Call<List<GrupoLectura>> call, Throwable t) {
                if (isAdded() && tabLayout.getSelectedTabPosition() == 1) actualizarEstadoVacio(true);
            }
        });
    }

    private void mostrarLista(List<GrupoLectura> lista) {
        listaActual = (lista != null) ? lista : new ArrayList<>();
        if (adapter != null) {
            adapter.setGrupoLectura(listaActual);
        }
        actualizarEstadoVacio(listaActual.isEmpty());
    }

    private void actualizarEstadoVacio(boolean estaVacio) {
        if (!isAdded()) return;

        if (estaVacio) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvMisGrupos.setVisibility(View.GONE);

            if (tabLayout.getSelectedTabPosition() == 0) {
                ivEmptyIcon.setImageResource(R.drawable.ic_people);
                tvEmptyTitle.setText("¡Estás solo por aquí!");
                tvEmptySubtitle.setText("Aún no te has unido a ningún grupo.");
            } else {
                ivEmptyIcon.setImageResource(R.drawable.ic_libro);
                tvEmptyTitle.setText("¿Tienes una idea?");
                tvEmptySubtitle.setText("Aún no has creado ningún grupo.");
            }
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvMisGrupos.setVisibility(View.VISIBLE);
        }
    }

    private void configurarBuscador(View v) {
        SearchView searchView = v.findViewById(R.id.searchViewMisGrupos);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                filtrar(newText);
                return true;
            }
        });
    }

    private void filtrar(String texto) {
        List<GrupoLectura> filtrada = new ArrayList<>();
        for (GrupoLectura g : listaActual) {
            if (g.getNombre().toLowerCase().contains(texto.toLowerCase())) {
                filtrada.add(g);
            }
        }
        adapter.setGrupoLectura(filtrada);
        actualizarEstadoVacio(filtrada.isEmpty());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (idUsuario > 0) cargarDatos();
    }
}