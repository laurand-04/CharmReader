package com.tfg.charmreader.menu.adapterRecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tfg.charmreader.R;
import com.tfg.charmreader.objetosBD.LibrosSinEstrenar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LibrosSinEstrenarAdapter extends RecyclerView.Adapter<LibrosSinEstrenarAdapter.LibroViewHolder> {

    private List<LibrosSinEstrenar> libros;
    private OnItemClickListener listener;
    // Formateador para mostrar la fecha de forma amigable (ej: 28/01/2026)
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface OnItemClickListener {
        void onItemClick(LibrosSinEstrenar libro);
    }

    public LibrosSinEstrenarAdapter(List<LibrosSinEstrenar> libros, OnItemClickListener listener) {
        this.libros = libros;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LibroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // CAMBIO 1: Inflamos el nuevo layout item_libro_futuro
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_libro_futuro, parent, false);
        return new LibroViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LibroViewHolder holder, int position) {
        LibrosSinEstrenar libro = libros.get(position);

        if (libro.getId() != null) {
            holder.tvTitulo.setText(libro.getId().getNombre());
        }
        holder.tvAutor.setText(libro.getAutor());

        if (libro.getFechaPublicacion() != null) {
            holder.tvFecha.setText(dateFormat.format(libro.getFechaPublicacion()));

            // --- LÓGICA DE COLORES ---
            Date hoy = new Date();
            if (libro.getFechaPublicacion().before(hoy)) {
                // La fecha ya pasó -> Verde
                holder.tvFecha.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
            } else {
                // La fecha es futura -> Rojo
                holder.tvFecha.setTextColor(android.graphics.Color.parseColor("#F44336"));
            }
        } else {
            holder.tvFecha.setText("Sin fecha");
            holder.tvFecha.setTextColor(android.graphics.Color.GRAY);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(libro);
        });
    }

    @Override
    public int getItemCount() {
        return libros != null ? libros.size() : 0;
    }

    public static class LibroViewHolder extends RecyclerView.ViewHolder {
        // CAMBIO 3: Añadimos tvFecha que existe en el nuevo XML
        TextView tvTitulo, tvAutor, tvFecha;

        public LibroViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloLibro);
            tvAutor = itemView.findViewById(R.id.tvAutorLibro);
            tvFecha = itemView.findViewById(R.id.tvFechaLanzamiento);
        }
    }

    public void setLibros(List<LibrosSinEstrenar> nuevosLibros) {
        this.libros.clear();
        if (nuevosLibros != null) {
            this.libros.addAll(nuevosLibros);
        }
        notifyDataSetChanged();
    }
}