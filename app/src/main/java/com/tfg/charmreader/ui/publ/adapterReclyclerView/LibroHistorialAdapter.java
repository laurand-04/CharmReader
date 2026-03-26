package com.tfg.charmreader.ui.publ.adapterReclyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tfg.charmreader.R;
import com.tfg.charmreader.data.model.BookEn;
import com.tfg.charmreader.data.model.CatalogoLectura;
import com.tfg.charmreader.data.pojo.LibroHistorialUI;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LibroHistorialAdapter extends RecyclerView.Adapter<LibroHistorialAdapter.HistorialViewHolder> {

    private List<LibroHistorialUI> listaHistorial;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(LibroHistorialUI item);
    }

    public LibroHistorialAdapter(List<LibroHistorialUI> listaHistorial, OnItemClickListener listener) {
        this.listaHistorial = listaHistorial;
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
        LibroHistorialUI item = listaHistorial.get(position);
        BookEn libro = item.getLibro();
        CatalogoLectura cata = item.getCatalogo();

        // 1. Título
        holder.tvTitulo.setText(libro.getTitulo() != null ? libro.getTitulo() : "Sin título");

        // 2. Portada (Glide)
        String idPortada = libro.getCoverId();
        if (idPortada != null && !idPortada.isEmpty() && !idPortada.equals("null")) {
            String url = "https://covers.openlibrary.org/b/id/" + idPortada + "-M.jpg";
            Glide.with(holder.itemView.getContext())
                    .load(url)
                    .placeholder(R.drawable.ic_libro)
                    .error(R.drawable.ic_libro)
                    .centerCrop()
                    .into(holder.ivPortada);
        } else {
            holder.ivPortada.setImageResource(R.drawable.ic_libro);
        }

        // 3. Fechas de lectura
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        if (cata.getFechaComienzo() != null && cata.getFechaFinalizacion() != null) {
            String rango = sdf.format(cata.getFechaComienzo()) + " - " + sdf.format(cata.getFechaFinalizacion());
            holder.tvFechas.setText(rango);
        } else {
            holder.tvFechas.setText("Fechas no disponibles");
        }

        // 4. Valoración Media (Ya viene calculada del ViewModel)
        double media = item.getMediaValoracion();
        if (media > 0) {
            holder.tvMediaNumerica.setText(String.format(Locale.getDefault(), "%.1f", media));
        } else {
            holder.tvMediaNumerica.setText("0.0");
        }

        int sesiones = item.getNumSesiones();
        String texto = sesiones == 1 ? "1 sesión" : sesiones + " sesiones";
        holder.tvSesionesHistorial.setText(texto);

        // 5. Click Listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (listaHistorial != null) ? listaHistorial.size() : 0;
    }

    // Método para actualizar los datos si es necesario
    public void setData(List<LibroHistorialUI> nuevaLista) {
        this.listaHistorial = nuevaLista;
        notifyDataSetChanged();
    }

    public static class HistorialViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPortada;
        TextView tvTitulo, tvFechas, tvMediaNumerica, tvSesionesHistorial;

        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPortada = itemView.findViewById(R.id.ivPortadaHistorial);
            tvTitulo = itemView.findViewById(R.id.tvTituloHistorial);
            tvFechas = itemView.findViewById(R.id.tvFechasHistorial);
            tvMediaNumerica = itemView.findViewById(R.id.tvMediaNumerica);
            tvSesionesHistorial = itemView.findViewById(R.id.tvSesionesHistorial);
        }
    }
}