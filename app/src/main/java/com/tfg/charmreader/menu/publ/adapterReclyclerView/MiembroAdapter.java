package com.tfg.charmreader.menu.publ.adapterReclyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tfg.charmreader.R;
import com.tfg.charmreader.objetosBD.Miembro;
import java.util.List;

public class MiembroAdapter extends RecyclerView.Adapter<MiembroAdapter.ViewHolder> {

    private List<Miembro> lista;
    private OnEliminarClickListener listener;

    public interface OnEliminarClickListener {
        void onEliminarClick(int idUsuario);
    }

    public MiembroAdapter(List<Miembro> lista, OnEliminarClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_miembro_gestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Miembro m = lista.get(position);

        // Si tu objeto Miembro tiene nombre:
        holder.tvNombre.setText("Usuario ID: " + m.getIdUsuario());

        holder.btnEliminar.setOnClickListener(v -> listener.onEliminarClick(m.getIdUsuario()));
    }

    @Override
    public int getItemCount() { return lista.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        ImageButton btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreMiembro);
            btnEliminar = itemView.findViewById(R.id.btnEliminarMiembro);
        }
    }
}