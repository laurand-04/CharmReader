package com.tfg.charmreader.menu.publ.adapterReclyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tfg.charmreader.R;
import com.tfg.charmreader.objetosBD.Valoracion;

import java.util.ArrayList;
import java.util.List;

public class ValoracionAdapter extends RecyclerView.Adapter<ValoracionAdapter.ValoracionViewHolder> {

    private List<Valoracion> valoraciones;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Valoracion valoracion);
    }

    public ValoracionAdapter(List<Valoracion> valoraciones, OnItemClickListener listener) {
        this.valoraciones = (valoraciones != null) ? valoraciones : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ValoracionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_valoraciones, parent, false);
        return new ValoracionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ValoracionViewHolder holder, int position) {
        Valoracion valoracion = valoraciones.get(position);

        // Seteamos los datos
        holder.ratingBar.setRating((float) valoracion.getCalificacion());
        holder.tvDescripcion.setText(valoracion.getDescripcion());
        holder.tvIdUsuario.setText("Usuario #" + valoracion.getIdUsuario());

        // Lógica de clic consistente con tus otros adapters
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(valoracion);
            }
        });
    }

    @Override
    public int getItemCount() {
        return valoraciones != null ? valoraciones.size() : 0;
    }

    public static class ValoracionViewHolder extends RecyclerView.ViewHolder {
        RatingBar ratingBar;
        TextView tvDescripcion, tvIdUsuario;

        public ValoracionViewHolder(@NonNull View itemView) {
            super(itemView);
            ratingBar = itemView.findViewById(R.id.ratingItem);
            tvDescripcion = itemView.findViewById(R.id.tvTextoComentario);
            tvIdUsuario = itemView.findViewById(R.id.tvIdUsuarioComentario);
        }
    }

    public void setValoraciones(List<Valoracion> nuevasValoraciones) {
        this.valoraciones.clear();
        if (nuevasValoraciones != null) {
            this.valoraciones.addAll(nuevasValoraciones);
        }
        notifyDataSetChanged();
    }
}