package com.tfg.charmreader.menu.publ.misGrupos.suscritos;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private List<BookEn> listaLibros = new ArrayList<>();
    private GrupoLectura grupo;

    // El ID del usuario deberías obtenerlo de tu sesión/SharedPreferences
    private int idUsuarioLogueado = 1;

    private I_APICatalogo apiCatalogo = API.getInstancia().create(I_APICatalogo.class);
    private I_ApiMiembro apiMiembro = API.getInstancia().create(I_ApiMiembro.class);
    private I_ApiVotacion apiVotacion = API.getInstancia().create(I_ApiVotacion.class);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_propuestas, container, false);

        recyclerView = view.findViewById(R.id.rvPropuestas);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getActivity() != null && getActivity().getIntent().hasExtra("objetoGrupo")) {
            grupo = (GrupoLectura) getActivity().getIntent().getSerializableExtra("objetoGrupo");
        }

        if (grupo != null) {
            cargarLibrosPropuestos(grupo.getIdGrupo());
        }

        return view;
    }

    private void cargarLibrosPropuestos(int idGrupo) {
        // 1. Obtener total de miembros
        apiMiembro.contarMiembros(idGrupo).enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> responseMiembros) {
                if (responseMiembros.isSuccessful() && responseMiembros.body() != null) {
                    int totalMiembros = responseMiembros.body().intValue();

                    // 2. Obtener libros propuestos
                    apiCatalogo.obtenerLibroPropuestas(idGrupo).enqueue(new Callback<List<BookEn>>() {
                        @Override
                        public void onResponse(Call<List<BookEn>> call, Response<List<BookEn>> responseLibros) {
                            if (responseLibros.isSuccessful() && responseLibros.body() != null) {
                                listaLibros = responseLibros.body();
                                configurarAdapter(totalMiembros);
                            }
                        }
                        @Override
                        public void onFailure(Call<List<BookEn>> call, Throwable t) { Log.e("API", t.getMessage()); }
                    });
                }
            }
            @Override
            public void onFailure(Call<Long> call, Throwable t) { Log.e("API", t.getMessage()); }
        });
    }

    private void configurarAdapter(int totalMiembros) {
        adapter = new LibroPropuestoAdapter(listaLibros, totalMiembros, new LibroPropuestoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BookEn libro) {
                // IR A DETALLE DEL LIBRO
                Intent i = new Intent(getContext(), LibroActual.class); // Cambia a tu clase de detalle
                i.putExtra("libroSeleccionado", libro);
                startActivity(i);
            }

            @Override
            public void onVotarClick(BookEn libro) {
                ejecutarVoto(libro.getId());
            }
        });

        // Pasamos la API de votación al adapter para que él mismo gestione el texto del botón
        adapter.setVotacionApi(apiVotacion, idUsuarioLogueado, grupo.getIdGrupo());
        recyclerView.setAdapter(adapter);
    }

    private void ejecutarVoto(int idLibro) {
        Votacion v = new Votacion(idUsuarioLogueado, grupo.getIdGrupo(), idLibro);

        apiVotacion.alternarVoto(v).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    // ESTO ES VITAL: Al recargar, el adapter vuelve a preguntar el estado
                    // y el botón cambiará de texto.
                    cargarLibrosPropuestos(grupo.getIdGrupo());
                    Toast.makeText(getContext(), "Acción realizada", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(getContext(), "Error al conectar", Toast.LENGTH_SHORT).show();
            }
        });
    }
}