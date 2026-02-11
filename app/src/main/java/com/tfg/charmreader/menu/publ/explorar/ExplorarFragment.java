package com.tfg.charmreader.menu.publ.explorar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_ApiGrupoLectura;
import com.tfg.charmreader.menu.publ.adapterReclyclerView.GrupoLecturaAdapter;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.GrupoLectura;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExplorarFragment extends Fragment {

    private RecyclerView rvGrupos;
    private GrupoLecturaAdapter adapter;
    private FloatingActionButton fabCrear;
    private LinearLayout layoutEmpty; // Para mostrar si no hay grupos

    private I_ApiGrupoLectura apiGrupoLectura = API.getInstancia().create(I_ApiGrupoLectura.class);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Asegúrate de que el XML se llame fragment_explorar o fragment_mis_grupos según corresponda
        View view = inflater.inflate(R.layout.fragment_explorar, container, false);

        // 1. Vinculación de vistas con los IDs del XML limpio
        rvGrupos = view.findViewById(R.id.rvGrupos);
        fabCrear = view.findViewById(R.id.fabCrearGrupo);
        layoutEmpty = view.findViewById(R.id.layoutEmptyGrupos);
        SearchView searchView = view.findViewById(R.id.searchViewGrupos);

        rvGrupos.setLayoutManager(new LinearLayoutManager(getContext()));

        // 2. Configuración del Adaptador con el listener de clic
        adapter = new GrupoLecturaAdapter(new ArrayList<>(), grupo -> {
            Intent intent = new Intent(getActivity(), InfoGrupoPublica.class);
            intent.putExtra("objetoGrupo", grupo);
            startActivity(intent);
        });

        rvGrupos.setAdapter(adapter);

        // 3. Botón flotante para nuevo grupo
        fabCrear.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NuevoGrupo.class);
            startActivity(intent);
        });

        cargarGrupos();

        // 4. Lógica del buscador (usando el método filtrar del adaptador)
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    ejecutarBusqueda(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Usamos la lógica de filtrado que ya tiene el adaptador
                if (adapter != null) {
                    adapter.filtrar(newText);
                }
                return true;
            }
        });

        return view;
    }

    private void ejecutarBusqueda(String query) {
        new Thread(() -> {
            try {
                retrofit2.Response<GrupoLectura> response = apiGrupoLectura.buscarGrupoPorNombre(query).execute();
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (response.isSuccessful() && response.body() != null) {
                            Intent intent = new Intent(getActivity(), InfoGrupoPublica.class);
                            intent.putExtra("objetoGrupo", response.body());
                            startActivity(intent);
                        } else {
                            mostrarAlerta("Aviso", "No se ha encontrado el grupo");
                        }
                    });
                }
            } catch (IOException e) {
                Log.e("ExplorarFragment", "Error en búsqueda", e);
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarGrupos();
    }

    private void cargarGrupos() {
        new Thread(() -> {
            try {
                retrofit2.Response<List<GrupoLectura>> response = apiGrupoLectura.obtenerGrupos().execute();
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (response.isSuccessful() && response.body() != null) {
                            List<GrupoLectura> lista = response.body();

                            // Gestionar visibilidad del layout vacío
                            if (lista.isEmpty()) {
                                layoutEmpty.setVisibility(View.VISIBLE);
                                rvGrupos.setVisibility(View.GONE);
                            } else {
                                layoutEmpty.setVisibility(View.GONE);
                                rvGrupos.setVisibility(View.VISIBLE);
                                adapter.setGrupoLectura(lista);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("ExplorarFragment", "Error cargando grupos: ", e);
            }
        }).start();
    }

    public void mostrarAlerta(String titulo, String contenido) {
        if (getContext() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle(titulo);
            builder.setMessage(contenido);
            builder.setPositiveButton("Entendido", (dialog, which) -> dialog.dismiss());
            builder.show();
        }
    }
}