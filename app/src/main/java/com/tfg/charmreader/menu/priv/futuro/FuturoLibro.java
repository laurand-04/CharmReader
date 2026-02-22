package com.tfg.charmreader.menu.priv.futuro;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_ApiLibrosSinEstrenar;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.LibrosSinEstrenar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FuturoLibro extends AppCompatActivity {

    private TextInputEditText etTitulo, etAutor, etFecha;
    private MaterialButton btnActualizar;
    private ImageView btnBack;
    private LibrosSinEstrenar libroRecibido;
    private Date fechaEditada;
    private I_ApiLibrosSinEstrenar apiService;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_futuro_libro);

        // 🔥 Efecto de desenfoque detrás del diálogo (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            getWindow().getAttributes().setBlurBehindRadius(20);
        }

        // Inicializar vistas y API
        apiService = API.getInstancia().create(I_ApiLibrosSinEstrenar.class);
        etTitulo = findViewById(R.id.etDetalleTitulo);
        etAutor = findViewById(R.id.etDetalleAutor);
        etFecha = findViewById(R.id.etDetalleFecha);
        btnActualizar = findViewById(R.id.btnActualizar);
        btnBack = findViewById(R.id.btnBackDetalleFuturo);

        libroRecibido = (LibrosSinEstrenar) getIntent().getSerializableExtra("libro_seleccionado");

        if (libroRecibido != null) {
            cargarDatos();
        }

        // 🔥 Listener para la 'X' con confirmación
        btnBack.setOnClickListener(v -> comprobarYSalir());

        // 🔥 Manejar botón 'Atrás' físico
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                comprobarYSalir();
            }
        });

        etFecha.setOnClickListener(v -> mostrarCalendario());
        btnActualizar.setOnClickListener(v -> actualizarLibro());
    }

    private void cargarDatos() {
        etTitulo.setText(libroRecibido.getId().getNombre());
        etAutor.setText(libroRecibido.getAutor());
        if (libroRecibido.getFechaPublicacion() != null) {
            fechaEditada = libroRecibido.getFechaPublicacion();
            etFecha.setText(sdf.format(fechaEditada));
        }
    }

    private void comprobarYSalir() {
        // Verificamos si los datos actuales son diferentes a los recibidos originalmente
        String tituloActual = etTitulo.getText().toString().trim();
        String autorActual = etAutor.getText().toString().trim();

        boolean haCambiado = !tituloActual.equals(libroRecibido.getId().getNombre()) ||
                !autorActual.equals(libroRecibido.getAutor()) ||
                (fechaEditada != null && !fechaEditada.equals(libroRecibido.getFechaPublicacion()));

        if (haCambiado) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("¿Descartar cambios?")
                    .setMessage("Tienes cambios sin guardar. Si sales ahora, se perderán.")
                    .setNegativeButton("Seguir editando", (dialog, which) -> dialog.dismiss())
                    .setPositiveButton("Descartar", (dialog, which) -> finish())
                    .show();
        } else {
            finish();
        }
    }

    private void mostrarCalendario() {
        Calendar cal = Calendar.getInstance();
        if (fechaEditada != null) cal.setTime(fechaEditada);

        // Usamos R.style.DialogPickerTheme para mantener tus colores corporativos
        DatePickerDialog dpd = new DatePickerDialog(this, R.style.DialogPickerTheme, (view, year, month, dayOfMonth) -> {
            Calendar seleccion = Calendar.getInstance();
            seleccion.set(year, month, dayOfMonth);
            fechaEditada = seleccion.getTime();
            etFecha.setText(sdf.format(fechaEditada));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        dpd.getDatePicker().setMinDate(System.currentTimeMillis());
        dpd.show();
    }

    private void actualizarLibro() {
        String nuevoTitulo = etTitulo.getText().toString().trim();
        String nuevoAutor = etAutor.getText().toString().trim();

        if (nuevoTitulo.isEmpty() || nuevoAutor.isEmpty() || fechaEditada == null) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        btnActualizar.setEnabled(false);
        btnActualizar.setText("GUARDANDO...");

        libroRecibido.setAutor(nuevoAutor);
        libroRecibido.setFechaPublicacion(fechaEditada);
        libroRecibido.getId().setNombre(nuevoTitulo);

        apiService.guardarLibrosSinEstrenar(libroRecibido).enqueue(new Callback<LibrosSinEstrenar>() {
            @Override
            public void onResponse(Call<LibrosSinEstrenar> call, Response<LibrosSinEstrenar> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(FuturoLibro.this, "Lanzamiento actualizado", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    restaurarBoton();
                    Toast.makeText(FuturoLibro.this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LibrosSinEstrenar> call, Throwable t) {
                restaurarBoton();
                Toast.makeText(FuturoLibro.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void restaurarBoton() {
        btnActualizar.setEnabled(true);
        btnActualizar.setText("GUARDAR CAMBIOS");
    }
}