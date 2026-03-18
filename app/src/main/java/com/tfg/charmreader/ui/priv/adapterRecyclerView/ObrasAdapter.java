package com.tfg.charmreader.ui.priv.adapterRecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tfg.charmreader.R;
import com.tfg.charmreader.data.model.ObrasModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ObrasAdapter extends RecyclerView.Adapter<ObrasAdapter.ObraViewHolder> {

    private List<ObrasModel> obrasMostradas;
    private List<ObrasModel> todasLasObras;

    private String queryActual = "";
    private String estadoActual = "TODOS";
    private boolean ordenarPorRecientes = false;

    private OnItemClickListener listener;
    private OnItemLongClickListener longListener;

    // Formateador para mostrar la fecha de forma legible
    private final SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface OnItemClickListener {
        void onItemClick(ObrasModel obra);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(ObrasModel obra);
    }

    public ObrasAdapter(List<ObrasModel> obras, OnItemClickListener listener) {
        this.todasLasObras = (obras != null) ? new ArrayList<>(obras) : new ArrayList<>();
        this.obrasMostradas = new ArrayList<>(this.todasLasObras);
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longListener) {
        this.longListener = longListener;
    }

    @NonNull
    @Override
    public ObraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_obra, parent, false);
        return new ObraViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ObraViewHolder holder, int position) {
        if (obrasMostradas.isEmpty()) return;

        ObrasModel obra = obrasMostradas.get(position);

        // 1. Título
        holder.tvTitulo.setText(obra.getNombre() != null ? obra.getNombre() : "Sin título");

        // 2. Fecha de modificación
        if (obra.getFecha_ultima_modificacion() != null) {
            String fechaStr = formatoFecha.format(obra.getFecha_ultima_modificacion());
            holder.tvFecha.setText("Modificado el " + fechaStr);
        } else {
            holder.tvFecha.setText("Sin modificaciones");
        }

        // 3. Manejo del Badge "Finalizado"
        if (obra.getFinalizado()) {
            holder.layoutBadgeFinalizado.setVisibility(View.VISIBLE);
        } else {
            holder.layoutBadgeFinalizado.setVisibility(View.GONE);
        }

        // 4. Carga de imagen con Glide
        Glide.with(holder.itemView.getContext())
                .load(obra.getUrl_imagen())
                .placeholder(R.drawable.ic_libro) // Puedes cambiarlo por un placeholder de escritura
                .error(R.drawable.ic_libro)
                .into(holder.ivPortada);

        // 5. Listeners de clicks
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(obra);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longListener != null) {
                longListener.onItemLongClick(obra);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return obrasMostradas.size();
    }

    public void setData(List<ObrasModel> nuevasObras) {
        this.todasLasObras.clear();
        this.todasLasObras.addAll(nuevasObras != null ? nuevasObras : new ArrayList<>());
        filtrar(""); // Forzamos el refiltrado al recibir nuevos datos
    }

    /**
     * Filtro por texto (Buscador)
     */
    public void filtrar(String texto) {
        this.queryActual = (texto == null) ? "" : texto.toLowerCase().trim();
        aplicarFiltrosCombinados();
    }

    /**
     * Filtro por estado (Chips: TODOS, SIN_FINALIZAR, FINALIZADOS)
     */
    public void filtrarPorEstado(String estado) {
        this.estadoActual = estado;
        aplicarFiltrosCombinados();
    }

    /**
     * Activa o desactiva la ordenación por recientes
     */
    public void setOrdenarPorRecientes(boolean activo) {
        this.ordenarPorRecientes = activo;
        aplicarFiltrosCombinados();
    }

    /**
     * Lógica central de filtrado y ordenación
     */
    private void aplicarFiltrosCombinados() {
        obrasMostradas.clear();

        for (ObrasModel obra : todasLasObras) {
            // 1. Validar Texto (Nombre de la obra)
            boolean coincideTexto = queryActual.isEmpty() ||
                    (obra.getNombre() != null && obra.getNombre().toLowerCase().contains(queryActual));

            // 2. Validar Estado
            boolean coincideEstado = true;
            if (!estadoActual.equals("TODOS")) {
                boolean estaFinalizado = obra.getFinalizado();

                switch (estadoActual) {
                    case "SIN_FINALIZAR":
                        coincideEstado = !estaFinalizado;
                        break;
                    case "FINALIZADOS":
                        coincideEstado = estaFinalizado;
                        break;
                }
            }

            // Si pasa todos los filtros, lo añadimos
            if (coincideTexto && coincideEstado) {
                obrasMostradas.add(obra);
            }
        }

        // 3. Aplicar ordenación si "Recientes" está activado
        if (ordenarPorRecientes) {
            Collections.sort(obrasMostradas, (o1, o2) -> {
                // Si alguna fecha es nula, la mandamos al final
                if (o1.getFecha_ultima_modificacion() == null) return 1;
                if (o2.getFecha_ultima_modificacion() == null) return -1;

                // Orden descendente (más recientes primero)
                return o2.getFecha_ultima_modificacion().compareTo(o1.getFecha_ultima_modificacion());
            });
        }

        notifyDataSetChanged();
    }

    public static class ObraViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvFecha;
        ImageView ivPortada;
        LinearLayout layoutBadgeFinalizado;

        public ObraViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloObra);
            tvFecha = itemView.findViewById(R.id.tvFechaModificacionObra);
            ivPortada = itemView.findViewById(R.id.ivPortadaObra);
            layoutBadgeFinalizado = itemView.findViewById(R.id.layoutBadgeFinalizadoObra);
        }
    }
}