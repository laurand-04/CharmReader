package com.tfg.charmreader.menu.publ.misGrupos;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import com.tfg.charmreader.R;
import com.tfg.charmreader.Utilidades;
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

    private I_ApiGrupoLectura apiGrupo = API.getInstancia().create(I_ApiGrupoLectura.class);
    private I_ApiMiembro apiMiembro = API.getInstancia().create(I_ApiMiembro.class);
    private int idUsuario;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mis_grupos, container, false);

        inicializarVistas(view);
        configurarTabs();
        configurarBuscador(view);

        Utilidades.obtenerIdUsuarioDesdeAPI(id -> {
            idUsuario = id;
            if (idUsuario > 0 && isAdded()) {
                cargarDatos();
            }
        });

        return view;
    }

    private void inicializarVistas(View v) {
        rvMisGrupos = v.findViewById(R.id.rvMisGrupos);
        tabLayout = v.findViewById(R.id.tabLayoutMisGrupos);
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

        apiMiembro.obtenerGruposDondeEsMiembro(idUsuario).enqueue(new Callback<List<GrupoLectura>>() {
            @Override
            public void onResponse(Call<List<GrupoLectura>> call, Response<List<GrupoLectura>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaSuscritos = response.body();
                    if (tabLayout.getSelectedTabPosition() == 0) mostrarLista(listaSuscritos);
                }
            }
            @Override public void onFailure(Call<List<GrupoLectura>> call, Throwable t) {}
        });

        apiGrupo.obtenerGruposPorAdmin(idUsuario).enqueue(new Callback<List<GrupoLectura>>() {
            @Override
            public void onResponse(Call<List<GrupoLectura>> call, Response<List<GrupoLectura>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaCreados = response.body();
                    if (tabLayout.getSelectedTabPosition() == 1) mostrarLista(listaCreados);
                }
            }
            @Override public void onFailure(Call<List<GrupoLectura>> call, Throwable t) {}
        });
    }

    private void mostrarLista(List<GrupoLectura> lista) {
        listaActual = (lista != null) ? lista : new ArrayList<>();
        adapter.setGrupoLectura(listaActual);
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
    }

    @Override
    public void onResume() {
        super.onResume();
        if (idUsuario > 0) cargarDatos();
    }
}