package com.tfg.charmreader.menu.priv.proximamente;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tfg.charmreader.R;
import com.tfg.charmreader.Utilidades;
import com.tfg.charmreader.interfacesAPI.I_ApiBook;
import com.tfg.charmreader.menu.priv.adapterRecyclerView.BookIntAdapter;
import com.tfg.charmreader.menu.publ.misGrupos.creados.NuevoLibroPropuesto;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.BookEn;

import java.util.ArrayList;
import java.util.List;

public class ProximamenteFragment extends Fragment {

    private RecyclerView rvLibros;
    private BookIntAdapter adapter;

    public ProximamenteFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_proximamente, container, false);

        rvLibros = view.findViewById(R.id.recyclerProximamente);
        rvLibros.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new BookIntAdapter(new ArrayList<>(), libro -> {
            Intent intent = new Intent(getActivity(), ProximoLibro.class); //AQUI HAY QUE VER SI QUEREMOS AÑADIR ALGUNA FUNCIONALIDAD AL CLICK EN LIBRO
            //intent.putExtra("Nombre", estanteria.getNombre());
            intent.putExtra("idLibro", libro.getId());
            startActivity(intent);
        });

        rvLibros.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fab_add_Proximamente);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), BuscadorAPIExterna.class);
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
            cargarLibros();
        }
    }

    private void cargarLibros() {
        new Thread(() -> {
            try {
                int idUsuario = Utilidades.obtenerIdUsuarioDesdeAPI();
                Log.d("DEBUG_APP", "ID Usuario obtenido: " + idUsuario);

                I_ApiBook apiBook = API.getInstancia().create(I_ApiBook.class);
                retrofit2.Response<List<BookEn>> response = apiBook.obtenerBooksPorUsuario(idUsuario).execute();

                if (response.isSuccessful() && response.body() != null) {
                    List<BookEn> listaRecibida = response.body();
                    Log.d("DEBUG_APP", "Libros recibidos: " + listaRecibida.size());

                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            adapter.setBooks(listaRecibida);
                            adapter.notifyDataSetChanged(); // Asegúrate de notificar cambios
                        });
                    }
                } else {
                    Log.e("DEBUG_APP", "Error en respuesta: " + response.message());
                }
            } catch (Exception e) {
                Log.e("DEBUG_APP", "Error crítico", e);
            }
        }).start();
    }
}