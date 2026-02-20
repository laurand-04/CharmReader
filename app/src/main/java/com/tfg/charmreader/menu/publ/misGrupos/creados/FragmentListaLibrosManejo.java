package com.tfg.charmreader.menu.publ.misGrupos.creados;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_APICatalogo;
import com.tfg.charmreader.interfacesAPI.I_ApiBook;
import com.tfg.charmreader.menu.priv.proximamente.ProximoLibro;
import com.tfg.charmreader.menu.publ.adapterReclyclerView.LibroPropuestoAdministradorAdapter;
import com.tfg.charmreader.objetosBD.*;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentListaLibrosManejo extends Fragment {

    private int idGrupo, tipoLista;
    private RecyclerView rvLibros;
    private final List<BookEn> listaLibros = new ArrayList<>();

    // Vistas para el Empty State
    private LinearLayout layoutEmpty;
    private ImageView ivEmptyIcon;
    private TextView tvEmptyTitle, tvEmptySubtitle;

    private boolean cargando = false;

    private final I_APICatalogo apiCatalogo = API.getInstancia().create(I_APICatalogo.class);
    private final I_ApiBook apiBook = API.getInstancia().create(I_ApiBook.class);

    public static FragmentListaLibrosManejo newInstance(int idGrupo, int tipoLista) {
        FragmentListaLibrosManejo fragment = new FragmentListaLibrosManejo();
        Bundle args = new Bundle();
        args.putInt("idGrupo", idGrupo);
        args.putInt("tipoLista", tipoLista);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lista_libros_manejo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        idGrupo = getArguments().getInt("idGrupo");
        tipoLista = getArguments().getInt("tipoLista");

        // 🔥 CORRECCIÓN DE ID: Debe ser rvListaManejo como en tu XML
        rvLibros = view.findViewById(R.id.rvListaManejo);

        // Vincular vistas del Empty State
        layoutEmpty = view.findViewById(R.id.layoutEmptyManejo);
        ivEmptyIcon = view.findViewById(R.id.ivEmptyIconManejo);
        tvEmptyTitle = view.findViewById(R.id.tvEmptyTitleManejo);
        tvEmptySubtitle = view.findViewById(R.id.tvEmptySubtitleManejo);

        rvLibros.setLayoutManager(new LinearLayoutManager(getContext()));

        LibroPropuestoAdministradorAdapter adapter = new LibroPropuestoAdministradorAdapter(
                listaLibros,
                idGrupo,
                libro -> {
                    if (tipoLista == 0) {
                        Intent intent = new Intent(getActivity(), ProximoLibro.class);
                        intent.putExtra("idLibro", libro.getId());
                        getActivity().startActivityForResult(intent, 123);
                    }
                }
        );

        rvLibros.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!cargando) {
            cargarDatos();
        }
    }

    private void cargarDatos() {
        cargando = true;
        apiCatalogo.verCatalogo(idGrupo).enqueue(new Callback<List<CatalogoLectura>>() {
            @Override
            public void onResponse(Call<List<CatalogoLectura>> call, Response<List<CatalogoLectura>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    filtrarYCargarLibros(response.body());
                } else {
                    cargando = false;
                    mostrarEstadoVacio(true);
                }
            }
            @Override
            public void onFailure(Call<List<CatalogoLectura>> call, Throwable t) {
                cargando = false;
                mostrarEstadoVacio(true);
            }
        });
    }

    private void filtrarYCargarLibros(List<CatalogoLectura> catalogo) {
        listaLibros.clear();

        List<Integer> idsAFiltrar = new ArrayList<>();
        for (CatalogoLectura item : catalogo) {
            boolean cumpleFiltro = false;
            // Usamos item.getEstado() comparado con tu Enum o enteros según tu lógica de BD
            if (tipoLista == 0 && item.getEstado() == CatalogoLectura.EstadoLectura.PROPUESTO) cumpleFiltro = true;
            else if (tipoLista == 1 && item.getEstado() == CatalogoLectura.EstadoLectura.ACTUAL) cumpleFiltro = true;
            else if (tipoLista == 2 && item.getEstado() == CatalogoLectura.EstadoLectura.FINALIZADO) cumpleFiltro = true;

            if (cumpleFiltro) {
                idsAFiltrar.add(item.getIdBook());
            }
        }

        if (idsAFiltrar.isEmpty()) {
            cargando = false;
            mostrarEstadoVacio(true);
            return;
        }

        final int[] completados = {0};
        for (Integer idBook : idsAFiltrar) {
            apiBook.obtenerBookPorId(idBook).enqueue(new Callback<BookEn>() {
                @Override
                public void onResponse(Call<BookEn> call, Response<BookEn> r) {
                    completados[0]++;
                    if (r.isSuccessful() && r.body() != null) {
                        listaLibros.add(r.body());
                    }

                    if (completados[0] == idsAFiltrar.size()) {
                        cargando = false;
                        finalizarCarga();
                    }
                }

                @Override
                public void onFailure(Call<BookEn> call, Throwable t) {
                    completados[0]++;
                    if (completados[0] == idsAFiltrar.size()) {
                        cargando = false;
                        finalizarCarga();
                    }
                }
            });
        }
    }

    private void finalizarCarga() {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                if (listaLibros.isEmpty()) {
                    mostrarEstadoVacio(true);
                } else {
                    mostrarEstadoVacio(false);
                    if (rvLibros.getAdapter() != null) {
                        rvLibros.getAdapter().notifyDataSetChanged();
                    }
                }
            });
        }
    }

    private void mostrarEstadoVacio(boolean vacio) {
        if (!isAdded()) return;

        if (vacio) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvLibros.setVisibility(View.GONE);

            switch (tipoLista) {
                case 0: // Propuestas
                    ivEmptyIcon.setImageResource(R.drawable.ic_libro);
                    tvEmptyTitle.setText("¡Lluvia de ideas!");
                    tvEmptySubtitle.setText("No has propuesto libros todavía. Pulsa el botón + para animar el grupo.");
                    break;
                case 1: // Actual
                    ivEmptyIcon.setImageResource(R.drawable.ic_people);
                    tvEmptyTitle.setText("Tiempo de descanso");
                    tvEmptySubtitle.setText("El grupo no está leyendo nada ahora mismo. ¿Es hora de votar una propuesta?");
                    break;
                case 2: // Finalizadas
                    ivEmptyIcon.setImageResource(R.drawable.ic_gavel);
                    tvEmptyTitle.setText("Camino por recorrer");
                    tvEmptySubtitle.setText("Aún no habéis terminado vuestra primera lectura oficial. ¡El historial os espera!");
                    break;
            }
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvLibros.setVisibility(View.VISIBLE);
        }
    }
}