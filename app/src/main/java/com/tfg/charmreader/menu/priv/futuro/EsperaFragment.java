package com.tfg.charmreader.menu.priv.futuro;

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
import com.tfg.charmreader.interfacesAPI.I_ApiLibrosSinEstrenar;
import com.tfg.charmreader.menu.priv.adapterRecyclerView.LibrosSinEstrenarAdapter;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.LibrosSinEstrenar;

import java.util.ArrayList;

import retrofit2.Response;

public class EsperaFragment extends Fragment {

    private RecyclerView rvLibros;
    private LibrosSinEstrenarAdapter adapter;

    public EsperaFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_espera, container, false);

        rvLibros = view.findViewById(R.id.recyclerLibrosSinEstrenar);
        rvLibros.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new LibrosSinEstrenarAdapter(new ArrayList<>(), libro -> {
            Intent intent = new Intent(getActivity(), FuturoLibro.class);
            intent.putExtra("libro_seleccionado", libro);
            startActivity(intent);
        });

        rvLibros.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fab_add_LibrosSinEstrenar);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NuevoFuturoLibro.class);
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

    @Override
    public void onResume() {
        super.onResume();
        // Llama aquí al método que hace el enqueue de Retrofit para listar
        cargarLibros();
    }

    private void cargarLibros() {
        new Thread(() -> {
            try {
                int idUsuario = Utilidades.obtenerIdUsuarioDesdeAPI();

                if (idUsuario <= 0) {
                    Log.e("ERROR EsperaFragment", "Error cargarLibros, ID de usuario inválido: " + idUsuario);
                    return;
                }

                I_ApiLibrosSinEstrenar apiLibrosSinEstrenar = API.getInstancia().create(I_ApiLibrosSinEstrenar.class);
                Response<ArrayList<LibrosSinEstrenar>> response = apiLibrosSinEstrenar.getLibrosSinEstrenarPorUsuario(idUsuario).execute();

                if (response.isSuccessful() && response.body() != null) {
                    ArrayList<LibrosSinEstrenar> listaRecibida = response.body();

                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (adapter != null) {
                                adapter.setLibros(listaRecibida);
                            }
                        });
                    }
                } else {
                    Log.e("ERROR EsperaFragment", "Error en cargarLibros, problemas en el acceso a API: " + response.code());
                }

            } catch (Exception e) {
                Log.e("ERROR EsperaFragment", "Error en cargarLibros, excepción en segundo plano: ", e);
            }
        }).start();
    }
}