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
import com.tfg.charmreader.objetosBD.Libro;

import java.util.ArrayList;
import java.util.List;

public class LibrosAdapter extends RecyclerView.Adapter<LibrosAdapter.LibroViewHolder> {

    private List<Libro> libros;
    private List<Libro> listaOriginal; // Copia para el filtro
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Libro libro);
    }

    public LibrosAdapter(List<Libro> libros, OnItemClickListener listener) {
        this.libros = libros;
        this.listaOriginal = new ArrayList<>(libros);
        this.listener = listener;
    }

    @NonNull
    @Override
    public LibroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_libro, parent, false);
        return new LibroViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LibroViewHolder holder, int position) {
        Libro libro = libros.get(position);
        holder.tvTitulo.setText(libro.getNombre());
        holder.tvAutor.setText(libro.getAutor());

        // Cargar portada con Glide
        if (libro.getUrl() != null && !libro.getUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(libro.getUrl())
                    .placeholder(R.drawable.ic_libro)
                    .error(R.drawable.ic_libro)
                    .into(holder.ivPortada);
        } else {
            holder.ivPortada.setImageResource(R.drawable.ic_libro);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(libro);
        });
    }

    @Override
    public int getItemCount() {
        return libros.size();
    }

    public void setLibros(List<Libro> nuevosLibros) {
        this.libros.clear();
        if (nuevosLibros != null) {
            this.libros.addAll(nuevosLibros);
            this.listaOriginal = new ArrayList<>(nuevosLibros);
        }
        notifyDataSetChanged();
    }

    // Lógica de búsqueda
    public void filtrar(String texto) {
        if (texto == null || texto.isEmpty()) {
            libros.clear();
            libros.addAll(listaOriginal);
        } else {
            List<Libro> filtrados = new ArrayList<>();
            String query = texto.toLowerCase().trim();
            for (Libro l : listaOriginal) {
                if (l.getNombre().toLowerCase().contains(query) ||
                        l.getAutor().toLowerCase().contains(query)) {
                    filtrados.add(l);
                }
            }
            libros.clear();
            libros.addAll(filtrados);
        }
        notifyDataSetChanged();
    }

    public static class LibroViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvAutor;
        ImageView ivPortada;

        public LibroViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloLibro);
            tvAutor = itemView.findViewById(R.id.tvAutorLibro);
            ivPortada = itemView.findViewById(R.id.ivPortadaLibro);
        }
    }
}