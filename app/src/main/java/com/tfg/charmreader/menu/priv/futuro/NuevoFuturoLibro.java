package com.tfg.charmreader.menu.priv.futuro;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
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
import com.tfg.charmreader.objetosBD.CCLibrosSinEstrenar;
import com.tfg.charmreader.objetosBD.LibrosSinEstrenar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NuevoFuturoLibro extends AppCompatActivity {
    private TextInputEditText etTitulo, etAutor, etFecha;
    private MaterialButton btnGuardar;
    private ImageView btnBack;
    private I_ApiLibrosSinEstrenar apiService;
    private Date fechaSeleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_futuro_libro);

        // 🔥 Efecto de desenfoque detrás del diálogo (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            getWindow().getAttributes().setBlurBehindRadius(20);
        }

        apiService = API.getInstancia().create(I_ApiLibrosSinEstrenar.class);
        vincularVistas();

        // 🔥 Listener para la 'X' con confirmación
        btnBack.setOnClickListener(v -> comprobarYSalir());

        // 🔥 Manejar botón 'Atrás' físico
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                comprobarYSalir();
            }
        });

        etFecha.setOnClickListener(v -> mostrarDatePicker());
        btnGuardar.setOnClickListener(v -> guardarLibro());
    }

    private void vincularVistas() {
        etTitulo = findViewById(R.id.etTitulo);
        etAutor = findViewById(R.id.etAutor);
        etFecha = findViewById(R.id.etFecha);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnBack = findViewById(R.id.btnBackNuevoFuturo);
    }

    private void comprobarYSalir() {
        String titulo = etTitulo.getText().toString().trim();
        String autor = etAutor.getText().toString().trim();

        if (!titulo.isEmpty() || !autor.isEmpty() || fechaSeleccionada != null) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("¿Descartar lanzamiento?")
                    .setMessage("Tienes cambios sin guardar. Si sales ahora, perderás la información introducida.")
                    .setNegativeButton("Seguir editando", (dialog, which) -> dialog.dismiss())
                    .setPositiveButton("Descartar", (dialog, which) -> finish())
                    .show();
        } else {
            finish();
        }
    }

    private void mostrarDatePicker() {
        Calendar calendar = Calendar.getInstance();

        // Añadimos R.style.DialogPickerTheme aquí para que combine con tu app
        DatePickerDialog datePicker = new DatePickerDialog(this, R.style.DialogPickerTheme, (view, year, month, dayOfMonth) -> {
            Calendar seleccion = Calendar.getInstance();
            seleccion.set(year, month, dayOfMonth);
            fechaSeleccionada = seleccion.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etFecha.setText(sdf.format(fechaSeleccionada));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        // No permitimos seleccionar fechas pasadas, ya que es un "Lanzamiento Futuro"
        datePicker.getDatePicker().setMinDate(System.currentTimeMillis());
        datePicker.show();
    }

    private void guardarLibro() {
        String titulo = etTitulo.getText().toString().trim();
        String autor = etAutor.getText().toString().trim();

        if (titulo.isEmpty() || autor.isEmpty() || fechaSeleccionada == null) {
            Toast.makeText(this, "Por favor, rellena todos los datos", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);

        if (idUsuario == -1) {
            Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGuardar.setEnabled(false);
        btnGuardar.setText("GUARDANDO...");

        enviarServidor(idUsuario, titulo, autor);
    }

    private void enviarServidor(int idUsuario, String titulo, String autor) {
        CCLibrosSinEstrenar claveCompuesta = new CCLibrosSinEstrenar(idUsuario, titulo);
        LibrosSinEstrenar nuevoLibro = new LibrosSinEstrenar();
        nuevoLibro.setId(claveCompuesta);
        nuevoLibro.setAutor(autor);
        nuevoLibro.setFechaPublicacion(fechaSeleccionada);

        apiService.guardarLibrosSinEstrenar(nuevoLibro).enqueue(new Callback<LibrosSinEstrenar>() {
            @Override
            public void onResponse(Call<LibrosSinEstrenar> call, Response<LibrosSinEstrenar> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(NuevoFuturoLibro.this, "¡Lanzamiento guardado!", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    restaurarBoton();
                }
            }

            @Override
            public void onFailure(Call<LibrosSinEstrenar> call, Throwable t) {
                restaurarBoton();
            }
        });
    }

    private void restaurarBoton() {
        btnGuardar.setEnabled(true);
        btnGuardar.setText("GUARDAR LIBRO");
    }
}