package com.tfg.charmreader.admin.adapterRecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tfg.charmreader.R;
import com.tfg.charmreader.objetosBD.Usuario;
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

        // Ajustamos los textos según los datos del objeto Usuario
        // Si el usuario no tiene nombre en la BD, podrías poner el email arriba
        holder.tvNombre.setText("Usuario ID: " + usuario.getId());
        holder.tvEmail.setText(usuario.getCorreo());

        holder.btnBorrar.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(v.getContext())
                    .setTitle("Eliminar Usuario")
                    .setMessage("¿Estás seguro de que deseas eliminar a " + usuario.getCorreo() + "?")
                    .setNegativeButton("CANCELAR", null)
                    .setPositiveButton("ELIMINAR", (dialog, which) -> {
                        if (deleteListener != null) {
                            deleteListener.onUsuarioDelete(usuario);
                        }
                    })
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return listaUsuarios != null ? listaUsuarios.size() : 0;
    }

    public static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvEmail; // Variables renombradas para coincidir
        ImageButton btnBorrar;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            // Sincronización de IDs con el XML
            tvNombre = itemView.findViewById(R.id.tvNombreUsuarioAdmin);
            tvEmail = itemView.findViewById(R.id.tvEmailUsuarioAdmin);
            btnBorrar = itemView.findViewById(R.id.btnEliminarUsuario);
        }
    }
}