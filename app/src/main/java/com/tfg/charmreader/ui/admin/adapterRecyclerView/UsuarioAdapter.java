package com.tfg.charmreader.ui.admin.adapterRecyclerView;

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
import com.tfg.charmreader.data.model.Usuario;
import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    private List<Usuario> listaUsuarios;
    private OnUsuarioDeleteListener deleteListener;

    public interface OnUsuarioDeleteListener {
        void onUsuarioDelete(Usuario usuario);
    }

    public UsuarioAdapter(List<Usuario> listaUsuarios, OnUsuarioDeleteListener deleteListener) {
        this.listaUsuarios = listaUsuarios;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usuario_admin, parent, false);
        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario usuario = listaUsuarios.get(position);

        // REGLA: Nombre o Usuario #ID
        String nombreAMostrar = (usuario.getNombre() != null && !usuario.getNombre().isEmpty())
                ? usuario.getNombre() : "Usuario #" + usuario.getId();

        holder.tvNombre.setText(nombreAMostrar);
        holder.tvEmail.setText(usuario.getCorreo());

        // Carga de foto con Glide
        Glide.with(holder.itemView.getContext())
                .load(usuario.getFoto())
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(holder.ivFoto);

        holder.btnBorrar.setOnClickListener(v -> mostrarDialogoConfirmacion(v.getContext(), usuario, nombreAMostrar));
    }

    private void mostrarDialogoConfirmacion(android.content.Context context, Usuario usuario, String nombreAMostrar) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Asegúrate de que el nombre del archivo XML sea exactamente este:
        dialog.setContentView(R.layout.dialog_eliminar_miembro);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // Estos IDs pertenecen al XML del DIÁLOGO flotante
        ImageView ivFoto = dialog.findViewById(R.id.ivFotoConfirmarEliminar);
        TextView tvNombre = dialog.findViewById(R.id.tvNombreConfirmarEliminar);
        MaterialButton btnEliminar = dialog.findViewById(R.id.btnConfirmarEliminar);
        MaterialButton btnCancelar = dialog.findViewById(R.id.btnCancelarEliminar);

        tvNombre.setText(nombreAMostrar);
        btnEliminar.setText("ELIMINAR USUARIO");

        Glide.with(context)
                .load(usuario.getFoto())
                .placeholder(R.drawable.ic_person)
                .circleCrop()
                .into(ivFoto);

        btnEliminar.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onUsuarioDelete(usuario);
            dialog.dismiss();
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return listaUsuarios != null ? listaUsuarios.size() : 0;
    }

    public static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvEmail;
        ImageView ivFoto; // Esta es la foto de la lista
        ImageButton btnBorrar;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            // Estos IDs pertenecen al XML del ITEM de la lista
            tvNombre = itemView.findViewById(R.id.tvNombreUsuarioAdmin);
            tvEmail = itemView.findViewById(R.id.tvEmailUsuarioAdmin);
            ivFoto = itemView.findViewById(R.id.ivFotoUsuarioAdmin); // ¡AHORA SÍ COINCIDE!
            btnBorrar = itemView.findViewById(R.id.btnEliminarUsuario);
        }
    }
}