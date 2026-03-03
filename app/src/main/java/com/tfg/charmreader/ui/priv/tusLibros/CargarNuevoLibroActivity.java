package com.tfg.charmreader.ui.priv.tusLibros;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import com.tfg.charmreader.databinding.ActivityCargarNuevoLibroBinding;
import com.tfg.charmreader.viewmodel.priv.tuslibros.CargarLibroViewModel;

public class CargarNuevoLibroActivity extends AppCompatActivity {

    private ActivityCargarNuevoLibroBinding binding;
    private CargarLibroViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCargarNuevoLibroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(CargarLibroViewModel.class);
        setupObservers();

        openEpubPicker();
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
            viewModel.procesarEpub(data.getData(), idUsuario);
        } else {
            finish();
        }
    }
}