package com.tfg.charmreader.menu.publ.misGrupos.creados; // Asegúrate de que el package sea correcto

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.tfg.charmreader.R;
import com.tfg.charmreader.objetosBD.GrupoLectura;

public class ManejoGrupo extends AppCompatActivity {

    private TextView tvNombre, tvId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manejo_grupo);

        tvNombre = findViewById(R.id.tvNombreGestion);
        tvId = findViewById(R.id.tvIdGestion);

        // Recuperamos el objeto serializable
        GrupoLectura grupo = (GrupoLectura) getIntent().getSerializableExtra("objetoGrupo");

        if (grupo != null) {
            tvNombre.setText(grupo.getNombre());
            tvId.setText("Gestión para ID: " + grupo.getIdGrupo());

            // Un pequeño aviso para confirmar visualmente
            Toast.makeText(this, "Entrando a gestión de: " + grupo.getNombre(), Toast.LENGTH_SHORT).show();
        } else {
            tvNombre.setText("Error al cargar datos");
        }
    }
}