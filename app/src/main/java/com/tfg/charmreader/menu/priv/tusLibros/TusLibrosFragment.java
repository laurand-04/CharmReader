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
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import okhttp3.ResponseBody;

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

        rvLibros = view.findViewById(R.id.recyclerLibros);
        layoutEmpty = view.findViewById(R.id.layoutEmptyTusLibros);
        searchView = view.findViewById(R.id.searchViewLibros);
        FloatingActionButton fab = view.findViewById(R.id.fab_add);

        rvLibros.setLayoutManager(new LinearLayoutManager(getContext()));

        // Configuración del Adapter
        adapter = new LibrosAdapter(new ArrayList<>(), libro -> {
            if (listaLibrosUsuarioGlobal != null) {
                for (LibrosDeUsuario ldu : listaLibrosUsuarioGlobal) {
                    if (ldu.getId().getIdL() == libro.getId()) {
                        if (ldu.getFechaFin() != null) {
                            mostrarDialogoRelectura(ldu);
                        } else {
                            abrirVisor(ldu, false);
                        }
                        return;
                    }
                }
            }
        });
        adapter.setSoloPendientes(true);

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

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CargarNuevoLibro.class);
            startActivityForResult(intent, 123);
        });

        return view;
    }

    private void mostrarDialogoEliminar(Libro libro) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar libro")
                .setMessage("¿Deseas eliminar '" + libro.getNombre() + "' de tu biblioteca? Se borrará también de tus estanterías.")
                .setNegativeButton("CANCELAR", null)
                .setPositiveButton("ELIMINAR", (dialog, which) -> ejecutarEliminacion(libro))
                .show();
    }

    private void ejecutarEliminacion(Libro libro) {
        SharedPreferences prefs = requireContext().getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        int idU = prefs.getInt("idUsuario", -1);
        int idL = libro.getId();

        new Thread(() -> {
            try {
                I_ApiLibrosDeUsuario api = API.getInstancia().create(I_ApiLibrosDeUsuario.class);
                // Llamada al endpoint DELETE /eliminar/{idU}/{idL}
                retrofit2.Response<ResponseBody> response = api.eliminarLibro(idU, idL).execute();

                if (response.isSuccessful()) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Libro eliminado correctamente", Toast.LENGTH_SHORT).show();
                            cargarLibros(); // Refresca la lista
                        });
                    }
                }
            } catch (Exception e) {
                Log.e("ELIMINAR", "Error: " + e.getMessage());
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
            if (rvLibros != null) rvLibros.postDelayed(this::cargarLibros, 500);
        }
    }

    private void cargarLibros() {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);

        if (idUsuario == -1) {
            mostrarEstadoVacio(true);
            return;
        }

        new Thread(() -> {
            try {
                I_ApiLibrosDeUsuario apiLibrosUsuario = API.getInstancia().create(I_ApiLibrosDeUsuario.class);
                I_ApiLibro apiLibros = API.getInstancia().create(I_ApiLibro.class);

                retrofit2.Response<List<LibrosDeUsuario>> responseRelacion = apiLibrosUsuario.obtenerLibrosDeUsuario(idUsuario).execute();
                listaLibrosUsuarioGlobal = responseRelacion.body();

                if (listaLibrosUsuarioGlobal == null || listaLibrosUsuarioGlobal.isEmpty()) {
                    mostrarEstadoVacio(true);
                    return;
                }

                List<Integer> idsLibros = new ArrayList<>();
                for (LibrosDeUsuario ldu : listaLibrosUsuarioGlobal) {
                    if (ldu.getId() != null) idsLibros.add(ldu.getId().getIdL());
                }

                retrofit2.Response<List<Libro>> responseLibros = apiLibros.obtenerLibrosPorIds(idsLibros).execute();
                List<Libro> listaLibros = responseLibros.body();

                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        if (listaLibros == null || listaLibros.isEmpty()) {
                            mostrarEstadoVacio(true);
                        } else {
                            mostrarEstadoVacio(false);
                            adapter.setData(listaLibros, listaLibrosUsuarioGlobal);
                        }
                    });
                }
            } catch (Exception e) {
                mostrarEstadoVacio(true);
            }
        }).start();
    }

    private void mostrarEstadoVacio(boolean estaVacio) {
        if (isAdded() && getActivity() != null) {
            requireActivity().runOnUiThread(() -> {
                layoutEmpty.setVisibility(estaVacio ? View.VISIBLE : View.GONE);
                rvLibros.setVisibility(estaVacio ? View.GONE : View.VISIBLE);
            });
        }
    }

    private void mostrarDialogoRelectura(LibrosDeUsuario ldu) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("¡Libro ya leído!")
                .setMessage("¿Qué deseas hacer con este libro?")
                .setNeutralButton("CANCELAR", null)
                .setNegativeButton("REINICIAR", (dialog, which) -> reiniciarYAbrir(ldu))
                .setPositiveButton("CONTINUAR", (dialog, which) -> abrirVisor(ldu, false))
                .show();
    }

    private void reiniciarYAbrir(LibrosDeUsuario ldu) {
        ldu.setCapitulo(0);
        ldu.setScroll(0f);
        ldu.setFechaInicio(new java.util.Date());
        ldu.setFechaFin(null);

        new Thread(() -> {
            try {
                Utilidades.apiLibrosDeUsuario.guardarProgreso(ldu).execute();
                if (isAdded()) requireActivity().runOnUiThread(() -> abrirVisor(ldu, true));
            } catch (Exception e) {
                Log.e("REINICIAR", "Error: " + e.getMessage());
            }
        }).start();
    }

    private void abrirVisor(LibrosDeUsuario ldu, boolean esReinicio) {
        Intent intent = new Intent(getActivity(), Visor_n.class);
        intent.putExtra("OBJETO_LIBRO_USUARIO", ldu);
        intent.putExtra("URL_LIBRO", ldu.getRuta());
        intent.putExtra("idL", ldu.getId().getIdL());
        if (esReinicio) intent.putExtra("REINICIAR_LECTURA", true);
        startActivity(intent);
    }
}