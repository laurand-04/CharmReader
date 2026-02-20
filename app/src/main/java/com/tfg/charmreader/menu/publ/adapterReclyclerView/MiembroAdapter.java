package com.tfg.charmreader.menu.publ.adapterReclyclerView;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_ApiUsuario;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.Miembro;
import com.tfg.charmreader.objetosBD.Usuario;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MiembroAdapter extends RecyclerView.Adapter<MiembroAdapter.ViewHolder> {

    private List<Miembro> lista;
    private OnEliminarClickListener listener;
    private final I_ApiUsuario apiUsuario = API.getInstancia().create(I_ApiUsuario.class);

    public interface OnEliminarClickListener {
        void onEliminarConfirmado(int idUsuario);
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

        // Reset temporal mientras carga
        holder.tvNombre.setText("Cargando...");
        holder.ivFoto.setImageResource(R.drawable.ic_person);

        apiUsuario.obtenerUsuarioPorId(m.getIdUsuario()).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Usuario user = response.body();

                    // REGLA: Si tiene nombre lo ponemos, si no, "Usuario #ID"
                    String nombreAMostrar;
                    if (user.getNombre() != null && !user.getNombre().isEmpty()) {
                        nombreAMostrar = user.getNombre();
                    } else {
                        nombreAMostrar = "Usuario #" + user.getId();
                    }

                    holder.tvNombre.setText(nombreAMostrar);

                    // Carga de foto con Glide
                    if (user.getFoto() != null && !user.getFoto().isEmpty()) {
                        Glide.with(holder.itemView.getContext())
                                .load(user.getFoto())
                                .placeholder(R.drawable.ic_person)
                                .circleCrop()
                                .into(holder.ivFoto);
                    }

                    // Pasamos el objeto usuario completo y el nombre procesado al diálogo
                    holder.btnEliminar.setOnClickListener(v ->
                            mostrarDialogoConfirmacion(v.getContext(), user, nombreAMostrar));
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                String fallbackNombre = "Usuario #" + m.getIdUsuario();
                holder.tvNombre.setText(fallbackNombre);
                holder.btnEliminar.setOnClickListener(v -> mostrarDialogoConfirmacion(v.getContext(), null, fallbackNombre));
            }
        });
    }

    /**
     * Muestra el diálogo de confirmación con la info del usuario.
     */
    private void mostrarDialogoConfirmacion(android.content.Context context, Usuario usuario, String nombreAMostrar) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_eliminar_miembro);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // Vincular vistas del diálogo
        ImageView ivFoto = dialog.findViewById(R.id.ivFotoConfirmarEliminar);
        TextView tvNombre = dialog.findViewById(R.id.tvNombreConfirmarEliminar);
        MaterialButton btnEliminar = dialog.findViewById(R.id.btnConfirmarEliminar);
        MaterialButton btnCancelar = dialog.findViewById(R.id.btnCancelarEliminar);

        // Seteamos el nombre (ya procesado como "Nombre" o "Usuario #ID")
        tvNombre.setText(nombreAMostrar);

        // Cargamos la foto en el diálogo
        if (usuario != null && usuario.getFoto() != null && !usuario.getFoto().isEmpty()) {
            Glide.with(context)
                    .load(usuario.getFoto())
                    .placeholder(R.drawable.ic_person)
                    .circleCrop()
                    .into(ivFoto);
        } else {
            ivFoto.setImageResource(R.drawable.ic_person);
        }

        btnEliminar.setOnClickListener(v -> {
            // El ID lo sacamos del usuario o de la variable que ya tenemos
            int idFinal = (usuario != null) ? usuario.getId() : -1;
            if (idFinal != -1) {
                listener.onEliminarConfirmado(idFinal);
            }
            dialog.dismiss();
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public int getItemCount() { return lista != null ? lista.size() : 0; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        ImageView ivFoto;
        ImageButton btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreMiembro);
            ivFoto = itemView.findViewById(R.id.ivFotoMiembro);
            btnEliminar = itemView.findViewById(R.id.btnEliminarMiembro);
        }
    }
}