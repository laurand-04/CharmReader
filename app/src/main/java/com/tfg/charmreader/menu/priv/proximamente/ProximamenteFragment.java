package com.tfg.charmreader.menu.priv.proximamente;

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
import com.tfg.charmreader.interfacesAPI.I_ApiBook;
import com.tfg.charmreader.menu.priv.adapterRecyclerView.BookIntAdapter;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.BookEn;

import java.util.ArrayList;
import java.util.List;

public class ProximamenteFragment extends Fragment {

    private RecyclerView rvLibros;
    private BookIntAdapter adapter;
    private TextView tvCount;
    private SearchView searchView;
    private LinearLayout layoutEmpty;

    public ProximamenteFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_proximamente, container, false);

        rvLibros = view.findViewById(R.id.recyclerProximamente);
        tvCount = view.findViewById(R.id.tvCountProximamente);
        searchView = view.findViewById(R.id.searchViewProximamente);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        rvLibros.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new BookIntAdapter(new ArrayList<>(), libro -> {
            Intent intent = new Intent(getActivity(), ProximoLibro.class);
            intent.putExtra("idLibro", libro.getId());
            startActivity(intent);
        });

        // 🔥 CONFIGURACIÓN CLIC LARGO PARA ELIMINAR
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

        FloatingActionButton fab = view.findViewById(R.id.fab_add_Proximamente);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), BuscadorAPIExterna.class);
            startActivityForResult(intent, 123);
        });

        return view;
    }

    private void mostrarDialogoEliminar(BookEn book) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar libro")
                .setMessage("¿Estás seguro de que deseas eliminar '" + book.getTitulo() + "' de tu lista de pendientes?")
                .setNegativeButton("CANCELAR", null)
                .setPositiveButton("ELIMINAR", (dialog, which) -> ejecutarEliminacion(book))
                .show();
    }

    private void ejecutarEliminacion(BookEn book) {
        new Thread(() -> {
            try {
                I_ApiBook apiBook = API.getInstancia().create(I_ApiBook.class);
                retrofit2.Response<Boolean> response = apiBook.eliminarBook(book.getId()).execute();

                if (response.isSuccessful() && Boolean.TRUE.equals(response.body())) {
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Libro eliminado", Toast.LENGTH_SHORT).show();
                            cargarLibros(); // Recargar la lista
                        });
                    }
                }
            } catch (Exception e) {
                Log.e("DELETE_BOOK", "Error al eliminar libro", e);
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

    private void cargarLibros() {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);

        if (idUsuario == -1) return;

        new Thread(() -> {
            try {
                I_ApiBook apiBook = API.getInstancia().create(I_ApiBook.class);
                retrofit2.Response<List<BookEn>> response = apiBook.obtenerBooksPorUsuario(idUsuario).execute();

                if (response.isSuccessful() && response.body() != null) {
                    List<BookEn> listaRecibida = response.body();

                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (listaRecibida.isEmpty()) {
                                layoutEmpty.setVisibility(View.VISIBLE);
                                rvLibros.setVisibility(View.GONE);
                                tvCount.setText("Sin lecturas planeadas");
                            } else {
                                layoutEmpty.setVisibility(View.GONE);
                                rvLibros.setVisibility(View.VISIBLE);
                                adapter.setBooks(listaRecibida);
                                int total = listaRecibida.size();
                                tvCount.setText((total == 1) ? "1 LIBRO PENDIENTE" : total + " LIBROS PENDIENTES");
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Log.e("DEBUG_APP", "Error al cargar libros", e);
            }
        }).start();
    }
}