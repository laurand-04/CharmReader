package com.tfg.charmreader.menu.publ.adapterReclyclerView;

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
import com.tfg.charmreader.interfacesAPI.I_ApiMiembro;
import com.tfg.charmreader.interfacesAPI.I_ApiValoracion;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.GrupoLectura;

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

    private final I_ApiMiembro apiMiembro = API.getInstancia().create(I_ApiMiembro.class);
    private final I_ApiValoracion apiValoracion = API.getInstancia().create(I_ApiValoracion.class);

    public interface OnItemClickListener {
        void onItemClick(GrupoLectura grupo);
    }

    public GrupoLecturaAdapter(List<GrupoLectura> grupos, OnItemClickListener listener) {
        this.grupos = (grupos != null) ? grupos : new ArrayList<>();
        this.listaOriginal = new ArrayList<>(this.grupos);
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Asegúrate de que el nombre del layout sea el correcto (item_grupo_lectura)
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grupo_lectura, parent, false);
        return new GroupViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        GrupoLectura grupo = grupos.get(position);

        holder.tvNombre.setText(grupo.getNombre());
        holder.tvUbicacion.setText(grupo.getUbicacion());
        holder.tvFrecuencia.setText(grupo.getFrecuenciaReunion().toString().toUpperCase());

        // 🔥 CARGA DE IMAGEN CON GLIDE (Sustituye la URL por la imagen real)
        if (grupo.getUrl() != null && !grupo.getUrl().isEmpty()) {
            android.util.Log.d("IMG_DEBUG", "Cargando URL: " + grupo.getUrl());
            Glide.with(holder.itemView.getContext())
                    .load(grupo.getUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Guarda en caché para no descargar siempre
                    .placeholder(R.drawable.ic_people)
                    .error(R.drawable.ic_people)
                    .centerCrop()
                    .into(holder.ivFoto);
        } else {
            holder.ivFoto.setImageResource(R.drawable.ic_people);
        }

        // --- CARGAR CONTEO DE MIEMBROS ---
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

        // --- CARGAR VALORACIÓN MEDIA ---
        apiValoracion.obtenerMediaGrupo(grupo.getIdGrupo()).enqueue(new Callback<Double>() {
            @Override
            public void onResponse(Call<Double> call, Response<Double> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Double media = response.body();
                    holder.tvValoracion.setText(String.format(Locale.getDefault(), "%.1f", media));
                } else {
                    holder.tvValoracion.setText("0.0");
                }
            }
            @Override
            public void onFailure(Call<Double> call, Throwable t) {
                holder.tvValoracion.setText("-.-");
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(grupo);
        });
    }

    @Override
    public int getItemCount() {
        return grupos != null ? grupos.size() : 0;
    }

    public void filtrar(String texto) {
        if (texto == null || texto.isEmpty()) {
            grupos.clear();
            grupos.addAll(listaOriginal);
        } else {
            List<GrupoLectura> filtrados = new ArrayList<>();
            String query = texto.toLowerCase().trim();
            for (GrupoLectura g : listaOriginal) {
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
}