package com.tfg.charmreader.ui.priv.adapterRecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tfg.charmreader.R;
import com.tfg.charmreader.data.model.Libro;
import com.tfg.charmreader.data.model.LibrosDeUsuario;

import java.util.ArrayList;
import java.util.List;

public class LibrosAdapter extends RecyclerView.Adapter<LibrosAdapter.LibroViewHolder> {

    private List<Libro> librosMostrados;
    private List<Libro> todosLosLibros;
    private List<LibrosDeUsuario> relacionesUsuario;
    private OnItemClickListener listener;
    private OnItemLongClickListener longListener;
    private boolean soloPendientes = false;

    public interface OnItemClickListener {
        void onItemClick(Libro libro);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Libro libro);
    }

    public LibrosAdapter(List<Libro> libros, OnItemClickListener listener) {
        // Inicializar siempre para evitar NullPointerException
        this.todosLosLibros = (libros != null) ? new ArrayList<>(libros) : new ArrayList<>();
        this.librosMostrados = new ArrayList<>(this.todosLosLibros);
        this.relacionesUsuario = new ArrayList<>();
        this.listener = listener;
    }

    public void setSoloPendientes(boolean soloPendientes) {
        this.soloPendientes = soloPendientes;
        // Importante: Refrescar la vista si se cambia el flag
        filtrar("");
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longListener) {
        this.longListener = longListener;
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
        if (librosMostrados.isEmpty()) return;

        Libro libro = librosMostrados.get(position);
        holder.tvTitulo.setText(libro.getNombre());
        holder.tvAutor.setText(libro.getAutor());

        // Manejo del Badge
        if (estaFinalizado(libro.getId())) {
            holder.tvBadgeLeido.setVisibility(View.VISIBLE);
        } else {
            holder.tvBadgeLeido.setVisibility(View.GONE);
        }

        // Carga de imagen con Glide
        Glide.with(holder.itemView.getContext())
                .load(libro.getUrl())
                .placeholder(R.drawable.ic_libro)
                .error(R.drawable.ic_libro)
                .into(holder.ivPortada);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(libro);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longListener != null) {
                longListener.onItemLongClick(libro);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return librosMostrados.size();
    }

    public void setData(List<Libro> nuevosLibros, List<LibrosDeUsuario> nuevasRelaciones) {
        this.todosLosLibros.clear();
        this.todosLosLibros.addAll(nuevosLibros != null ? nuevosLibros : new ArrayList<>());

        this.relacionesUsuario.clear();
        this.relacionesUsuario.addAll(nuevasRelaciones != null ? nuevasRelaciones : new ArrayList<>());

        // 🔥 Forzamos el filtrado para que aplique la lógica de ocultar leídos o mostrar badges
        filtrar("");
    }

    public void filtrar(String texto) {
        librosMostrados.clear();
        String query = (texto == null) ? "" : texto.toLowerCase().trim();

        for (Libro libro : todosLosLibros) {
            // 1. Verificamos si el libro coincide con el nombre o autor
            boolean coincideBusqueda = query.isEmpty() ||
                    libro.getNombre().toLowerCase().contains(query) ||
                    libro.getAutor().toLowerCase().contains(query);

            if (coincideBusqueda) {
                boolean finalizado = estaFinalizado(libro.getId());

                // 2. Aplicamos la lógica de visualización:
                // Si el buscador está VACÍO y queremos SOLO PENDIENTES, saltamos los finalizados.
                // Si el buscador TIENE TEXTO, ignoramos el flag 'soloPendientes' y mostramos tod0.
                if (query.isEmpty() && soloPendientes && finalizado) {
                    continue;
                }

                librosMostrados.add(libro);
            }
        }
        notifyDataSetChanged();
    }

    private boolean estaFinalizado(int idLibro) {
        if (relacionesUsuario == null) return false;
        for (LibrosDeUsuario ldu : relacionesUsuario) {
            // Verificamos que el ID no sea nulo antes de comparar
            if (ldu.getId() != null && ldu.getId().getIdL() == idLibro) {
                return ldu.getFechaFin() != null;
            }
        }
        return false;
    }

    public static class LibroViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvAutor;
        ImageView ivPortada;
        View tvBadgeLeido;

        public LibroViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloLibro);
            tvAutor = itemView.findViewById(R.id.tvAutorLibro);
            ivPortada = itemView.findViewById(R.id.ivPortadaLibro);
            tvBadgeLeido = itemView.findViewById(R.id.tvBadgeLeido);
        }
    }
}