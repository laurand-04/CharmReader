package com.tfg.charmreader.ui.publ.misGrupos.suscritos;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.tfg.charmreader.R;
import com.tfg.charmreader.data.model.BookEn;
import com.tfg.charmreader.databinding.ActivityLibroActualBinding;
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
        }

        // Botón atrás opcional (si decides añadirlo al XML o Toolbar)
        // binding.btnBack.setOnClickListener(v -> finish());
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
    }
}