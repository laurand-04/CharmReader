package com.tfg.charmreader.menu.priv.tusLibros;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_ApiLibro;
import com.tfg.charmreader.interfacesAPI.I_ApiLibrosDeUsuario;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.Libro;
import com.tfg.charmreader.objetosBD.LibrosDeUsuario;
import com.tfg.charmreader.menu.priv.adapterRecyclerView.LibrosAdapter;

import java.util.ArrayList;
import java.util.List;

public class TusLibrosFragment extends Fragment {

    private RecyclerView rvLibros;
    private LibrosAdapter adapter;
    private SearchView searchView;
    private LinearLayout layoutEmpty;
    private List<LibrosDeUsuario> listaLibrosUsuarioGlobal;

    public TusLibrosFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tus_libros, container, false);

        // 1. Inicialización de vistas
        rvLibros = view.findViewById(R.id.recyclerLibros);
        layoutEmpty = view.findViewById(R.id.layoutEmptyTusLibros);
        searchView = view.findViewById(R.id.searchViewLibros);
        FloatingActionButton fab = view.findViewById(R.id.fab_add);

        rvLibros.setLayoutManager(new LinearLayoutManager(getContext()));

        // 2. Configuración del Adapter
        adapter = new LibrosAdapter(new ArrayList<>(), libro -> {
            if (listaLibrosUsuarioGlobal != null) {
                for (LibrosDeUsuario ldu : listaLibrosUsuarioGlobal) {
                    if (ldu.getId().getIdL() == libro.getId()) {
                        Intent intent = new Intent(getActivity(), Visor_n.class);
                        intent.putExtra("URL_LIBRO", ldu.getRuta());
                        intent.putExtra("idL", ldu.getId().getIdL());
                        startActivity(intent);
                        return;
                    }
                }
            }
        });
        rvLibros.setAdapter(adapter);

        // 3. Buscador funcional
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.filtrar(newText);
                }
                return true;
            }
        });

        // 4. Acción del FAB
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CargarNuevoLibro.class);
            startActivityForResult(intent, 123);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cargarLibros();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            // Un pequeño delay para dar tiempo al servidor a procesar el nuevo libro
            if (rvLibros != null) {
                rvLibros.postDelayed(this::cargarLibros, 500);
            }
        }
    }

    private void cargarLibros() {
        // 🔥 CAMBIO CLAVE: Obtenemos el ID desde SharedPreferences (instantáneo)
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);

        if (idUsuario == -1) {
            Log.e("TusLibrosFragment", "ID de usuario no encontrado en SharedPreferences");
            mostrarEstadoVacio(true);
            return;
        }

        new Thread(() -> {
            try {
                I_ApiLibrosDeUsuario apiLibrosUsuario = API.getInstancia().create(I_ApiLibrosDeUsuario.class);
                I_ApiLibro apiLibros = API.getInstancia().create(I_ApiLibro.class);

                // Obtener la relación libros-usuario
                retrofit2.Response<List<LibrosDeUsuario>> responseRelacion = apiLibrosUsuario.obtenerLibrosDeUsuario(idUsuario).execute();
                listaLibrosUsuarioGlobal = responseRelacion.body();

                if (listaLibrosUsuarioGlobal == null || listaLibrosUsuarioGlobal.isEmpty()) {
                    mostrarEstadoVacio(true);
                    return;
                }

                // Extraer los IDs para pedir los metadatos (portada, título, etc)
                List<Integer> idsLibros = new ArrayList<>();
                for (LibrosDeUsuario ldu : listaLibrosUsuarioGlobal) {
                    if (ldu.getId() != null) {
                        idsLibros.add(ldu.getId().getIdL());
                    }
                }

                // Pedir los objetos Libro completos
                retrofit2.Response<List<Libro>> responseLibros = apiLibros.obtenerLibrosPorIds(idsLibros).execute();
                List<Libro> listaLibros = responseLibros.body();

                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        if (listaLibros == null || listaLibros.isEmpty()) {
                            mostrarEstadoVacio(true);
                        } else {
                            mostrarEstadoVacio(false);
                            adapter.setLibros(listaLibros);
                        }
                    });
                }

            } catch (Exception e) {
                Log.e("TusLibrosFragment", "Error cargando libros", e);
                mostrarEstadoVacio(true);
            }
        }).start();
    }

    private void mostrarEstadoVacio(boolean estaVacio) {
        if (isAdded() && getActivity() != null) {
            requireActivity().runOnUiThread(() -> {
                if (estaVacio) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    rvLibros.setVisibility(View.GONE);
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                    rvLibros.setVisibility(View.VISIBLE);
                }
            });
        }
    }
}