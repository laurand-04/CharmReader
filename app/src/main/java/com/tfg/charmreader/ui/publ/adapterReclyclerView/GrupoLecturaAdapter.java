package com.tfg.charmreader.ui.publ.adapterReclyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tfg.charmreader.R;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiMiembro;
import com.tfg.charmreader.data.network.API.API;
import com.tfg.charmreader.data.model.GrupoLectura;
import com.tfg.charmreader.data.repository.publ.InfoGrupoRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GrupoLecturaAdapter extends RecyclerView.Adapter<GrupoLecturaAdapter.GroupViewHolder> {

    private List<GrupoLectura> grupos;
    private List<GrupoLectura> listaOriginal;
    private OnItemClickListener listener;
    private OnItemLongClickListener longListener; // 🔥 Listener para clic largo
    //No podemos quitar I_ApiMiembro
    private final I_ApiMiembro apiMiembro = API.getInstancia().create(I_ApiMiembro.class);
    private final InfoGrupoRepository infoGrupoRepository = new InfoGrupoRepository();


    // 🔥 Interfaz para el clic

    public interface OnItemClickListener {
        void onItemClick(GrupoLectura grupo);
    }

    // 🔥 Interfaz para el borrado/ceder control
    public interface OnItemLongClickListener {
        void onItemLongClick(GrupoLectura grupo);
    }

    public GrupoLecturaAdapter(List<GrupoLectura> grupos, OnItemClickListener listener) {
        this.grupos = (grupos != null) ? grupos : new ArrayList<>();
        this.listaOriginal = new ArrayList<>(this.grupos);
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longListener) {
        this.longListener = longListener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grupo_lectura, parent, false);
        return new GroupViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        GrupoLectura grupo = grupos.get(position);

        holder.tvNombre.setText(grupo.getNombre());
        holder.tvUbicacion.setText(grupo.getUbicacion());
        holder.tvFrecuencia.setText(grupo.getFrecuenciaReunion().toString().toUpperCase());

        if (grupo.getUrl() != null && !grupo.getUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(grupo.getUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_people)
                    .error(R.drawable.ic_people)
                    .centerCrop()
                    .into(holder.ivFoto);
        } else {
            holder.ivFoto.setImageResource(R.drawable.ic_people);
        }

        apiMiembro.contarMiembros(grupo.getIdGrupo()).enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful() && response.body() != null) {
                    holder.tvMiembros.setText(response.body() + " miembros");
                }
            }
            @Override public void onFailure(Call<Long> call, Throwable t) { holder.tvMiembros.setText("0 miembros"); }
        });

        infoGrupoRepository.obtenerMediaGrupo(grupo.getIdGrupo(), new Callback<Double>() {
            @Override
            public void onResponse(Call<Double> call, Response<Double> response) {
                if (response.isSuccessful() && response.body() != null) {
                    holder.tvValoracion.setText(String.format(Locale.getDefault(), "%.1f", response.body()));
                } else { holder.tvValoracion.setText("0.0"); }
            }
            @Override public void onFailure(Call<Double> call, Throwable t) { holder.tvValoracion.setText("-.-"); }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(grupo);
        });

        // 🔥 Configuración de clic largo
        holder.itemView.setOnLongClickListener(v -> {
            if (longListener != null) {
                longListener.onItemLongClick(grupo);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() { return grupos != null ? grupos.size() : 0; }

    public void setGrupoLectura(List<GrupoLectura> nuevosGrupos) {
        this.grupos.clear();
        if (nuevosGrupos != null) {
            this.grupos.addAll(nuevosGrupos);
            this.listaOriginal = new ArrayList<>(nuevosGrupos);
        }
        notifyDataSetChanged();
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvUbicacion, tvFrecuencia, tvMiembros, tvValoracion;
        ImageView ivFoto;
        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreGrupo);
            tvUbicacion = itemView.findViewById(R.id.tvUbicacionGrupo);
            tvFrecuencia = itemView.findViewById(R.id.tvFrecuenciaBadge);
            tvMiembros = itemView.findViewById(R.id.tvMiembrosCount);
            tvValoracion = itemView.findViewById(R.id.tvValoracionMedia);
            ivFoto = itemView.findViewById(R.id.ivGrupoFoto);
        }
    }

    public void filtrar(String texto) {
        if (texto == null || texto.isEmpty()) {
            grupos.clear();
            grupos.addAll(listaOriginal);
        } else {
            List<GrupoLectura> filtrados = new ArrayList<>();
            String query = texto.toLowerCase().trim();
            for (GrupoLectura g : listaOriginal) {
                // Filtramos por nombre del grupo o por ubicación
                if (g.getNombre().toLowerCase().contains(query) ||
                        g.getUbicacion().toLowerCase().contains(query)) {
                    filtrados.add(g);
                }
            }
            grupos.clear();
            grupos.addAll(filtrados);
        }
        notifyDataSetChanged();
    }
}