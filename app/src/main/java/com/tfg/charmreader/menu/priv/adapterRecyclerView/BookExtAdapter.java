package com.tfg.charmreader.menu.priv.adapterRecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // IMPORTANTE
import android.widget.TextView;  // IMPORTANTE

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tfg.charmreader.R;
import com.tfg.charmreader.objetosBD.Book; // USA TU CLASE, NO LA DE EPUBLIB

import java.util.List;

public class BookExtAdapter extends RecyclerView.Adapter<BookExtAdapter.LibroViewHolder> {
        private List<Book> libros;
    private OnBookClickListener listener; // Campo para el listener

    // Interfaz para que la Activity escuche los clics
    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    // Actualizamos el constructor para recibir el listener
    public BookExtAdapter(List<Book> libros, OnBookClickListener listener) {
        this.libros = libros;
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
        Book libro = libros.get(position);

        holder.tvTitulo.setText(libro.getTitle());
        holder.tvAutor.setText(libro.getFirstAuthor());

        String idPortada = libro.getCoverId();
        String urlCompleta = null;
        if (idPortada != null && !idPortada.isEmpty()) {
            urlCompleta = "https://covers.openlibrary.org/b/id/" + idPortada + "-M.jpg";
            Glide.with(holder.itemView.getContext()).load(urlCompleta).into(holder.ivPortada);
        } else {
            holder.ivPortada.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        Glide.with(holder.itemView.getContext())
                .load(urlCompleta) // Ahora Glide recibe una URL web y sabe que debe descargarla
                .placeholder(android.R.drawable.ic_menu_gallery) // Imagen mientras carga
                .error(android.R.drawable.stat_notify_error)      // Imagen si falla la descarga
                .into(holder.ivPortada);

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

    public void updateData(List<Book> nuevosLibros) {
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

    public void setBooks(List<Book> nuevosLibros) {
        this.libros.clear();
        if (nuevosLibros != null) {
            this.libros.addAll(nuevosLibros);
        }
        notifyDataSetChanged();
    }
}