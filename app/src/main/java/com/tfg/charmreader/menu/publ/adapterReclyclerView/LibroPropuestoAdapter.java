package com.tfg.charmreader.menu.publ.adapterReclyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_ApiVotacion;
import com.tfg.charmreader.objetosBD.BookEn;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibroPropuestoAdapter extends RecyclerView.Adapter<LibroPropuestoAdapter.LibroViewHolder> {

    private List<BookEn> libros;
    private OnItemClickListener listener;
    private int totalMiembrosGrupo;

    private I_ApiVotacion apiVotacion;
    private int idUsuario;
    private int idGrupo;

    public interface OnItemClickListener {
        void onItemClick(BookEn libro);
        void onVotarClick(BookEn libro);
    }

    public LibroPropuestoAdapter(List<BookEn> libros, int totalMiembros, OnItemClickListener listener) {
        this.libros = (libros != null) ? libros : new ArrayList<>();
        this.totalMiembrosGrupo = totalMiembros;
        this.listener = listener;
    }

    public void setVotacionApi(I_ApiVotacion api, int idUsuario, int idGrupo) {
        this.apiVotacion = api;
        this.idUsuario = idUsuario;
        this.idGrupo = idGrupo;
    }

    @NonNull
    @Override
    public LibroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_libro_propuesto, parent, false);
        return new LibroViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LibroViewHolder holder, int position) {
        BookEn libro = libros.get(position);

        holder.tvTitulo.setText(libro.getTitulo());
        holder.autor.setText(libro.getAutor());

        actualizarConteoVotos(holder, libro.getId());
        comprobarEstadoBoton(holder, libro.getId());

        String idPortada = libro.getCoverId();
        if (idPortada != null && !idPortada.isEmpty() && !idPortada.equals("null")) {
            String urlImagen = "https://covers.openlibrary.org/b/id/" + idPortada + "-M.jpg";

            Glide.with(holder.itemView.getContext())
                    .load(urlImagen)
                    .placeholder(R.drawable.ic_libro)
                    .centerCrop() // Para que rellene el hueco igual que en historial
                    .into(holder.portada);
        } else {
            holder.portada.setImageResource(R.drawable.ic_libro);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(libro));
        holder.btnVotar.setOnClickListener(v -> listener.onVotarClick(libro));
    }

    private void actualizarConteoVotos(LibroViewHolder holder, int idLibro) {
        if (apiVotacion != null) {
            apiVotacion.obtenerConteoVotos(idGrupo, idLibro).enqueue(new Callback<Long>() {
                @Override
                public void onResponse(Call<Long> call, Response<Long> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        holder.tvVotos.setText("Votos: " + response.body() + "/" + totalMiembrosGrupo);
                    }
                }
                @Override public void onFailure(Call<Long> call, Throwable t) {}
            });
        }
    }

    private void comprobarEstadoBoton(LibroViewHolder holder, int idLibro) {
        if (apiVotacion != null) {
            apiVotacion.comprobarEstado(idUsuario, idGrupo, idLibro).enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        boolean yaVotado = response.body();

                        holder.btnVotar.post(() -> {
                            holder.btnVotar.setText(yaVotado ? "RETIRAR" : "VOTAR");
                            if (yaVotado) {
                                holder.btnVotar.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_red_dark));
                            } else {
                                holder.btnVotar.setTextColor(holder.itemView.getContext().getColor(R.color.basico));
                            }
                        });
                    }
                }
                @Override public void onFailure(Call<Boolean> call, Throwable t) {}
            });
        }
    }

    @Override
    public int getItemCount() { return libros.size(); }

    public static class LibroViewHolder extends RecyclerView.ViewHolder {
        ImageView portada;
        TextView tvTitulo, autor, tvVotos;
        MaterialButton btnVotar;

        public LibroViewHolder(@NonNull View itemView) {
            super(itemView);
            portada = itemView.findViewById(R.id.ivPortadaPropuesta);
            tvTitulo = itemView.findViewById(R.id.tvTituloPropuesta);
            autor = itemView.findViewById(R.id.tvAutorPropuesta);
            tvVotos = itemView.findViewById(R.id.tvVotos);
            btnVotar = itemView.findViewById(R.id.btnVotar);
        }
    }
}