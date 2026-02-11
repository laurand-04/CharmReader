package com.tfg.charmreader.menu.priv.estanteria;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tfg.charmreader.Utilidades;
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

    public EstanteriaFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_estanteria, container, false);

        rvEstanterias = view.findViewById(R.id.recyclerEstanterias);
        rvEstanterias.setLayoutManager(new LinearLayoutManager(getContext()));

        // Configuración del adaptador con la corrección del Intent
        adapter = new EstanteriasAdapter(new ArrayList<>(), estanteria -> {
            Intent intent = new Intent(getActivity(), LibrosEstanteria.class);
            intent.putExtra("Nombre", estanteria.getNombre());
            intent.putExtra("idEstanteria", estanteria.getId());
            // 🔥 CORRECCIÓN: Enviamos el color para que la pantalla de libros sea dinámica
            intent.putExtra("Color", estanteria.getColor());
            startActivity(intent);
        });

        rvEstanterias.setAdapter(adapter);

        searchView = view.findViewById(R.id.searchViewEstanterias);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) adapter.filtrar(newText);
                return true;
            }
        });

        FloatingActionButton fab = view.findViewById(R.id.fab_add_estanteria);
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
        new Thread(() -> {
            int idUsuario = Utilidades.obtenerIdUsuarioDesdeAPI();
            if (idUsuario == -1) {
                Log.e("EstanteriaFragment", "ID de usuario no encontrado");
                return;
            }
            try {
                I_ApiEstanteria apiEstanteria = API.getInstancia().create(I_ApiEstanteria.class);
                I_ApiLibrosDeUsuario apiLibrosUsuario = API.getInstancia().create(I_ApiLibrosDeUsuario.class);

                retrofit2.Response<List<Estanteria>> response = apiEstanteria.obtenerEstanteriasDeUsuario(idUsuario).execute();

                if (response.isSuccessful() && response.body() != null) {
                    List<Estanteria> listaRecibida = response.body();

                    // Obtenemos el conteo de libros de la tabla libros_de_usuario para cada estantería
                    for (Estanteria e : listaRecibida) {
                        retrofit2.Response<Integer> resConteo = apiLibrosUsuario.contarLibrosEnEstanteria(e.getId()).execute();
                        if (resConteo.isSuccessful() && resConteo.body() != null) {
                            e.setCantidadLibros(resConteo.body());
                        }
                    }

                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (adapter != null) {
                                adapter.setEstanterias(listaRecibida);
                            }
                        });
                    }
                } else {
                    Log.e("EstanteriaFragment", "Error en la respuesta de la API: " + response.code());
                }
            } catch (Exception e) {
                Log.e("EstanteriaFragment", "Excepción al cargar estanterías", e);
            }
        }).start();
    }
}