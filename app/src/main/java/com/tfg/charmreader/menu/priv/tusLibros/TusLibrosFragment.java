package com.tfg.charmreader.menu.priv.tusLibros;

import android.app.Activity;
import android.content.Intent;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tfg.charmreader.R;
import com.tfg.charmreader.Utilidades;
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
    private List<LibrosDeUsuario> listaLibrosUsuarioGlobal;

    public TusLibrosFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tus_libros, container, false);

        rvLibros = view.findViewById(R.id.recyclerLibros);
        rvLibros.setLayoutManager(new LinearLayoutManager(getContext()));

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

        // Configuración de la Barra de Búsqueda
        searchView = view.findViewById(R.id.searchViewLibros);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.filtrar(newText);
                }
                return true;
            }
        });

        FloatingActionButton fab = view.findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CargarNuevoLibro.class);
            startActivityForResult(intent, 123); // Refresca al volver
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
            if (rvLibros != null) {
                rvLibros.postDelayed(this::cargarLibros, 500);
            }
        }
    }

    private void cargarLibros() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) return;

        new Thread(() -> {
            try {
                int idUsuario = Utilidades.obtenerIdUsuarioDesdeAPI();
                if (idUsuario == -1) return;

                I_ApiLibrosDeUsuario apiLibrosUsuario = API.getInstancia().create(I_ApiLibrosDeUsuario.class);
                I_ApiLibro apiLibros = API.getInstancia().create(I_ApiLibro.class);

                listaLibrosUsuarioGlobal = apiLibrosUsuario.obtenerLibrosDeUsuario(idUsuario).execute().body();
                if (listaLibrosUsuarioGlobal == null || listaLibrosUsuarioGlobal.isEmpty()) return;

                List<Integer> idsLibros = new ArrayList<>();
                for (LibrosDeUsuario ldu : listaLibrosUsuarioGlobal) {
                    if (ldu.getId() != null && ldu.getId().getIdL() != null) {
                        idsLibros.add(ldu.getId().getIdL());
                    }
                }

                List<Libro> listaLibros = apiLibros.obtenerLibrosPorIds(idsLibros).execute().body();

                if (listaLibros != null && isAdded()) {
                    requireActivity().runOnUiThread(() -> adapter.setLibros(listaLibros));
                }

            } catch (Exception e) {
                Log.e("TusLibrosFragment", "Error cargando libros", e);
            }
        }).start();
    }
}