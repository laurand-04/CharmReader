package com.tfg.charmreader.menu.futuro;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
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
    private LibrosSinEstrenar libroRecibido;
    private Date fechaEditada;
    private I_ApiLibrosSinEstrenar apiService;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_futuro_libro);

        // Inicializar
        apiService = API.getInstancia().create(I_ApiLibrosSinEstrenar.class);
        etTitulo = findViewById(R.id.etDetalleTitulo);
        etAutor = findViewById(R.id.etDetalleAutor);
        etFecha = findViewById(R.id.etDetalleFecha);
        btnActualizar = findViewById(R.id.btnActualizar);

        // Obtener el objeto enviado desde el RecyclerView
        libroRecibido = (LibrosSinEstrenar) getIntent().getSerializableExtra("libro_seleccionado");

        if (libroRecibido != null) {
            cargarDatos();
        }

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

    private void mostrarCalendario() {
        Calendar cal = Calendar.getInstance();
        if (fechaEditada != null) cal.setTime(fechaEditada);

        DatePickerDialog dpd = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar seleccion = Calendar.getInstance();
            seleccion.set(year, month, dayOfMonth);
            fechaEditada = seleccion.getTime();
            etFecha.setText(sdf.format(fechaEditada));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        dpd.getDatePicker().setMinDate(System.currentTimeMillis());
        dpd.show();
    }

    private void actualizarLibro() {
        // Actualizamos los valores en el objeto
        libroRecibido.setAutor(etAutor.getText().toString());
        libroRecibido.setFechaPublicacion(fechaEditada);
        // Nota: Si el nombre cambió y es parte del ID, esto podría crear un registro nuevo en lugar de actualizar.
        libroRecibido.getId().setNombre(etTitulo.getText().toString());

        apiService.guardarLibrosSinEstrenar(libroRecibido).enqueue(new Callback<LibrosSinEstrenar>() {
            @Override
            public void onResponse(Call<LibrosSinEstrenar> call, Response<LibrosSinEstrenar> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(FuturoLibro.this, "Cambios guardados", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(FuturoLibro.this, "Error 400: Revisa los datos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LibrosSinEstrenar> call, Throwable t) {
                Toast.makeText(FuturoLibro.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}