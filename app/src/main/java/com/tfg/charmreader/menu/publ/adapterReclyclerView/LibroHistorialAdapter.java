package com.tfg.charmreader.menu.publ.adapterReclyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_ApiValoracion;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.BookEn;
import com.tfg.charmreader.objetosBD.CatalogoLectura;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibroHistorialAdapter extends RecyclerView.Adapter<LibroHistorialAdapter.HistorialViewHolder> {

    private List<BookEn> listaLibros;
    private List<CatalogoLectura> listaFechas;
    private OnItemClickListener listener;

    private final I_ApiValoracion apiValoracion = API.getInstancia().create(I_ApiValoracion.class);

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
        BookEn libro = listaLibros.get(position);
        CatalogoLectura cata = listaFechas.get(position);

        holder.tvTitulo.setText(libro.getTitulo());

        String idPortada = libro.getCoverId();
        if (idPortada != null && !idPortada.isEmpty() && !idPortada.equals("null")) {
            String url = "https://covers.openlibrary.org/b/id/" + idPortada + "-M.jpg";
            Glide.with(holder.itemView.getContext())
                    .load(url)
                    .placeholder(R.drawable.ic_libro)
                    .into(holder.ivPortada);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        if (cata.getFechaComienzo() != null && cata.getFechaFinalizacion() != null) {
            String rango = sdf.format(cata.getFechaComienzo()) + " - " + sdf.format(cata.getFechaFinalizacion());
            holder.tvFechas.setText(rango);
        } else {
            holder.tvFechas.setText("Fechas no disponibles");
        }

        // --- SOLUCIÓN: Cargar Valoración y Texto numérico ---
        apiValoracion.obtenerMediaLibro(cata.getIdGrupo(), libro.getId()).enqueue(new Callback<Double>() {
            @Override
            public void onResponse(Call<Double> call, Response<Double> response) {
                if (response.isSuccessful() && response.body() != null) {
                    float media = response.body().floatValue();

                    // Actualizamos las estrellas
                    holder.ratingBar.setRating(media);

                    // Actualizamos el texto de la media (EJ: "4.5")
                    holder.tvMediaNumerica.setText(String.format(Locale.getDefault(), "%.1f", media));
                } else {
                    holder.ratingBar.setRating(0f);
                    holder.tvMediaNumerica.setText("0.0");
                }
            }

            @Override
            public void onFailure(Call<Double> call, Throwable t) {
                holder.ratingBar.setRating(0f);
                holder.tvMediaNumerica.setText("-.-");
            }
        });

        holder.itemView.setOnClickListener(v -> listener.onItemClick(libro));
    }

    @Override
    public int getItemCount() {
        return (listaLibros != null) ? listaLibros.size() : 0;
    }

    public static class HistorialViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPortada;
        TextView tvTitulo, tvFechas, tvMediaNumerica; // tvMediaNumerica añadido
        RatingBar ratingBar;

        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPortada = itemView.findViewById(R.id.ivPortadaHistorial);
            tvTitulo = itemView.findViewById(R.id.tvTituloHistorial);
            tvFechas = itemView.findViewById(R.id.tvFechasHistorial);
            ratingBar = itemView.findViewById(R.id.ratingHistorial);
            // IMPORTANTE: Referenciamos el TextView del valor numérico
            tvMediaNumerica = itemView.findViewById(R.id.tvMediaNumerica);
        }
    }
}