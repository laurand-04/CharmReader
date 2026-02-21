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
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_espera, container, false);

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

        // Configurar el borrado por clic largo
        adapter.setOnItemLongClickListener(this::mostrarDialogoEliminar);

        rvLibros.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) adapter.filtrar(newText);
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

    private void mostrarDialogoEliminar(LibrosSinEstrenar libro) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar lanzamiento")
                .setMessage("¿Deseas quitar '" + libro.getId().getNombre() + "' de la lista?")
                .setNegativeButton("CANCELAR", null)
                .setPositiveButton("ELIMINAR", (dialog, which) -> ejecutarEliminacion(libro))
                .show();
    }

    private void ejecutarEliminacion(LibrosSinEstrenar libro) {
        new Thread(() -> {
            try {
                I_ApiLibrosSinEstrenar api = API.getInstancia().create(I_ApiLibrosSinEstrenar.class);

                // 🔥 Ajustado para recibir String según tu interfaz I_ApiLibrosSinEstrenar
                Response<String> response = api.eliminarLibrosSinEstrenar(libro.getId()).execute();

                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Comprobamos si la respuesta fue exitosa y si el backend devolvió "Bien"
                        if (response.isSuccessful() && response.body() != null && response.body().equalsIgnoreCase("Bien")) {
                            Toast.makeText(getContext(), "Libro eliminado", Toast.LENGTH_SHORT).show();
                            cargarLibros();
                        } else {
                            Toast.makeText(getContext(), "No se pudo eliminar el libro", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("EsperaFragment", "Error en eliminación: " + e.getMessage());
                if (isAdded()) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
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
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);

        if (idUsuario <= 0) return;

        new Thread(() -> {
            try {
                I_ApiLibrosSinEstrenar api = API.getInstancia().create(I_ApiLibrosSinEstrenar.class);
                Response<ArrayList<LibrosSinEstrenar>> response = api.getLibrosSinEstrenarPorUsuario(idUsuario).execute();

                if (response.isSuccessful() && response.body() != null) {
                    ArrayList<LibrosSinEstrenar> listaRecibida = response.body();
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> actualizarInterfaz(listaRecibida));
                    }
                }
            } catch (Exception e) {
                Log.e("EsperaFragment", "Error en cargarLibros", e);
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
            tvCount.setText((total == 1) ? "1 LANZAMIENTO PENDIENTE" : total + " LANZAMIENTOS PENDIENTES");
        }
    }
}