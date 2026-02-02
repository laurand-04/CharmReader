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
import com.tfg.charmreader.interfacesAPI.I_ApiBook;
import com.tfg.charmreader.interfacesAPI.I_ApiSesion;
import com.tfg.charmreader.interfacesAPI.I_ApiValoracion;
import com.tfg.charmreader.objetosBD.BookEn;
import com.tfg.charmreader.objetosBD.CatalogoLectura;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibroHistorialAdapter extends RecyclerView.Adapter<LibroHistorialAdapter.HistorialViewHolder> {

    private List<CatalogoLectura> historial;
    private int idGrupo;
    private I_ApiBook apiBook;
    private I_ApiSesion apiSesion;
    private I_ApiValoracion apiValoracion;
    private OnItemClickListener listener;

    // Interfaz para gestionar el clic
    public interface OnItemClickListener {
        void onItemClick(CatalogoLectura registro);
    }

    public LibroHistorialAdapter(List<CatalogoLectura> historial, int idGrupo,
                                 I_ApiBook apiBook, I_ApiSesion apiSesion,
                                 I_ApiValoracion apiValoracion, OnItemClickListener listener) {
        this.historial = historial;
        this.idGrupo = idGrupo;
        this.apiBook = apiBook;
        this.apiSesion = apiSesion;
        this.apiValoracion = apiValoracion;
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
        CatalogoLectura registro = historial.get(position);
        int idLibro = registro.getIdBook();

        // 1. FORMATEO DE FECHAS
        SimpleDateFormat sdfApi = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat sdfVista = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        String fInicioApi = (registro.getFechaComienzo() != null) ? sdfApi.format(registro.getFechaComienzo()) : "";
        String fFinApi = (registro.getFechaFinalizacion() != null) ? sdfApi.format(registro.getFechaFinalizacion()) : "";

        if (registro.getFechaComienzo() != null && registro.getFechaFinalizacion() != null) {
            String fechasFormateadas = sdfVista.format(registro.getFechaComienzo()) + " - " + sdfVista.format(registro.getFechaFinalizacion());
            holder.tvFechas.setText(fechasFormateadas);
        } else {
            holder.tvFechas.setText("Fechas no disponibles");
        }

        // 2. DETALLES DEL LIBRO Y PORTADA
        apiBook.obtenerBookPorId(idLibro).enqueue(new Callback<BookEn>() {
            @Override
            public void onResponse(Call<BookEn> call, Response<BookEn> response) {
                if (response.isSuccessful() && response.body() != null) {
                    holder.tvTitulo.setText(response.body().getTitulo());
                    String idPortada = response.body().getCoverId();
                    if (idPortada != null && !idPortada.isEmpty() && !idPortada.equals("null")) {
                        String urlImagen = "https://covers.openlibrary.org/b/id/" + idPortada + "-M.jpg";
                        Glide.with(holder.itemView.getContext())
                                .load(urlImagen)
                                .placeholder(R.drawable.ic_libro)
                                .error(android.R.drawable.stat_notify_error)
                                .into(holder.ivPortada);
                    } else {
                        holder.ivPortada.setImageResource(R.drawable.ic_libro);
                    }
                }
            }
            @Override public void onFailure(Call<BookEn> call, Throwable t) {
                holder.tvTitulo.setText("Error al cargar libro");
            }
        });

        // 3. CONTEO DE SESIONES
        if (!fInicioApi.isEmpty() && !fFinApi.isEmpty()) {
            apiSesion.contarSesionesPorRango(idGrupo, fInicioApi, fFinApi)
                    .enqueue(new Callback<Long>() {
                        @Override
                        public void onResponse(Call<Long> call, Response<Long> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                holder.tvSesiones.setText("Sesiones totales: " + response.body());
                            }
                        }
                        @Override public void onFailure(Call<Long> call, Throwable t) {}
                    });
        }

        // 4. VALORACIÓN MEDIA
        apiValoracion.obtenerMediaLibro(idGrupo, idLibro).enqueue(new Callback<Double>() {
            @Override
            public void onResponse(Call<Double> call, Response<Double> response) {
                if (response.isSuccessful() && response.body() != null) {
                    holder.ratingMedia.setRating(response.body().floatValue());
                }
            }
            @Override public void onFailure(Call<Double> call, Throwable t) {}
        });

        // GESTIÓN DEL CLIC
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(registro);
            }
        });
    }

    @Override
    public int getItemCount() {
        return historial != null ? historial.size() : 0;
    }

    public static class HistorialViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPortada;
        TextView tvTitulo, tvFechas, tvSesiones;
        RatingBar ratingMedia;

        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPortada = itemView.findViewById(R.id.ivPortadaHistorial);
            tvTitulo = itemView.findViewById(R.id.tvTituloHistorial);
            tvFechas = itemView.findViewById(R.id.tvFechasHistorial);
            tvSesiones = itemView.findViewById(R.id.tvSesionesHistorial);
            ratingMedia = itemView.findViewById(R.id.ratingHistorial);
        }
    }
}