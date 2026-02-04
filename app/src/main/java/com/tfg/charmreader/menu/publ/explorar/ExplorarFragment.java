package com.tfg.charmreader.menu.publ.explorar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    private List<GrupoLectura> listaGL = new ArrayList<>();
    private FloatingActionButton fabCrear; // 1. Declarar el botón

    private I_ApiGrupoLectura apiGrupoLectura = API.getInstancia().create(I_ApiGrupoLectura.class);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explorar, container, false);

        rvGrupos = view.findViewById(R.id.rvGrupos);
        fabCrear = view.findViewById(R.id.fabCrearGrupo); // 2. Vincular
        rvGrupos.setLayoutManager(new LinearLayoutManager(getContext()));
        SearchView searchView = view.findViewById(R.id.searchView);

        adapter = new GrupoLecturaAdapter(new ArrayList<>(), grupo -> {
            Intent intent = new Intent(getActivity(), InfoGrupoPublica.class);
            intent.putExtra("objetoGrupo", grupo);
            startActivity(intent);
        });

        rvGrupos.setAdapter(adapter);

        // 3. Listener para abrir el formulario de Nuevo Grupo
        fabCrear.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NuevoGrupo.class);
            startActivity(intent);
        });

        cargarGrupos();

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
                filtrar(newText);
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
        cargarGrupos(); // Refresca la lista si el usuario creó un grupo y volvió
    }

    public void mostrarAlerta(String titulo, String contenido) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(titulo);
        builder.setMessage(contenido);
        builder.setPositiveButton("Entendido", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void filtrar(String texto) {
        if (listaGL == null) return;
        List<GrupoLectura> filtrada = new ArrayList<>();
        for (GrupoLectura grupo : listaGL) {
            if (grupo.getNombre().toLowerCase().contains(texto.toLowerCase()) ||
                    grupo.getUbicacion().toLowerCase().contains(texto.toLowerCase())) {
                filtrada.add(grupo);
            }
        }
        adapter.setGrupoLectura(filtrada);
    }

    private void cargarGrupos() {
        new Thread(() -> {
            try {
                retrofit2.Response<List<GrupoLectura>> response = apiGrupoLectura.obtenerGrupos().execute();
                if (response.isSuccessful() && response.body() != null) {
                    listaGL = response.body();
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (adapter != null) {
                                adapter.setGrupoLectura(listaGL);
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Log.e("ExplorarFragment", "Error cargando grupos: ", e);
            }
        }).start();
    }
}