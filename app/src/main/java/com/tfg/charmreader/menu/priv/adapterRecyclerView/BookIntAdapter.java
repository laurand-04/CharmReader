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
import com.tfg.charmreader.objetosBD.BookEn;

import java.util.ArrayList;
import java.util.List;

public class BookIntAdapter extends RecyclerView.Adapter<BookIntAdapter.LibroViewHolder> {
    private List<BookEn> libros;
    private List<BookEn> listaOriginal; // 🔥 Copia para el filtro
    private OnBookClickListener listener;

    public interface OnBookClickListener {
        void onBookClick(BookEn book);
    }

    public BookIntAdapter(List<BookEn> libros, OnBookClickListener listener) {
        this.libros = (libros != null) ? libros : new ArrayList<>();
        this.listaOriginal = new ArrayList<>(this.libros);
        this.listener = listener;
    }

    @NonNull
    @Override
    public LibroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_libro_openlibrary, parent, false);
        return new LibroViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LibroViewHolder holder, int position) {
        BookEn libro = libros.get(position);
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
            holder.ivPortada.setImageResource(android.R.drawable.stat_notify_error);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onBookClick(libro);
        });
    }

    @Override
    public int getItemCount() {
        return libros != null ? libros.size() : 0;
    }

    // 🔥 MÉTODO PARA FILTRAR
    public void filtrar(String texto) {
        if (texto == null || texto.isEmpty()) {
            libros.clear();
            libros.addAll(listaOriginal);
        } else {
            List<BookEn> filtrados = new ArrayList<>();
            String query = texto.toLowerCase().trim();
            for (BookEn b : listaOriginal) {
                if (b.getTitulo().toLowerCase().contains(query) ||
                        b.getAutor().toLowerCase().contains(query)) {
                    filtrados.add(b);
                }
            }
            libros.clear();
            libros.addAll(filtrados);
        }
        notifyDataSetChanged();
    }

    public void setBooks(List<BookEn> nuevosLibros) {
        this.libros.clear();
        if (nuevosLibros != null) {
            this.libros.addAll(nuevosLibros);
            this.listaOriginal = new ArrayList<>(nuevosLibros); // Actualizar copia
        }
        notifyDataSetChanged();
    }

    static class LibroViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvAutor;
        ImageView ivPortada;

        public LibroViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloItem);
            tvAutor = itemView.findViewById(R.id.tvAutorItem);
            ivPortada = itemView.findViewById(R.id.ivPortadaItem);
        }
    }
}