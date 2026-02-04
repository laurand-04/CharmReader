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
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.BookEn;
import com.tfg.charmreader.objetosBD.CatalogoLectura;
import com.tfg.charmreader.objetosBD.GrupoLectura;
import com.tfg.charmreader.menu.publ.adapterReclyclerView.LibroHistorialAdapter;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentHistorial extends Fragment {

    private RecyclerView recyclerView;
    private GrupoLectura grupo;
    private I_APICatalogo apiCatalogo = API.getInstancia().create(I_APICatalogo.class);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historial, container, false);
        recyclerView = view.findViewById(R.id.rvHistorial);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getActivity() != null && getActivity().getIntent().hasExtra("objetoGrupo")) {
            grupo = (GrupoLectura) getActivity().getIntent().getSerializableExtra("objetoGrupo");
            cargarHistorialCombinado();
        }
        return view;
    }

    private void cargarHistorialCombinado() {
        // PASO 1: Obtener la lista de CatalogoLectura (contiene las fechas)
        apiCatalogo.obtenerHistorial2(grupo.getIdGrupo()).enqueue(new Callback<List<CatalogoLectura>>() {
            @Override
            public void onResponse(Call<List<CatalogoLectura>> call, Response<List<CatalogoLectura>> responseCata) {
                if (responseCata.isSuccessful() && responseCata.body() != null) {
                    List<CatalogoLectura> listaFechas = responseCata.body();

                    // PASO 2: Obtener la lista de BookEn (contiene info visual)
                    apiCatalogo.obtenerHistorial(grupo.getIdGrupo()).enqueue(new Callback<List<BookEn>>() {
                        @Override
                        public void onResponse(Call<List<BookEn>> call, Response<List<BookEn>> responseLibros) {
                            if (responseLibros.isSuccessful() && responseLibros.body() != null) {
                                List<BookEn> listaLibros = responseLibros.body();

                                // PASO 3: Enviar ambas listas al adaptador
                                LibroHistorialAdapter adapter = new LibroHistorialAdapter(
                                        listaLibros,
                                        listaFechas,
                                        libro -> {
                                            Intent intent = new Intent(getContext(), ValoracionesLibro.class);
                                            intent.putExtra("idLibro", libro.getId());
                                            intent.putExtra("idGrupo", grupo.getIdGrupo());
                                            startActivity(intent);
                                        }
                                );
                                recyclerView.setAdapter(adapter);
                            }
                        }
                        @Override public void onFailure(Call<List<BookEn>> call, Throwable t) {}
                    });
                } else {
                    Toast.makeText(getContext(), "Historial vacío", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<CatalogoLectura>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}