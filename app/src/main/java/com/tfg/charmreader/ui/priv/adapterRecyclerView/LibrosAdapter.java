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


    private String queryActual = "";
    private String estadoActual = "TODOS";
    private String autorActual = null;


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
                .load(libro.getUrlImagen())
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

    /**
     * Filtro por texto (Buscador)
     */
    public void filtrar(String texto) {
        this.queryActual = (texto == null) ? "" : texto.toLowerCase().trim();
        aplicarFiltrosCombinados();
    }

    /**
     * Filtro por estado (Chips)
     */
    public void filtrarPorEstado(String estado) {
        this.estadoActual = estado;
        aplicarFiltrosCombinados();
    }

    /**
     * Filtro por Autor (Menú desplegable)
     */
    public void filtrarPorAutor(String autor) {
        this.autorActual = autor;
        aplicarFiltrosCombinados();
    }

    /**
     * Lógica central de filtrado
     */
    private void aplicarFiltrosCombinados() {
        librosMostrados.clear();

        for (Libro libro : todosLosLibros) {
            // 1. Validar Texto (Nombre o Autor)
            boolean coincideTexto = queryActual.isEmpty() ||
                    libro.getNombre().toLowerCase().contains(queryActual) ||
                    libro.getAutor().toLowerCase().contains(queryActual);

            // 2. Validar Autor específico
            boolean coincideAutor = (autorActual == null) ||
                    libro.getAutor().equalsIgnoreCase(autorActual);

            // 3. Validar Estado
            boolean coincideEstado = true;
            if (!estadoActual.equals("TODOS")) {
                boolean iniciado = estaIniciado(libro.getId());
                boolean finalizado = estaFinalizado(libro.getId());

                switch (estadoActual) {
                    case "SIN_EMPEZAR":
                        coincideEstado = !iniciado;
                        break;
                    case "EMPEZADOS":
                        coincideEstado = (iniciado && !finalizado);
                        break;
                    case "TERMINADOS":
                        coincideEstado = finalizado;
                        break;
                }
            }

            // Si pasa todos los filtros, lo añadimos
            if (coincideTexto && coincideAutor && coincideEstado) {
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

    private boolean estaIniciado(int idLibro) {
        if (relacionesUsuario == null) return false;
        for (LibrosDeUsuario ldu : relacionesUsuario) {
            // Verificamos que el ID no sea nulo antes de comparar
            if (ldu.getId() != null && ldu.getId().getIdL() == idLibro) {
                return ldu.getFechaInicio() != null;
            }
        }
        return false;
    }

    public List<String> getListaAutoresUnicos() {
        List<String> autores = new ArrayList<>();
        for (Libro l : todosLosLibros) {
            if (!autores.contains(l.getAutor())) {
                autores.add(l.getAutor());
            }
        }
        java.util.Collections.sort(autores);
        return autores;
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