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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tfg.charmreader.Utilidades;
import com.tfg.charmreader.interfacesAPI.I_ApiEstanteria;
import com.tfg.charmreader.menu.priv.adapterRecyclerView.EstanteriasAdapter;
import com.tfg.charmreader.R;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.Estanteria;

import java.util.ArrayList;
import java.util.List;

public class EstanteriaFragment extends Fragment {

    private RecyclerView rvEstanterias;
    private EstanteriasAdapter adapter;

    public EstanteriaFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_estanteria, container, false);

        rvEstanterias = view.findViewById(R.id.recyclerEstanterias);
        rvEstanterias.setLayoutManager(new LinearLayoutManager(getContext()));

        // Configuración del click corregida para usar el objeto directo
        // En EstanteriaFragment.java
        adapter = new EstanteriasAdapter(new ArrayList<>(), estanteria -> {
            Log.d("DEBUG_APP", "CLICK DETECTADO en: " + estanteria.getNombre()); // Si esto no sale, el problema es el Adapter
            Intent intent = new Intent(getActivity(), LibrosEstanteria.class);
            intent.putExtra("Nombre", estanteria.getNombre());
            intent.putExtra("idEstanteria", estanteria.getId());
            startActivity(intent);
        });

        rvEstanterias.setAdapter(adapter);

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
        Log.d("EstanteriaFragment", "PASO 1: Entrando en cargarEstanterias()");

        new Thread(() -> {
            int idUsuario = Utilidades.obtenerIdUsuarioDesdeAPI();
            if (idUsuario == -1) {
                Log.e("EstanteriaFragment", "El id es -1 en cargarEstanterias");
                return;
            }
            try {
                Log.d("EstanteriaFragment", "PASO 3: El ID de usuario en Utilidades es: " + idUsuario);

                if (idUsuario <= 0) {
                    Log.e("EstanteriaFragment", "PASO 4: ERROR - El ID es inválido. Por eso sale blanco.");
                    return;
                }

                I_ApiEstanteria apiEstanteria = API.getInstancia().create(I_ApiEstanteria.class);
                retrofit2.Response<List<Estanteria>> response = apiEstanteria.obtenerEstanteriasDeUsuario(idUsuario).execute();

                if (response.isSuccessful() && response.body() != null) {
                    List<Estanteria> listaRecibida = response.body();
                    Log.d("EstanteriaFragment", "PASO 5: ¡Éxito! Registros recibidos: " + listaRecibida.size());

                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (isAdded() && adapter != null) { // <--- Seguridad extra
                                adapter.setEstanterias(listaRecibida);
                            }
                            if (listaRecibida.isEmpty()) {
                                Log.w("EstanteriaFragment", "PASO 6: La lista está vacía en la base de datos.");
                            }
                            adapter.setEstanterias(listaRecibida);
                        });
                    }
                } else {
                    Log.e("EstanteriaFragment", "PASO 5: Error en la API. Código: " + response.code());
                }

            } catch (Exception e) {
                Log.e("EstanteriaFragment", "PASO EXTRA: Hubo una excepción grave: ", e);
            }
        }).start();
    }
}