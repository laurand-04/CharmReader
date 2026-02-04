package com.tfg.charmreader.menu.publ.adapterReclyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_ApiVotacion;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.BookEn;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibroPropuestoAdministradorAdapter extends RecyclerView.Adapter<LibroPropuestoAdministradorAdapter.ViewHolder> {

    private List<BookEn> listaLibros;
    private int idGrupo;
    private final I_ApiVotacion apiVotacion = API.getInstancia().create(I_ApiVotacion.class);

    // 🔹 Interfaz para el clic
    private OnLibroClickListener listener;

    public interface OnLibroClickListener {
        void onLibroClick(BookEn libro);
    }

    // 🔹 Constructor actualizado
    public LibroPropuestoAdministradorAdapter(List<BookEn> listaLibros, int idGrupo, OnLibroClickListener listener) {
        this.listaLibros = listaLibros;
        this.idGrupo = idGrupo;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_libro_propuesto_administrador, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookEn libro = listaLibros.get(position);

        holder.tvTitulo.setText(libro.getTitulo());
        holder.tvAutor.setText(libro.getAutor());
        holder.tvVotos.setText("...");

        // 🔹 Clic en el elemento
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onLibroClick(libro);
        });

        String coverId = libro.getCoverId();
        if (coverId != null && !coverId.isEmpty() && !coverId.equals("null")) {
            String urlImagen = "https://covers.openlibrary.org/b/id/" + coverId + "-M.jpg";
            Glide.with(holder.itemView.getContext())
                    .load(urlImagen)
                    .placeholder(R.drawable.ic_libro)
                    .error(R.drawable.ic_libro)
                    .into(holder.ivPortada);
        } else {
            holder.ivPortada.setImageResource(R.drawable.ic_libro);
        }

        apiVotacion.obtenerConteoVotos(idGrupo, libro.getId()).enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful() && response.body() != null) {
                    holder.tvVotos.setText(response.body() + " votos");
                } else {
                    holder.tvVotos.setText("0 votos");
                }
            }
            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                holder.tvVotos.setText("0 votos");
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaLibros != null ? listaLibros.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvAutor, tvVotos;
        ImageView ivPortada;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloLibro);
            tvAutor = itemView.findViewById(R.id.tvAutorLibro);
            tvVotos = itemView.findViewById(R.id.tvVotosLibro);
            ivPortada = itemView.findViewById(R.id.ivPortadaLibro);
        }
    }
}