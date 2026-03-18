package com.tfg.charmreader.ui.publ.misGrupos.suscritos;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tfg.charmreader.R;
import com.tfg.charmreader.data.model.BookEn;
import com.tfg.charmreader.data.model.GrupoLectura;
import com.tfg.charmreader.databinding.ActivityLibroActualBinding;
import com.tfg.charmreader.ui.priv.tusLibros.VisorActivity;
import com.tfg.charmreader.viewmodel.publ.misGrupos.suscritos.LibroActualViewModel;

public class LibroActualActivity extends AppCompatActivity {

    private ActivityLibroActualBinding binding;
    private LibroActualViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLibroActualBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LibroActualViewModel.class);

        setupObservers();

        // Recuperar datos
        if (savedInstanceState == null) {
            BookEn libro = (BookEn) getIntent().getSerializableExtra("libroSeleccionado");
            viewModel.setLibro(libro);

            boolean mostrarIA = getIntent().getBooleanExtra("esIA", false);
            Log.d("DEBUG_FLOW", "mostrarIA = " + mostrarIA);

            if (mostrarIA && libro != null) {
                // Modo IA: Ocultamos el botón de lectura y mostramos el layout de IA
                binding.btnLeerEpub.setVisibility(android.view.View.GONE);
                binding.layoutIA.setVisibility(android.view.View.VISIBLE);

                viewModel.cargarComentariosYGenerarResumen(libro.getTitulo());
            }
        }

        // Botón atrás opcional (si decides añadirlo al XML o Toolbar)
        // binding.btnBack.setOnClickListener(v -> finish());

        binding.btnLeerEpub.setOnClickListener(v -> {
            BookEn libroL = viewModel.getLibro().getValue();
            if (libroL != null) {
                if (libroL.getUrlLibro() != null) {
                    viewModel.descargarLibro(libroL);
                } else {
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("Libro no disponible todavía")
                            .setMessage("El coordinador del grupo aún no ha subido el archivo del libro.\n\n" +
                                    "Cuando esté disponible podrás descargarlo desde esta misma pantalla.\n\n" +
                                    "Por ahora tendrás que esperar a que se publique.")
                            .setPositiveButton("Entendido", (dialog, which) -> dialog.dismiss())
                            .show();

                }
            }
        });
    }

    private void setupObservers() {
        viewModel.getLibro().observe(this, libro -> {
            if (libro == null) return;

            binding.tvDetalleTitulo.setText(libro.getTitulo());
            binding.tvDetalleSubtitulo.setText(libro.getSubtitulo());
            binding.tvDetalleAutor.setText(libro.getAutor());
            binding.tvDetalleAnio.setText(String.valueOf(libro.getFechaPublicacion()));

            // Mapeo del tema (usando el toString del enum o valor por defecto)
            String temaStr = libro.getTema() != null ? libro.getTema().toString() : "General";
            binding.tvDetalleTema.setText(temaStr);

            binding.tvDetalleResumen.setText(libro.getResumen());
        });

        viewModel.getCoverUrl().observe(this, url -> {
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_libro)
                    .error(R.drawable.ic_libro)
                    .centerCrop()
                    .into(binding.ivDetallePortada);
        });

        viewModel.getResumenIA().observe(this, resumen -> {
            if (resumen != null) {
                // Si no quieres complicarte, usa esto:
                binding.tvComentarioIA.setText(resumen);

                // Pero si quieres que "aparezca" suavemente:
                binding.tvComentarioIA.setTranslationY(20f);
                binding.tvComentarioIA.animate()
                        .translationY(0f)
                        .alpha(1f)
                        .setDuration(500)
                        .start();
            }
        });

        // Observar la nota media calculada
        viewModel.getMediaValoracion().observe(this, media -> {
            binding.tvValoracionMedia.setText(media);
        });

        // Opcional: Observar el estado de carga para feedback visual
        viewModel.getCargandoIA().observe(this, cargando -> {
            if (cargando != null && cargando) {
                // MODO CARGANDO
                binding.pbCargandoIA.setVisibility(View.VISIBLE);
                binding.tvComentarioIA.setText("Gemini está analizando las reseñas...");
                binding.tvComentarioIA.setAlpha(0.6f);
                binding.tvValoracionMedia.setAlpha(0.3f);
            } else {
                // MODO LISTO
                binding.pbCargandoIA.setVisibility(View.GONE);
                binding.tvComentarioIA.setAlpha(1.0f);
                binding.tvValoracionMedia.setAlpha(1.0f);
            }
        });

        viewModel.getMensaje().observe(this, msg -> {
            if (msg == "Iniciando descarga..."){
                Intent intent = new Intent(this, VisorActivity.class);
                intent.putExtra("Libro", viewModel.getLibro().getValue());
                intent.putExtra("grupoB", true);
                GrupoLectura grupo = (GrupoLectura) getIntent().getSerializableExtra("grupo");
                intent.putExtra("grupoO", grupo);
                startActivity(intent);
            }else{
                Log.e("LibroActualActivity", "Error al descargarLibro");
            }
        });
    }
}