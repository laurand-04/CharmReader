package com.tfg.charmreader.menu.priv.futuro;

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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_ApiLibrosSinEstrenar;
import com.tfg.charmreader.menu.priv.adapterRecyclerView.LibrosSinEstrenarAdapter;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.LibrosSinEstrenar;

import java.util.ArrayList;

import retrofit2.Response;

public class EsperaFragment extends Fragment {

    private RecyclerView rvLibros;
    private LibrosSinEstrenarAdapter adapter;
    private TextView tvCount;
    private LinearLayout layoutEmpty;
    private SearchView searchView;

    public EsperaFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_espera, container, false);

        // Inicialización de vistas
        rvLibros = view.findViewById(R.id.recyclerLibrosSinEstrenar);
        tvCount = view.findViewById(R.id.tvCountEspera);
        layoutEmpty = view.findViewById(R.id.layoutEmptyEspera);
        searchView = view.findViewById(R.id.searchViewEspera);

        rvLibros.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new LibrosSinEstrenarAdapter(new ArrayList<>(), libro -> {
            Intent intent = new Intent(getActivity(), FuturoLibro.class);
            intent.putExtra("libro_seleccionado", libro);
            startActivity(intent);
        });

        rvLibros.setAdapter(adapter);

        // Configuración del buscador
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
        cargarLibros();
    }

    private void cargarLibros() {
        // 1. Obtenemos el ID guardado localmente (sin red, sin errores)
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);

        if (idUsuario <= 0) {
            Log.e("EsperaFragment", "ID de usuario no válido");
            return;
        }

        // 2. Solo usamos el hilo para la petición pesada
        new Thread(() -> {
            try {
                I_ApiLibrosSinEstrenar api = API.getInstancia().create(I_ApiLibrosSinEstrenar.class);
                Response<ArrayList<LibrosSinEstrenar>> response = api.getLibrosSinEstrenarPorUsuario(idUsuario).execute();

                if (response.isSuccessful() && response.body() != null) {
                    ArrayList<LibrosSinEstrenar> listaRecibida = response.body();

                    // 3. Volvemos al hilo de UI de forma segura
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            actualizarInterfaz(listaRecibida);
                        });
                    }
                }
            } catch (Exception e) {
                Log.e("ERROR EsperaFragment", "Error en cargarLibros", e);
            }
        }).start();
    }

    private void actualizarInterfaz(ArrayList<LibrosSinEstrenar> lista) {
        if (lista.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvLibros.setVisibility(View.GONE);
            tvCount.setText("Sin lanzamientos pendientes");
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvLibros.setVisibility(View.VISIBLE);
            adapter.setLibros(lista);

            int total = lista.size();
            String msg = (total == 1) ? "1 LANZAMIENTO PENDIENTE" : total + " LANZAMIENTOS PENDIENTES";
            tvCount.setText(msg);
        }
    }
}