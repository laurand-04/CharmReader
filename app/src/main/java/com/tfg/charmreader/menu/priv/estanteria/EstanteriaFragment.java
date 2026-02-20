package com.tfg.charmreader.menu.priv.estanteria;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tfg.charmreader.interfacesAPI.I_ApiEstanteria;
import com.tfg.charmreader.interfacesAPI.I_ApiLibrosDeUsuario;
import com.tfg.charmreader.menu.priv.adapterRecyclerView.EstanteriasAdapter;
import com.tfg.charmreader.R;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.Estanteria;

import java.util.ArrayList;
import java.util.List;

public class EstanteriaFragment extends Fragment {

    private RecyclerView rvEstanterias;
    private EstanteriasAdapter adapter;
    private SearchView searchView;
    private LinearLayout layoutEmpty;

    public EstanteriaFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_estanteria, container, false);

        rvEstanterias = view.findViewById(R.id.recyclerEstanterias);
        layoutEmpty = view.findViewById(R.id.layoutEmptyEstanteria);
        searchView = view.findViewById(R.id.searchViewEstanterias);
        FloatingActionButton fab = view.findViewById(R.id.fab_add_estanteria);

        rvEstanterias.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EstanteriasAdapter(new ArrayList<>(), estanteria -> {
            Intent intent = new Intent(getActivity(), LibrosEstanteria.class);
            intent.putExtra("Nombre", estanteria.getNombre());
            intent.putExtra("idEstanteria", estanteria.getId());
            intent.putExtra("Color", estanteria.getColor());
            startActivity(intent);
        });

        rvEstanterias.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) adapter.filtrar(newText);
                return true;
            }
        });

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CargarNuevaEstanteria.class);
            startActivityForResult(intent, 123);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cargarEstanterias();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            cargarEstanterias();
        }
    }

    private void cargarEstanterias() {
        // 1. En Fragmentos, el contexto se obtiene con requireContext() o getContext()
        if (getContext() == null) return;

        SharedPreferences prefs = getContext().getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);

        if (idUsuario == -1) {
            Log.e("EstanteriaFragment", "ID no encontrado");
            mostrarEstadoVacio(true);
            return;
        }

        new Thread(() -> {
            try {
                I_ApiEstanteria apiEstanteria = API.getInstancia().create(I_ApiEstanteria.class);
                I_ApiLibrosDeUsuario apiLibrosUsuario = API.getInstancia().create(I_ApiLibrosDeUsuario.class);

                retrofit2.Response<List<Estanteria>> response = apiEstanteria.obtenerEstanteriasDeUsuario(idUsuario).execute();

                if (response.isSuccessful() && response.body() != null) {
                    List<Estanteria> listaRecibida = response.body();

                    if (listaRecibida.isEmpty()) {
                        mostrarEstadoVacio(true);
                        return;
                    }

                    for (Estanteria e : listaRecibida) {
                        retrofit2.Response<Integer> resConteo = apiLibrosUsuario.contarLibrosEnEstanteria(e.getId()).execute();
                        if (resConteo.isSuccessful() && resConteo.body() != null) {
                            e.setCantidadLibros(resConteo.body());
                        }
                    }

                    // 2. Usamos getActivity().runOnUiThread porque estamos en un Fragment
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            mostrarEstadoVacio(false);
                            adapter.setEstanterias(listaRecibida);
                        });
                    }
                } else {
                    mostrarEstadoVacio(true);
                }
            } catch (Exception e) {
                Log.e("EstanteriaFragment", "Error", e);
                mostrarEstadoVacio(true);
            }
        }).start();
    }

    private void mostrarEstadoVacio(boolean estaVacio) {
        // En Fragmentos siempre comprobar isAdded() antes de tocar la UI desde un hilo
        if (isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (estaVacio) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    rvEstanterias.setVisibility(View.GONE);
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                    rvEstanterias.setVisibility(View.VISIBLE);
                }
            });
        }
    }
}