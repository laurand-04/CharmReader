package com.tfg.charmreader.ui.priv.tusLibros;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.tfg.charmreader.data.model.BookEn;
import com.tfg.charmreader.data.model.Obras;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import com.tfg.charmreader.databinding.ActivityCargarNuevoLibroBinding;
import com.tfg.charmreader.viewmodel.priv.tuslibros.CargarLibroViewModel;

import java.io.File;

public class CargarNuevoLibroActivity extends AppCompatActivity {

    private ActivityCargarNuevoLibroBinding binding;
    private CargarLibroViewModel viewModel;
    private boolean grupo;
    private int idLibro;
    private String urlObra;
    private Obras obra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCargarNuevoLibroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        grupo = getIntent().getBooleanExtra("grupo", false);
        idLibro = getIntent().getIntExtra("idLibro", -1);
        urlObra = getIntent().getStringExtra("urlObra");
        obra = (Obras) getIntent().getSerializableExtra("obra");

        viewModel = new ViewModelProvider(this).get(CargarLibroViewModel.class);
        setupObservers();
        Log.d("DEBUG_PUBLISH", "url de la obra " + urlObra);

        if (urlObra != null && !urlObra.isEmpty()) {
            idLibro = -3;
            procesarRutaDirecta(urlObra);
        } else {
            // Si no viene ruta (es un libro nuevo externo), abrimos el picker
            openEpubPicker();
        }
    }

    private void procesarRutaDirecta(String ruta) {
        int idUsuario = AuthRepository.getInstance(getApplicationContext()).getIdUsuario();

        try {
            // Convertimos el String de la ruta en una Uri de archivo
            Uri epubUri = Uri.fromFile(new File(ruta));

            // Llamamos al ViewModel directamente
            viewModel.procesarEpub(epubUri, idUsuario, grupo, idLibro, obra);
        } catch (Exception e) {
            Toast.makeText(this, "Error al acceder al archivo de la obra", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupObservers() {
        viewModel.getUploadSuccess().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Libro añadido con éxito", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
                finish();
            }
        });

        viewModel.getError().observe(this, msg -> {
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            finish();
        });
    }

    private void openEpubPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/epub+zip");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 42);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 42 && resultCode == RESULT_OK && data != null) {
            int idUsuario = AuthRepository.getInstance(getApplicationContext()).getIdUsuario();
            viewModel.procesarEpub(data.getData(), idUsuario, grupo, idLibro, null);
        } else {
            finish();
        }
    }
}