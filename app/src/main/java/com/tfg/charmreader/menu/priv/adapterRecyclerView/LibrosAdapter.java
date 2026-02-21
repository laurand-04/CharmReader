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
import com.tfg.charmreader.objetosBD.LibrosDeUsuario;

import java.util.ArrayList;
import java.util.List;

public class LibrosAdapter extends RecyclerView.Adapter<LibrosAdapter.LibroViewHolder> {

    private List<Libro> librosMostrados;
    private List<Libro> todosLosLibros;
    private List<LibrosDeUsuario> relacionesUsuario;
    private OnItemClickListener listener;
    private OnItemLongClickListener longListener;
    private boolean soloPendientes = false; // 🔥 Flag para controlar el filtrado

    public interface OnItemClickListener {
        void onItemClick(Libro libro);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Libro libro);
    }

    public LibrosAdapter(List<Libro> libros, OnItemClickListener listener) {
        this.librosMostrados = new ArrayList<>();
        this.todosLosLibros = new ArrayList<>(libros);
        this.relacionesUsuario = new ArrayList<>();
        this.listener = listener;
    }

    // 🔥 Método para activar/desactivar el filtrado de leídos
    public void setSoloPendientes(boolean soloPendientes) {
        this.soloPendientes = soloPendientes;
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
        Libro libro = librosMostrados.get(position);
        holder.tvTitulo.setText(libro.getNombre());
        holder.tvAutor.setText(libro.getAutor());

        if (estaFinalizado(libro.getId())) {
            holder.tvBadgeLeido.setVisibility(View.VISIBLE);
        } else {
            holder.tvBadgeLeido.setVisibility(View.GONE);
        }

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
        this.todosLosLibros = (nuevosLibros != null) ? nuevosLibros : new ArrayList<>();
        this.relacionesUsuario = (nuevasRelaciones != null) ? nuevasRelaciones : new ArrayList<>();
        filtrar("");
    }

    public void filtrar(String texto) {
        librosMostrados.clear();
        String query = (texto == null) ? "" : texto.toLowerCase().trim();

        for (Libro libro : todosLosLibros) {
            boolean coincideBusqueda = query.isEmpty() ||
                    libro.getNombre().toLowerCase().contains(query) ||
                    libro.getAutor().toLowerCase().contains(query);

            if (coincideBusqueda) {
                // 🔥 Lógica Clave: Si soloPendientes es true Y está finalizado, NO lo añadimos.
                // Pero si hay texto en el buscador, ignoramos el filtro de pendientes para que aparezca todo.
                if (soloPendientes && query.isEmpty() && estaFinalizado(libro.getId())) {
                    continue;
                }
                librosMostrados.add(libro);
            }
        }
        notifyDataSetChanged();
    }

    private boolean estaFinalizado(int idLibro) {
        for (LibrosDeUsuario ldu : relacionesUsuario) {
            if (ldu.getId().getIdL() == idLibro) {
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