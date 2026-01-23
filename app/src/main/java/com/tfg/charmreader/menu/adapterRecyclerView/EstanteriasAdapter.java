package com.tfg.charmreader.menu.adapterRecyclerView;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tfg.charmreader.R;
import com.tfg.charmreader.menu.estanteria.LibrosEstanteria;
import com.tfg.charmreader.objetosBD.Estanteria;
import java.util.ArrayList;
import java.util.List;

public class EstanteriasAdapter extends RecyclerView.Adapter<EstanteriasAdapter.EstanteriaViewHolder> {

    private List<Estanteria> estanterias;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Estanteria estanteria);
    }

    public EstanteriasAdapter(List<Estanteria> estanterias, OnItemClickListener listener) {
        // Aseguramos que nunca sea null al iniciar
        this.estanterias = (estanterias != null) ? estanterias : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public EstanteriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_estanteria, parent, false);
        return new EstanteriaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EstanteriaViewHolder holder, int position) {
        Estanteria estanteria = estanterias.get(position);
        holder.tvTitulo.setText(estanteria.getNombre());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(estanteria);
            }
            /*Intent intent = new Intent(v.getContext(), LibrosEstanteria.class);
            // Pasamos el ID o nombre para saber qué libros cargar
            intent.putExtra("idEstanteria", estanteria.getId());
            intent.putExtra("nombreEstanteria", estanteria.getNombre());
            v.getContext().startActivity(intent);*/
        });

    }

    @Override
    public int getItemCount() {
        return estanterias != null ? estanterias.size() : 0;
    }

    // MÉTODO CORREGIDO: Reemplaza la lista completa
    public void setEstanterias(List<Estanteria> nuevasEstanterias) {
        this.estanterias = nuevasEstanterias;
        notifyDataSetChanged();
    }

    public static class EstanteriaViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo;

        public EstanteriaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvNombreEstanteria);
        }
    }
}