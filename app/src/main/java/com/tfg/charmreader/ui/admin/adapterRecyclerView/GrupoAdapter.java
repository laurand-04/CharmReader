package com.tfg.charmreader.ui.admin.adapterRecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tfg.charmreader.R;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiMiembro;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiValoracion;
import com.tfg.charmreader.data.network.API.API;
import com.tfg.charmreader.data.model.GrupoLectura;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GrupoAdapter extends RecyclerView.Adapter<GrupoAdapter.GrupoAdminViewHolder> {

    private List<GrupoLectura> grupos;
    private OnGrupoDeleteListener deleteListener;

    private final I_ApiMiembro apiMiembro = API.getInstancia().create(I_ApiMiembro.class);
    private final I_ApiValoracion apiValoracion = API.getInstancia().create(I_ApiValoracion.class);

    public interface OnGrupoDeleteListener {
        void onGrupoDelete(GrupoLectura grupo);
    }

    public GrupoAdapter(List<GrupoLectura> grupos, OnGrupoDeleteListener deleteListener) {
        this.grupos = (grupos != null) ? grupos : new ArrayList<>();
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public GrupoAdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grupo_admin, parent, false);
        return new GrupoAdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GrupoAdminViewHolder holder, int position) {
        GrupoLectura grupo = grupos.get(position);

        // 1. Datos básicos
        holder.tvNombre.setText(grupo.getNombre());
        holder.tvDescripcion.setText(grupo.getDescripcion());
        holder.tvUbicacion.setText(grupo.getUbicacion());
        holder.tvFrecuencia.setText(grupo.getFrecuenciaReunion().toString());
        holder.tvAdminId.setText("Admin ID: " + grupo.getIdUsuario());

        // 2. Cargar Avatar con Glide
        if (grupo.getUrl() != null && !grupo.getUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(grupo.getUrl())
                    .placeholder(R.drawable.ic_people)
                    .error(R.drawable.ic_people)
                    .centerCrop()
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_people);
        }

        // 3. Formatear Fecha de Creación
        if (grupo.getFechaCreacion() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.tvFecha.setText("Creado: " + sdf.format(grupo.getFechaCreacion()));
        }

        // 4. Cargar Conteo de Miembros
        apiMiembro.contarMiembros(grupo.getIdGrupo()).enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful() && response.body() != null) {
                    holder.tvMiembros.setText(response.body() + " miembros");
                }
            }
            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                holder.tvMiembros.setText("0 miembros");
            }
        });

        // 5. Cargar Valoración Media
        apiValoracion.obtenerMediaGrupo(grupo.getIdGrupo()).enqueue(new Callback<Double>() {
            @Override
            public void onResponse(Call<Double> call, Response<Double> response) {
                if (response.isSuccessful() && response.body() != null) {
                    holder.tvValoracion.setText(String.format(Locale.getDefault(), "%.1f", response.body()));
                } else {
                    holder.tvValoracion.setText("0.0");
                }
            }
            @Override
            public void onFailure(Call<Double> call, Throwable t) {
                holder.tvValoracion.setText("-.-");
            }
        });

        // 6. Botón de borrado con diálogo moderno
        holder.btnBorrar.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(v.getContext())
                    .setTitle("Eliminar Grupo")
                    .setMessage("¿Estás seguro de que deseas eliminar '" + grupo.getNombre() + "'? Se borrará toda la actividad vinculada.")
                    .setNegativeButton("CANCELAR", null)
                    .setPositiveButton("ELIMINAR", (dialog, which) -> {
                        if (deleteListener != null) {
                            deleteListener.onGrupoDelete(grupo);
                        }
                    })
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return grupos.size();
    }

    public static class GrupoAdminViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDescripcion, tvUbicacion, tvFrecuencia, tvMiembros, tvValoracion, tvAdminId, tvFecha;
        ImageView ivAvatar;
        MaterialButton btnBorrar;

        public GrupoAdminViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatarGrupoAdmin);
            tvNombre = itemView.findViewById(R.id.tvNombreGrupoAdmin);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionAdmin);
            tvUbicacion = itemView.findViewById(R.id.tvUbicacionAdmin);
            tvFrecuencia = itemView.findViewById(R.id.tvFrecuenciaBadgeAdmin);
            tvMiembros = itemView.findViewById(R.id.tvContadorMiembrosAdmin);
            tvValoracion = itemView.findViewById(R.id.tvValoracionMediaAdmin);
            tvAdminId = itemView.findViewById(R.id.tvAdminIdInfo);
            tvFecha = itemView.findViewById(R.id.tvFechaCreacionAdmin);
            btnBorrar = itemView.findViewById(R.id.btnBorrarGrupoAdmin);
        }
    }
}