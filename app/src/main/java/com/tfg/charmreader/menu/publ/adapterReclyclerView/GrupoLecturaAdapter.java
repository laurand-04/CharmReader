package com.tfg.charmreader.menu.publ.adapterReclyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_ApiMiembro;
import com.tfg.charmreader.interfacesAPI.I_ApiValoracion;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.GrupoLectura;
import com.tfg.charmreader.objetosBD.Valoracion;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GrupoLecturaAdapter extends RecyclerView.Adapter<GrupoLecturaAdapter.GroupViewHolder> {

    private List<GrupoLectura> grupos;
    private OnItemClickListener listener;
    private final I_ApiMiembro apiMiembro = API.getInstancia().create(I_ApiMiembro.class);
    private final I_ApiValoracion apiValoracion = API.getInstancia().create(I_ApiValoracion.class);

    public interface OnItemClickListener {
        void onItemClick(GrupoLectura grupo);
    }

    public GrupoLecturaAdapter(List<GrupoLectura> grupos, OnItemClickListener listener) {
        this.grupos = (grupos != null) ? grupos : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grupo_lectura, parent, false);
        return new GroupViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        GrupoLectura grupo = grupos.get(position);

        holder.tvNombre.setText(grupo.getNombre());
        holder.tvDescripcion.setText(grupo.getDescripcion());
        holder.tvUbicacion.setText(grupo.getUbicacion());
        holder.tvFrecuencia.setText(grupo.getFrecuenciaReunion().toString());

        // --- NUEVO: Cargar Conteo de Miembros ---
        apiMiembro.contarMiembros(grupo.getIdGrupo()).enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful() && response.body() != null) {
                    holder.tvMiembros.setText(response.body() + " miembros");
                }
            }
            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                holder.tvMiembros.setText("0 miembros");
            }
        });

        // --- NUEVO: Cargar Valoración Media ---
        apiValoracion.verValoraciones(Valoracion.TipoValoracion.GRUPO, grupo.getIdGrupo()).enqueue(new Callback<List<Valoracion>>() {
            @Override
            public void onResponse(Call<List<Valoracion>> call, Response<List<Valoracion>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<Valoracion> vals = response.body();
                    double suma = 0;
                    for (Valoracion v : vals) suma += v.getCalificacion();
                    double media = suma / vals.size();
                    holder.tvValoracion.setText(String.format(Locale.getDefault(), "%.1f (%d)", media, vals.size()));
                } else {
                    holder.tvValoracion.setText("0.0 (0)");
                }
            }
            @Override
            public void onFailure(Call<List<Valoracion>> call, Throwable t) {
                holder.tvValoracion.setText("-.-");
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(grupo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return grupos != null ? grupos.size() : 0;
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        // NUEVO: Añadidos tvMiembros y tvValoracion
        TextView tvNombre, tvDescripcion, tvUbicacion, tvFrecuencia, tvMiembros, tvValoracion;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreGrupo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionBreve);
            tvUbicacion = itemView.findViewById(R.id.tvUbicacionGrupo);
            tvFrecuencia = itemView.findViewById(R.id.tvFrecuenciaBadge);

            // NUEVO: Referencias a los nuevos IDs del XML
            tvMiembros = itemView.findViewById(R.id.tvContadorMiembros);
            tvValoracion = itemView.findViewById(R.id.tvValoracionMedia);
        }
    }

    public void setGrupoLectura(List<GrupoLectura> nuevosGrupos) {
        this.grupos.clear();
        if (nuevosGrupos != null) {
            this.grupos.addAll(nuevosGrupos);
        }
        notifyDataSetChanged();
    }
}