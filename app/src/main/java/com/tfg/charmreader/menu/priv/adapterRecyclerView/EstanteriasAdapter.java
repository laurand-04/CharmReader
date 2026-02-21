package com.tfg.charmreader.menu.priv.adapterRecyclerView;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tfg.charmreader.R;
import com.tfg.charmreader.objetosBD.Estanteria;
import java.util.ArrayList;
import java.util.List;

public class EstanteriasAdapter extends RecyclerView.Adapter<EstanteriasAdapter.EstanteriaViewHolder> {

    private List<Estanteria> estanterias;
    private List<Estanteria> listaOriginal;
    private OnItemClickListener listener;
    private OnItemLongClickListener longListener; // 🔥 Listener para eliminación

    public interface OnItemClickListener {
        void onItemClick(Estanteria estanteria);
    }

    // 🔥 Interfaz para borrar
    public interface OnItemLongClickListener {
        void onItemLongClick(Estanteria estanteria);
    }

    public EstanteriasAdapter(List<Estanteria> estanterias, OnItemClickListener listener) {
        this.estanterias = (estanterias != null) ? estanterias : new ArrayList<>();
        this.listaOriginal = new ArrayList<>(this.estanterias);
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longListener) {
        this.longListener = longListener;
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

        if (estanteria.getColor() != null && !estanteria.getColor().isEmpty()) {
            try {
                holder.ivIcono.getBackground().setColorFilter(
                        Color.parseColor(estanteria.getColor()),
                        PorterDuff.Mode.SRC_IN
                );
            } catch (Exception e) {
                holder.ivIcono.getBackground().setColorFilter(0xFFF3E5F5, PorterDuff.Mode.SRC_IN);
            }
        }

        int num = estanteria.getCantidadLibros();
        holder.tvCantidad.setText(num == 1 ? "1 libro" : num + " libros");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(estanteria);
        });

        // 🔥 Evento de clic largo para eliminar
        holder.itemView.setOnLongClickListener(v -> {
            if (longListener != null) {
                longListener.onItemLongClick(estanteria);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return estanterias.size();
    }

    public void setEstanterias(List<Estanteria> nuevasEstanterias) {
        this.estanterias.clear();
        if (nuevasEstanterias != null) {
            this.estanterias.addAll(nuevasEstanterias);
            this.listaOriginal = new ArrayList<>(nuevasEstanterias);
        }
        notifyDataSetChanged();
    }

    public void filtrar(String texto) {
        if (texto == null || texto.isEmpty()) {
            estanterias.clear();
            estanterias.addAll(listaOriginal);
        } else {
            List<Estanteria> filtrados = new ArrayList<>();
            String query = texto.toLowerCase().trim();
            for (Estanteria e : listaOriginal) {
                if (e.getNombre().toLowerCase().contains(query)) {
                    filtrados.add(e);
                }
            }
            estanterias.clear();
            estanterias.addAll(filtrados);
        }
        notifyDataSetChanged();
    }

    public static class EstanteriaViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvCantidad;
        ImageView ivIcono;

        public EstanteriaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvNombreEstanteria);
            tvCantidad = itemView.findViewById(R.id.tvCantidadLibros);
            ivIcono = itemView.findViewById(R.id.ivIconoEstanteria);
        }
    }
}