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

    private I_ApiGrupoLectura apiGrupoLectura = API.getInstancia().create(I_ApiGrupoLectura.class);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explorar, container, false);

        rvGrupos = view.findViewById(R.id.rvGrupos);
        rvGrupos.setLayoutManager(new LinearLayoutManager(getContext()));
        SearchView searchView = view.findViewById(R.id.searchView);

        // 1. Configurar el Adapter ANTES de cargar los datos
        adapter = new GrupoLecturaAdapter(new ArrayList<>(), grupo -> {
            Intent intent = new Intent(getActivity(), InfoGrupoPublica.class);
            intent.putExtra("objetoGrupo", grupo);
            startActivity(intent);
        });

        rvGrupos.setAdapter(adapter);

        // 2. Cargar datos
        cargarGrupos();

        // 3. Buscador
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    // Ejecutamos en un hilo secundario para no bloquear la app
                    new Thread(() -> {
                        try {
                            retrofit2.Response<GrupoLectura> response = apiGrupoLectura.buscarGrupoPorNombre(query).execute();

                            // Volvemos al hilo principal para mostrar resultados o alertas
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

    @Override
    public void onResume() {
        super.onResume();
        cargarGrupos();
    }

    public void mostrarAlerta(String titulo, String contenido) {
        // Usamos requireContext() para asegurarnos de tener un contexto válido
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(titulo);
        builder.setMessage(contenido);

        builder.setPositiveButton("Entendido", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void filtrar(String texto) {
        if (listaGL == null) return;

        List<GrupoLectura> filtrada = new ArrayList<>();
        for (GrupoLectura grupo : listaGL) {
            // Comprobamos nombre o ubicación para que sea más útil
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
                // 2. Ejecutamos la llamada síncrona (.execute) ya que estamos en un hilo aparte
                retrofit2.Response<List<GrupoLectura>> response = apiGrupoLectura.obtenerGrupos().execute();

                if (response.isSuccessful() && response.body() != null) {
                    // Guardamos la lista en la variable de clase para que el buscador funcione
                    listaGL = response.body();

                    // 3. VOLVEMOS al hilo principal para tocar el RecyclerView
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (adapter != null) {
                                adapter.setGrupoLectura(listaGL);
                            }
                        });
                    }
                } else {
                    Log.e("ExplorarFragment", "Error API: " + response.code());
                }

            } catch (Exception e) {
                Log.e("ExplorarFragment", "Excepción en segundo plano: ", e);
            }
        }).start();
    }
}
