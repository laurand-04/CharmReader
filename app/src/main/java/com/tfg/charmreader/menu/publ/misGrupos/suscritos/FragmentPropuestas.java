package com.tfg.charmreader.menu.publ.misGrupos.suscritos;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_APICatalogo;
import com.tfg.charmreader.interfacesAPI.I_ApiMiembro;
import com.tfg.charmreader.interfacesAPI.I_ApiVotacion;
import com.tfg.charmreader.menu.publ.adapterReclyclerView.LibroPropuestoAdapter;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.BookEn;
import com.tfg.charmreader.objetosBD.GrupoLectura;
import com.tfg.charmreader.objetosBD.Votacion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentPropuestas extends Fragment {

    private RecyclerView recyclerView;
    private LibroPropuestoAdapter adapter;
    private LinearLayout layoutEmpty;
    private List<BookEn> listaLibros = new ArrayList<>();
    private GrupoLectura grupo;

    private int idUsuarioLogueado;

    private final I_APICatalogo apiCatalogo = API.getInstancia().create(I_APICatalogo.class);
    private final I_ApiMiembro apiMiembro = API.getInstancia().create(I_ApiMiembro.class);
    private final I_ApiVotacion apiVotacion = API.getInstancia().create(I_ApiVotacion.class);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_propuestas, container, false);

        // 1. Obtener ID de usuario localmente (Instantáneo)
        if (getContext() != null) {
            SharedPreferences prefs = getContext().getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
            idUsuarioLogueado = prefs.getInt("idUsuario", -1);
        }

        recyclerView = view.findViewById(R.id.rvPropuestas);
        layoutEmpty = view.findViewById(R.id.layoutEmptyPropuestas);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getActivity() != null && getActivity().getIntent().hasExtra("objetoGrupo")) {
            grupo = (GrupoLectura) getActivity().getIntent().getSerializableExtra("objetoGrupo");
        }

        // 2. Si tenemos ID y Grupo, cargamos directamente
        if (idUsuarioLogueado != -1 && grupo != null) {
            cargarLibrosPropuestos(grupo.getIdGrupo());
        } else {
            mostrarEstadoVacio(true);
        }

        return view;
    }

    private void cargarLibrosPropuestos(int idGrupo) {
        // Obtener total de miembros para calcular porcentajes de votos
        apiMiembro.contarMiembros(idGrupo).enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> responseMiembros) {
                if (responseMiembros.isSuccessful() && responseMiembros.body() != null) {
                    int totalMiembros = responseMiembros.body().intValue();

                    // Obtener libros en estado 'PROPUESTO'
                    apiCatalogo.obtenerLibroPropuestas(idGrupo).enqueue(new Callback<List<BookEn>>() {
                        @Override
                        public void onResponse(Call<List<BookEn>> call, Response<List<BookEn>> responseLibros) {
                            if (isAdded() && responseLibros.isSuccessful() && responseLibros.body() != null) {
                                listaLibros = responseLibros.body();

                                if (listaLibros.isEmpty()) {
                                    mostrarEstadoVacio(true);
                                } else {
                                    mostrarEstadoVacio(false);
                                    configurarAdapter(totalMiembros);
                                }
                            } else {
                                mostrarEstadoVacio(true);
                            }
                        }
                        @Override public void onFailure(Call<List<BookEn>> call, Throwable t) { mostrarEstadoVacio(true); }
                    });
                }
            }
            @Override public void onFailure(Call<Long> call, Throwable t) { mostrarEstadoVacio(true); }
        });
    }

    private void configurarAdapter(int totalMiembros) {
        adapter = new LibroPropuestoAdapter(listaLibros, totalMiembros, new LibroPropuestoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BookEn libro) {
                Intent i = new Intent(getContext(), LibroActual.class);
                i.putExtra("libroSeleccionado", libro);
                startActivity(i);
            }

            @Override
            public void onVotarClick(BookEn libro) {
                ejecutarVoto(libro.getId());
            }
        });

        adapter.setVotacionApi(apiVotacion, idUsuarioLogueado, grupo.getIdGrupo());
        recyclerView.setAdapter(adapter);
    }

    private void ejecutarVoto(int idLibro) {
        Votacion v = new Votacion(idUsuarioLogueado, grupo.getIdGrupo(), idLibro);

        apiVotacion.alternarVoto(v).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    // Refrescamos la lista para ver el cambio en el contador de votos
                    cargarLibrosPropuestos(grupo.getIdGrupo());
                    Toast.makeText(getContext(), "Voto actualizado", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(getContext(), "Error al votar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarEstadoVacio(boolean estaVacio) {
        if (isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                layoutEmpty.setVisibility(estaVacio ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(estaVacio ? View.GONE : View.VISIBLE);
            });
        }
    }
}