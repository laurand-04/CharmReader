package com.tfg.charmreader.menu.publ.adapterReclyclerView;

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
import com.tfg.charmreader.objetosBD.CatalogoLectura;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LibroHistorialAdapter extends RecyclerView.Adapter<LibroHistorialAdapter.HistorialViewHolder> {

    private List<BookEn> listaLibros;
    private List<CatalogoLectura> listaFechas;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(BookEn libro);
    }

    public LibroHistorialAdapter(List<BookEn> libros, List<CatalogoLectura> fechas, OnItemClickListener listener) {
        this.listaLibros = libros;
        this.listaFechas = fechas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_libro_historial, parent, false);
        return new HistorialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        // Sincronización por posición
        BookEn libro = listaLibros.get(position);
        CatalogoLectura cata = listaFechas.get(position);

        // 1. Mostrar datos del libro
        holder.tvTitulo.setText(libro.getTitulo());

        String idPortada = libro.getCoverId();
        if (idPortada != null && !idPortada.isEmpty() && !idPortada.equals("null")) {
            String url = "https://covers.openlibrary.org/b/id/" + idPortada + "-M.jpg";
            Glide.with(holder.itemView.getContext())
                    .load(url)
                    .placeholder(R.drawable.ic_libro)
                    .into(holder.ivPortada);
        }

        // 2. Mostrar fechas desde el objeto CatalogoLectura
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        if (cata.getFechaComienzo() != null && cata.getFechaFinalizacion() != null) {
            String rango = sdf.format(cata.getFechaComienzo()) + " - " + sdf.format(cata.getFechaFinalizacion());
            holder.tvFechas.setText(rango);
        } else {
            holder.tvFechas.setText("Fechas no disponibles");
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(libro));
    }

    @Override
    public int getItemCount() {
        return (listaLibros != null) ? listaLibros.size() : 0;
    }

    public static class HistorialViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPortada;
        TextView tvTitulo, tvFechas;

        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPortada = itemView.findViewById(R.id.ivPortadaHistorial);
            tvTitulo = itemView.findViewById(R.id.tvTituloHistorial);
            tvFechas = itemView.findViewById(R.id.tvFechasHistorial);
        }
    }
}