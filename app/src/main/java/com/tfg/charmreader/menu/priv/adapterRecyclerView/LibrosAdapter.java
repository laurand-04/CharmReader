package com.tfg.charmreader.menu.priv.adapterRecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tfg.charmreader.R;
import com.tfg.charmreader.objetosBD.Libro;

import java.util.List;

public class LibrosAdapter extends RecyclerView.Adapter<LibrosAdapter.LibroViewHolder> {

    private List<Libro> libros;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Libro libro);
    }

    public LibrosAdapter(List<Libro> libros, OnItemClickListener listener) {
        this.libros = libros;
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

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(libro);
            }
        });
    }

    @Override
    public int getItemCount() {
        return libros.size();
    }

    public static class LibroViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvAutor;

        public LibroViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloLibro);
            tvAutor = itemView.findViewById(R.id.tvAutorLibro);
        }
    }

    public void setLibros(List<Libro> nuevosLibros) {
        this.libros.clear();
        if (nuevosLibros != null) this.libros.addAll(nuevosLibros);
        notifyDataSetChanged();
    }
}

