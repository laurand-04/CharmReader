package com.tfg.charmreader.ui.publ.adapterReclyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tfg.charmreader.R;
import com.tfg.charmreader.data.model.Usuario;
import java.util.ArrayList;
import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    private List<Usuario> usuarios;
    private List<Usuario> listaOriginal;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Usuario usuario);
    }

    public UsuarioAdapter(List<Usuario> usuarios, OnItemClickListener listener) {
        this.usuarios = (usuarios != null) ? usuarios : new ArrayList<>();
        this.listaOriginal = new ArrayList<>(this.usuarios);
        this.listener = listener;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario usuario = usuarios.get(position);

        holder.tvNombre.setText(usuario.getNombre());

        // Lógica de Privacidad
        // Cambia .isPublico() por el métod real de tu clase Usuario (ej: getPublico() == 1)
        if (usuario.getPublico()) {
            holder.tvPrivacidad.setText("Usuario público");
            holder.ivFlecha.setVisibility(View.VISIBLE);

            // Si es público, habilitamos el click
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(usuario);
            });
            holder.itemView.setAlpha(1.0f);
        } else {
            holder.tvPrivacidad.setText("Usuario privado");
            holder.ivFlecha.setVisibility(View.GONE);

            // Si es privado, deshabilitamos el click
            holder.itemView.setOnClickListener(null);
            // Opcional: bajar un poco la opacidad para indicar que no es interactivo
            holder.itemView.setAlpha(0.8f);
        }

        // Carga de imagen
        if (usuario.getFoto() != null && !usuario.getFoto().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(usuario.getFoto())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .circleCrop()
                    .into(holder.ivFoto);
        } else {
            holder.ivFoto.setImageResource(R.drawable.ic_person);
        }
    }

    @Override
    public int getItemCount() { return usuarios != null ? usuarios.size() : 0; }

    public void setUsuarios(List<Usuario> nuevosUsuarios) {
        this.usuarios.clear();
        if (nuevosUsuarios != null) {
            this.usuarios.addAll(nuevosUsuarios);
            this.listaOriginal = new ArrayList<>(nuevosUsuarios);
        }
        notifyDataSetChanged();
    }

    public void filtrar(String texto) {
        if (texto == null || texto.isEmpty()) {
            usuarios.clear();
            usuarios.addAll(listaOriginal);
        } else {
            List<Usuario> filtrados = new ArrayList<>();
            String query = texto.toLowerCase().trim();
            for (Usuario u : listaOriginal) {
                if (u.getNombre().toLowerCase().contains(query)) {
                    filtrados.add(u);
                }
            }
            usuarios.clear();
            usuarios.addAll(filtrados);
        }
        notifyDataSetChanged();
    }

    public static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvPrivacidad;
        ImageView ivFoto, ivFlecha;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreUsuario);
            tvPrivacidad = itemView.findViewById(R.id.tvPrivacidad);
            ivFoto = itemView.findViewById(R.id.ivUsuarioFoto);
            ivFlecha = itemView.findViewById(R.id.ivFlecha);
        }
    }
}