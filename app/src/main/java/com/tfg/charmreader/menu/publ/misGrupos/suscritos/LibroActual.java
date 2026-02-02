package com.tfg.charmreader.menu.publ.misGrupos.suscritos;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.tfg.charmreader.R;
import com.tfg.charmreader.objetosBD.BookEn;

public class LibroActual extends AppCompatActivity {

    private ImageView ivPortada;
    private TextView tvTitulo, tvSubtitulo, tvAutor, tvAnio, tvTema, tvResumen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_libro_actual);

        // Inicializar vistas
        ivPortada = findViewById(R.id.ivDetallePortada);
        tvTitulo = findViewById(R.id.tvDetalleTitulo);
        tvSubtitulo = findViewById(R.id.tvDetalleSubtitulo);
        tvAutor = findViewById(R.id.tvDetalleAutor);
        tvAnio = findViewById(R.id.tvDetalleAnio);
        tvTema = findViewById(R.id.tvDetalleTema);
        tvResumen = findViewById(R.id.tvDetalleResumen);

        // Recuperar el objeto BookEn pasado por el Intent
        BookEn libro = (BookEn) getIntent().getSerializableExtra("libroSeleccionado");

        if (libro != null) {
            tvTitulo.setText(libro.getTitulo());
            tvSubtitulo.setText(libro.getSubtitulo());
            tvAutor.setText(libro.getAutor());
            tvAnio.setText(String.valueOf(libro.getFechaPublicacion()));
            tvTema.setText(libro.getTema());
            tvResumen.setText(libro.getResumen());

            // CARGA DE IMAGEN CON URL DE OPEN LIBRARY
            String idPortada = libro.getCoverId();
            if (idPortada != null && !idPortada.isEmpty() && !idPortada.equals("null")) {
                // Usamos "-L" para que en esta pantalla de detalle la imagen tenga calidad
                String urlImagen = "https://covers.openlibrary.org/b/id/" + idPortada + "-L.jpg";

                Glide.with(this)
                        .load(urlImagen)
                        .placeholder(R.drawable.ic_libro)
                        .error(android.R.drawable.stat_notify_error)
                        .into(ivPortada);
            } else {
                ivPortada.setImageResource(R.drawable.ic_libro);
            }
        }
    }
}