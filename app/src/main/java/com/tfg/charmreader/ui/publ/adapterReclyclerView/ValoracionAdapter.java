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
import com.tfg.charmreader.data.model.Usuario;
import com.tfg.charmreader.data.model.Valoracion;
import com.tfg.charmreader.data.repository.publ.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ValoracionAdapter extends RecyclerView.Adapter<ValoracionAdapter.ValoracionViewHolder> {

    private List<Valoracion> valoraciones;
    private OnItemClickListener listener;
    private final UserRepository userRepository = new UserRepository();

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

        // 1. Datos básicos de la valoración
        holder.tvDescripcion.setText(valoracion.getDescripcion());
        holder.tvNotaNumerica.setText(String.format(Locale.getDefault(), "%.1f", (float) valoracion.getCalificacion()));

        // 2. Valores por defecto mientras carga la API
        holder.tvIdUsuario.setText("Usuario #" + valoracion.getIdUsuario());
        holder.ivAvatar.setImageResource(R.drawable.ic_person);

        // 3. Obtener nombre y foto real del autor
        userRepository.obtenerUsuarioPorId(valoracion.getIdUsuario(), new Callback<Usuario>(){
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Usuario u = response.body();

                    // Regla unificada de nombre
                    if (u.getNombre() != null && !u.getNombre().isEmpty()) {
                        holder.tvIdUsuario.setText(u.getNombre());
                    } else {
                        holder.tvIdUsuario.setText("Usuario #" + u.getId());
                    }

                    // Carga de avatar con Glide
                    if (u.getFoto() != null && !u.getFoto().isEmpty()) {
                        Glide.with(holder.itemView.getContext())
                                .load(u.getFoto())
                                .placeholder(R.drawable.ic_person)
                                .circleCrop()
                                .into(holder.ivAvatar);
                    }
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                // Si falla, se queda con "Usuario #ID" puesto arriba
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(valoracion);
        });
    }

    @Override
    public int getItemCount() {
        return valoraciones.size();
    }

    public static class ValoracionViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescripcion, tvIdUsuario, tvNotaNumerica;
        ImageView ivAvatar;

        public ValoracionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescripcion = itemView.findViewById(R.id.tvTextoComentario);
            tvIdUsuario = itemView.findViewById(R.id.tvIdUsuarioComentario);
            tvNotaNumerica = itemView.findViewById(R.id.tvNotaNumerica);
            ivAvatar = itemView.findViewById(R.id.ivAvatarUsuario);
        }
    }



    public void updateData(List<Valoracion> nuevasValoraciones) {
        this.valoraciones.clear();
        if (nuevasValoraciones != null) {
            this.valoraciones.addAll(nuevasValoraciones);
        }
        notifyDataSetChanged();
    }
}