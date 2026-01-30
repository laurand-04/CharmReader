package com.tfg.charmreader.menu.priv.adapterRecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tfg.charmreader.R;
import com.tfg.charmreader.objetosBD.BookEn; // Clase de tu BD interna

import java.util.List;

public class BookIntAdapter extends RecyclerView.Adapter<BookIntAdapter.LibroViewHolder> {
    private List<BookEn> libros;
    private OnBookClickListener listener;

    // Interfaz adaptada a BookEn
    public interface OnBookClickListener {
        void onBookClick(BookEn book);
    }

    public BookIntAdapter(List<BookEn> libros, OnBookClickListener listener) {
        this.libros = libros;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LibroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Puedes usar el mismo layout si los IDs (tvTituloItem, etc) coinciden
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_libro_openlibrary, parent, false);
        return new LibroViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LibroViewHolder holder, int position) {
        BookEn libro = libros.get(position);

        // Usamos los getters de tu clase interna (asegúrate de que los nombres coincidan)
        holder.tvTitulo.setText(libro.getTitulo());
        holder.tvAutor.setText(libro.getAutor());

        String idPortada = libro.getCoverId();
        if (idPortada != null && !idPortada.isEmpty() && !idPortada.equals("null")) {
            String urlImagen = "https://covers.openlibrary.org/b/id/" + idPortada + "-M.jpg";

            Glide.with(holder.itemView.getContext())
                    .load(urlImagen)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.ivPortada);
        } else {
            // Si no hay ID, ponemos la imagen de error directamente
            holder.ivPortada.setImageResource(android.R.drawable.stat_notify_error);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(libro);
            }
        });
    }

    @Override
    public int getItemCount() {
        return libros != null ? libros.size() : 0;
    }

    public void updateData(List<BookEn> nuevosLibros) {
        this.libros = nuevosLibros;
        notifyDataSetChanged();
    }

    static class LibroViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo;
        TextView tvAutor;
        ImageView ivPortada;

        public LibroViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloItem);
            tvAutor = itemView.findViewById(R.id.tvAutorItem);
            ivPortada = itemView.findViewById(R.id.ivPortadaItem);
        }
    }

    public void setBooks(List<BookEn> nuevosLibros) {
        this.libros.clear();
        if (nuevosLibros != null) this.libros.addAll(nuevosLibros);
        notifyDataSetChanged();
    }
}