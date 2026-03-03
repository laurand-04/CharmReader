package com.tfg.charmreader.ui.priv.proximamente;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.tfg.charmreader.R;
import com.tfg.charmreader.data.model.BookEn;
import com.tfg.charmreader.databinding.ActivityProximoLibroBinding;
import com.tfg.charmreader.viewmodel.priv.proximamente.ProximoLibroViewModel;

public class ProximoLibroActivity extends AppCompatActivity {

    private ActivityProximoLibroBinding binding;
    private ProximoLibroViewModel viewModel;
    private BookEn libroActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProximoLibroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configurarStatusBar();

        int idLibro = getIntent().getIntExtra("idLibro", -1);
        viewModel = new ViewModelProvider(this).get(ProximoLibroViewModel.class);

        setupObservers();
        setupListeners();
        configurarDropdownTemas();

        if (idLibro != -1) viewModel.cargarLibro(idLibro);
    }

    private void setupObservers() {
        viewModel.getLibro().observe(this, libro -> {
            this.libroActual = libro;
            actualizarUI(libro);
        });

        viewModel.getIsLoading().observe(this, loading -> {
            binding.layoutLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.getIsSaving().observe(this, saving -> {
            binding.layoutLoading.setVisibility(saving ? View.VISIBLE : View.GONE);
            binding.layoutContenido.setAlpha(saving ? 0.4f : 1.0f);
        });

        viewModel.getMensaje().observe(this, msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());

        viewModel.getSuccessAction().observe(this, success -> {
            if (success) {
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void actualizarUI(BookEn libro) {
        binding.tvDetalleTituloDisplay.setText(libro.getTitulo());
        binding.tvDetalleAutorDisplay.setText(libro.getAutor());
        binding.etDetalleSubtitulo.setText(libro.getSubtitulo());
        binding.etDetalleResumen.setText(libro.getResumen());

        if (libro.getTema() != null) {
            binding.etDetalleTema.setText(libro.getTema().toString(), false);
        }

        binding.btnInfoExterna.setVisibility((libro.getInfoUrl() == null || libro.getInfoUrl().isEmpty())
                ? View.GONE : View.VISIBLE);

        String urlImagen = "https://covers.openlibrary.org/b/id/" + libro.getCoverId() + "-M.jpg";
        Glide.with(this).load(urlImagen).centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery).into(binding.ivDetallePortada);
    }

    private void setupListeners() {
        binding.btnBackDetalle.setOnClickListener(v -> finish());

        binding.btnInfoExterna.setOnClickListener(v -> {
            if (libroActual != null && libroActual.getInfoUrl() != null) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(libroActual.getInfoUrl())));
            }
        });

        binding.btnActualizarLibro.setOnClickListener(v -> {
            if (libroActual != null) {
                viewModel.actualizarLibro(libroActual,
                        binding.etDetalleSubtitulo.getText().toString().trim(),
                        binding.etDetalleResumen.getText().toString().trim(),
                        binding.etDetalleTema.getText().toString());
            }
        });
    }

    private void configurarDropdownTemas() {
        String[] nombresTemas = new String[ProximoLibroActivity.TemaLibro.values().length];
        for (int i = 0; i < ProximoLibroActivity.TemaLibro.values().length; i++) {
            nombresTemas[i] = ProximoLibroActivity.TemaLibro.values()[i].toString();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, nombresTemas);
        binding.etDetalleTema.setAdapter(adapter);
    }

    private void configurarStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    public enum TemaLibro {
        AVENTURAS, CIENCIA_FICCION, DRAMA, FANTASIA, HISTORICA, HUMOR,
        POLICIACA, ROMANCE, SUSPENSE, TERROR, INFANTIL, JUVENIL,
        BIOGRAFIA, AUTOAYUDA, ENSAYO, OTRO
    }
}