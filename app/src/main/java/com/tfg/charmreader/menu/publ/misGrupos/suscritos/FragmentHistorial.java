package com.tfg.charmreader.menu.publ.misGrupos.suscritos;

import android.content.Intent;
import android.os.Bundle;
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
import com.tfg.charmreader.interfacesAPI.I_ApiBook;
import com.tfg.charmreader.interfacesAPI.I_ApiSesion;
import com.tfg.charmreader.interfacesAPI.I_ApiValoracion;
import com.tfg.charmreader.menu.publ.adapterReclyclerView.LibroHistorialAdapter;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.CatalogoLectura;
import com.tfg.charmreader.objetosBD.GrupoLectura;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentHistorial extends Fragment {

    private RecyclerView recyclerView;
    private GrupoLectura grupo;

    private I_APICatalogo apiCatalogo = API.getInstancia().create(I_APICatalogo.class);
    private I_ApiBook apiBook = API.getInstancia().create(I_ApiBook.class);
    private I_ApiSesion apiSesion = API.getInstancia().create(I_ApiSesion.class);
    private I_ApiValoracion apiValoracion = API.getInstancia().create(I_ApiValoracion.class);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historial, container, false);

        recyclerView = view.findViewById(R.id.rvHistorial);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getActivity() != null && getActivity().getIntent().hasExtra("objetoGrupo")) {
            grupo = (GrupoLectura) getActivity().getIntent().getSerializableExtra("objetoGrupo");
            cargarHistorial();
        }

        return view;
    }

    private void cargarHistorial() {
        apiCatalogo.obtenerHistorial2(grupo.getIdGrupo()).enqueue(new Callback<List<CatalogoLectura>>() {
            @Override
            public void onResponse(Call<List<CatalogoLectura>> call, Response<List<CatalogoLectura>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LibroHistorialAdapter adapter = new LibroHistorialAdapter(
                            response.body(),
                            grupo.getIdGrupo(),
                            apiBook,
                            apiSesion,
                            apiValoracion,
                            registro -> {
                                // Lógica de navegación al seleccionar un libro
                                Intent intent = new Intent(getContext(), ValoracionesLibro.class);
                                intent.putExtra("idLibro", registro.getIdBook());
                                intent.putExtra("idGrupo", grupo.getIdGrupo());
                                startActivity(intent);
                            }
                    );
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(getContext(), "No hay lecturas en el historial", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CatalogoLectura>> call, Throwable t) {
                Toast.makeText(getContext(), "Error al cargar el historial", Toast.LENGTH_SHORT).show();
            }
        });
    }
}