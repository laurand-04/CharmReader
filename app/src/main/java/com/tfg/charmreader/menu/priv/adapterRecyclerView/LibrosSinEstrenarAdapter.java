package com.tfg.charmreader.menu.priv.adapterRecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tfg.charmreader.R;
import com.tfg.charmreader.objetosBD.LibrosSinEstrenar;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LibrosSinEstrenarAdapter extends RecyclerView.Adapter<LibrosSinEstrenarAdapter.LibroViewHolder> {

    private List<LibrosSinEstrenar> libros;
    private List<LibrosSinEstrenar> listaOriginal;
    private OnItemClickListener listener;
    private OnItemLongClickListener longListener; // 🔥 Nuevo listener
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface OnItemClickListener {
        void onItemClick(LibrosSinEstrenar libro);
    }

    // 🔥 Interfaz para borrar
    public interface OnItemLongClickListener {
        void onItemLongClick(LibrosSinEstrenar libro);
    }

    public LibrosSinEstrenarAdapter(List<LibrosSinEstrenar> libros, OnItemClickListener listener) {
        this.libros = (libros != null) ? libros : new ArrayList<>();
        this.listaOriginal = new ArrayList<>(this.libros);
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longListener) {
        this.longListener = longListener;
    }

    @NonNull
    @Override
    public LibroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_libro_futuro, parent, false);
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
            Date hoy = new Date();
            if (libro.getFechaPublicacion().before(hoy)) {
                holder.tvFecha.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
            } else {
                holder.tvFecha.setTextColor(android.graphics.Color.parseColor("#F44336"));
            }
        } else {
            holder.tvFecha.setText("Sin fecha");
            holder.tvFecha.setTextColor(android.graphics.Color.GRAY);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(libro);
        });

        // 🔥 Configuración de clic largo
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
        return libros != null ? libros.size() : 0;
    }

    public void filtrar(String texto) {
        if (texto == null || texto.isEmpty()) {
            libros.clear();
            libros.addAll(listaOriginal);
        } else {
            List<LibrosSinEstrenar> filtrados = new ArrayList<>();
            String query = texto.toLowerCase().trim();
            for (LibrosSinEstrenar libro : listaOriginal) {
                String titulo = (libro.getId() != null) ? libro.getId().getNombre().toLowerCase() : "";
                String autor = (libro.getAutor() != null) ? libro.getAutor().toLowerCase() : "";
                if (titulo.contains(query) || autor.contains(query)) {
                    filtrados.add(libro);
                }
            }
            libros.clear();
            libros.addAll(filtrados);
        }
        notifyDataSetChanged();
    }

    public void setLibros(List<LibrosSinEstrenar> nuevosLibros) {
        this.libros.clear();
        if (nuevosLibros != null) {
            this.libros.addAll(nuevosLibros);
            this.listaOriginal = new ArrayList<>(nuevosLibros);
        }
        notifyDataSetChanged();
    }

    public static class LibroViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvAutor, tvFecha;
        public LibroViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloLibro);
            tvAutor = itemView.findViewById(R.id.tvAutorLibro);
            tvFecha = itemView.findViewById(R.id.tvFechaLanzamiento);
        }
    }
}