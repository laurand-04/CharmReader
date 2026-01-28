package com.tfg.charmreader.menu.futuro;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.tfg.charmreader.R;
import com.tfg.charmreader.Utilidades;
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
    private I_ApiLibrosSinEstrenar apiService;
    private Date fechaSeleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_futuro_libro);

        apiService = API.getInstancia().create(I_ApiLibrosSinEstrenar.class);
        etTitulo = findViewById(R.id.etTitulo);
        etAutor = findViewById(R.id.etAutor);
        etFecha = findViewById(R.id.etFecha);
        btnGuardar = findViewById(R.id.btnGuardar);

        etFecha.setOnClickListener(v -> mostrarDatePicker());
        btnGuardar.setOnClickListener(v -> guardarLibro());
    }

    private void mostrarDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar seleccion = Calendar.getInstance();
            seleccion.set(year, month, dayOfMonth);
            fechaSeleccionada = seleccion.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etFecha.setText(sdf.format(fechaSeleccionada));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePicker.getDatePicker().setMinDate(System.currentTimeMillis());
        datePicker.show();
    }

    private void guardarLibro() {
        String titulo = etTitulo.getText().toString().trim();
        String autor = etAutor.getText().toString().trim();

        if (titulo.isEmpty() || autor.isEmpty() || fechaSeleccionada == null) {
            Toast.makeText(this, "Datos incompletos", Toast.LENGTH_SHORT).show();
            return;
        }

        // SOLUCIÓN AL CRASH: Ejecutar la obtención del ID en un hilo aparte
        new Thread(() -> {
            try {
                Log.d("DEBUG_GUARDAR", "Obteniendo ID de usuario en hilo secundario...");
                int idUsuario = Utilidades.obtenerIdUsuarioDesdeAPI();
                Log.d("DEBUG_GUARDAR", "ID obtenido: " + idUsuario);

                // Una vez tenemos el ID, volvemos al hilo principal para Retrofit (que ya gestiona sus propios hilos)
                runOnUiThread(() -> enviarServidor(idUsuario, titulo, autor));

            } catch (Exception e) {
                Log.e("DEBUG_GUARDAR", "Error en hilo: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "Error obteniendo usuario", Toast.LENGTH_SHORT).show());
            }
        }).start();
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
                    Toast.makeText(NuevoFuturoLibro.this, "¡Guardado!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Log.e("DEBUG_GUARDAR", "Error API: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<LibrosSinEstrenar> call, Throwable t) {
                Log.e("DEBUG_GUARDAR", "Fallo red: " + t.getMessage());
            }
        });
    }
}