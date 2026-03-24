package com.tfg.charmreader.ui.autentication.perfil.publico;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.tfg.charmreader.R;
import com.tfg.charmreader.data.model.Libro;
import com.tfg.charmreader.data.model.Obras;
import com.tfg.charmreader.databinding.ActivityDetalleObraBinding;
import com.tfg.charmreader.viewmodel.autentication.perfil.publico.DetalleObraViewModel;

public class DetalleObraActivity extends AppCompatActivity {

    private ActivityDetalleObraBinding binding;
    private DetalleObraViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetalleObraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar el ViewModel siguiendo el patrón MVVM
        viewModel = new ViewModelProvider(this).get(DetalleObraViewModel.class);

        // Recuperar datos enviados desde el Fragmento anterior
        Libro libroExtra = (Libro) getIntent().getSerializableExtra("Libro");

        setupObservers();
        setupListeners();

        // Si tenemos el libro, pedimos al ViewModel que cargue la información de la Obra
        if (libroExtra != null) {
            Log.d("DetalleObra", "Cargando obra para idLibro: " + libroExtra.getId());
            viewModel.cargarDetalleObra(libroExtra.getId());
        } else {
            Toast.makeText(this, "Error: No se pudo recuperar la información del libro", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupObservers() {
        // Observar estado de carga (Maneja el layout semitransparente con ProgressBar)
        viewModel.getIsLoading().observe(this, cargando -> {
            if (cargando != null) {
                binding.layoutLoadingDetalle.setVisibility(cargando ? View.VISIBLE : View.GONE);
                // Deshabilitar botón durante la carga para evitar peticiones duplicadas
                binding.btnDescargarObra.setEnabled(!cargando);
            }
        });

        // Observar los datos de la obra una vez lleguen de la API
        viewModel.getObraLiveData().observe(this, obra -> {
            if (obra != null) {
                vincularDatos(obra);
            }
        });

        // Observar el éxito del registro de la descarga (Libro de Usuario)
        viewModel.getDescargaExitosa().observe(this, exitoso -> {
            if (exitoso != null && exitoso) {
                Toast.makeText(this, "¡Libro añadido a tu biblioteca!", Toast.LENGTH_LONG).show();
                binding.btnDescargarObra.setEnabled(false);
                binding.btnDescargarObra.setText("Ya en tu biblioteca");
            }
        });

        // Observar posibles mensajes de error
        viewModel.getErrorLiveData().observe(this, mensaje -> {
            if (mensaje != null) {
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        // Botón de retroceso (Flecha superior)
        binding.btnBackDetalle.setOnClickListener(v -> finish());

        // Botón de descargar / añadir a biblioteca
        binding.btnDescargarObra.setOnClickListener(v -> {
            Obras obraActual = viewModel.getObraLiveData().getValue();
            if (obraActual != null) {
                    viewModel.registrarDescarga(obraActual);
                } else {
                Toast.makeText(this, "Debes iniciar sesión para descargar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void vincularDatos(Obras obra) {
        // Rellenar la UI con los datos obtenidos de la tabla 'obras'
        binding.tvDetalleTitulo.setText(obra.getNombre());
        binding.tvDetalleAutor.setText("Por " + obra.getAutor());
        binding.tvDetalleSinopsis.setText(obra.getSinopsis());

        // Opcional: Actualizar el título de la Toolbar con el nombre de la obra
        binding.tvToolbarTitulo.setText(obra.getNombre());

        // Cargar imagen de portada con Glide
        Glide.with(this)
                .load(obra.getUrl_imagen())
                .placeholder(R.drawable.ic_libro)
                .error(R.drawable.ic_libro)
                .into(binding.ivDetallePortada);
    }
}