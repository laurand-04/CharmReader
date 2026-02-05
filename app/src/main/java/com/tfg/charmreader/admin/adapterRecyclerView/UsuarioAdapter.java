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

    // Interfaz para comunicar el clic de borrado con la Actividad
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

        holder.tvEmail.setText(usuario.getCorreo());
        holder.tvId.setText("ID Sistema: " + usuario.getId());

        holder.btnBorrar.setOnClickListener(v -> {
            // Diálogo de confirmación antes de llamar al listener
            new MaterialAlertDialogBuilder(v.getContext())
                    .setTitle("Eliminar Usuario")
                    .setMessage("¿Estás seguro de que deseas eliminar permanentemente a " + usuario.getCorreo() + "?")
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
        TextView tvEmail, tvId;
        ImageButton btnBorrar;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmail = itemView.findViewById(R.id.tvEmailAdmin);
            tvId = itemView.findViewById(R.id.tvIdAdmin);
            btnBorrar = itemView.findViewById(R.id.btnBorrarUsuario);
        }
    }
}