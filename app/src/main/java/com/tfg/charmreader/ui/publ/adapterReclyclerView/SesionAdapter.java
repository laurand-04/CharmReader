package com.tfg.charmreader.ui.publ.adapterReclyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tfg.charmreader.R;
import com.tfg.charmreader.data.model.Sesion;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SesionAdapter extends RecyclerView.Adapter<SesionAdapter.ViewHolder> {

    private List<Sesion> sesiones;
    private OnSesionLongClickListener longClickListener;

    public interface OnSesionLongClickListener {
        void onSesionLongClick(Sesion sesion);
    }

    public SesionAdapter(List<Sesion> sesiones, OnSesionLongClickListener longClickListener) {
        this.sesiones = sesiones;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sesion, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Sesion s = sesiones.get(position);

        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.tvFecha.setText(fmt.format(s.getFecha()));
        holder.tvHora.setText(s.getHora() + "h");
        holder.tvCaps.setText("Caps: " + s.getCapituloInicio() + " - " + s.getCapituloFinalizacion());

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) longClickListener.onSesionLongClick(s);
            return true;
        });
    }

    @Override
    public int getItemCount() { return sesiones.size(); }

    public void updateData(List<Sesion> nuevasSesiones) {
        this.sesiones.clear();
        if (nuevasSesiones != null) {
            this.sesiones.addAll(nuevasSesiones);
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha, tvHora, tvCaps;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFechaSesion);
            tvHora = itemView.findViewById(R.id.tvHoraSesion);
            tvCaps = itemView.findViewById(R.id.tvCapsSesion);
        }
    }
}