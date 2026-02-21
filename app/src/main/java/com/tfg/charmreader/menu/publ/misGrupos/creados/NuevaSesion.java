package com.tfg.charmreader.menu.publ.misGrupos.creados;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_ApiSesion;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.Sesion;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NuevaSesion extends AppCompatActivity {

    private int idGrupo;
    private TextInputEditText etFecha, etHora, etCapInicio, etCapFin;
    private Calendar calendario = Calendar.getInstance();
    private final I_ApiSesion apiSesion = API.getInstancia().create(I_ApiSesion.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_sesion);

        idGrupo = getIntent().getIntExtra("idGrupo", -1);

        vincularVistas();
        configurarSelectores();

        findViewById(R.id.btnBackNuevaSesion).setOnClickListener(v -> finish());
        findViewById(R.id.btnGuardarSesion).setOnClickListener(v -> guardarSesion());
    }

    private void vincularVistas() {
        etFecha = findViewById(R.id.etFecha);
        etHora = findViewById(R.id.etHora);
        etCapInicio = findViewById(R.id.etCapInicio);
        etCapFin = findViewById(R.id.etCapFin);
    }

    private void configurarSelectores() {
        etFecha.setOnClickListener(v -> {
            // Añadimos R.style.DialogPickerTheme aquí
            DatePickerDialog datePicker = new DatePickerDialog(this, R.style.DialogPickerTheme, (view, year, month, day) -> {
                calendario.set(Calendar.YEAR, year);
                calendario.set(Calendar.MONTH, month);
                calendario.set(Calendar.DAY_OF_MONTH, day);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                etFecha.setText(sdf.format(calendario.getTime()));
            }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH));

            datePicker.show();
        });

        etHora.setOnClickListener(v -> {
            // Añadimos R.style.DialogPickerTheme aquí también
            TimePickerDialog timePicker = new TimePickerDialog(this, R.style.DialogPickerTheme, (view, hourOfDay, minute) -> {
                String horaFormateada = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                etHora.setText(horaFormateada);
            }, 18, 0, true);

            timePicker.show();
        });
    }

    private void guardarSesion() {
        String fechaStr = etFecha.getText().toString();
        String horaStr = etHora.getText().toString();
        String capIniStr = etCapInicio.getText().toString();
        String capFinStr = etCapFin.getText().toString();

        // 1. Validación de campos vacíos
        if (fechaStr.isEmpty() || horaStr.isEmpty() || capIniStr.isEmpty() || capFinStr.isEmpty()) {
            Toast.makeText(this, "⚠️ Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int capIni = Integer.parseInt(capIniStr);
        int capFin = Integer.parseInt(capFinStr);

        // 2. Validación de rango de capítulos
        if (capFin <= capIni) {
            etCapFin.setError("El capítulo final debe ser mayor al inicial");
            Toast.makeText(this, "❌ Rango de capítulos inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Validación de fecha (opcional pero recomendada: no permitir fechas pasadas)
        Calendar hoy = Calendar.getInstance();
        // Resetear horas para comparar solo días
        hoy.set(Calendar.HOUR_OF_DAY, 0);
        hoy.set(Calendar.MINUTE, 0);
        hoy.set(Calendar.SECOND, 0);
        hoy.set(Calendar.MILLISECOND, 0);

        if (calendario.before(hoy)) {
            Toast.makeText(this, "❌ No puedes programar una sesión en una fecha pasada", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si todo es correcto, procedemos al envío
        Sesion nueva = new Sesion();
        nueva.setIdGrupo(idGrupo);
        nueva.setFecha(calendario.getTime());
        nueva.setHora(horaStr);
        nueva.setCapituloInicio(capIni);
        nueva.setCapituloFinalizacion(capFin);

        // Feedback visual para el usuario (puedes añadir un ProgressDialog si quieres)
        findViewById(R.id.btnGuardarSesion).setEnabled(false);

        apiSesion.nuevaSesion(nueva).enqueue(new Callback<Sesion>() {
            @Override
            public void onResponse(Call<Sesion> call, Response<Sesion> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(NuevaSesion.this, "✅ Sesión programada con éxito", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // Indicamos éxito
                    finish();
                } else {
                    findViewById(R.id.btnGuardarSesion).setEnabled(true);
                    Toast.makeText(NuevaSesion.this, "Error del servidor al guardar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Sesion> call, Throwable t) {
                findViewById(R.id.btnGuardarSesion).setEnabled(true);
                Toast.makeText(NuevaSesion.this, "Error de red: comprueba tu conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}