package com.tfg.charmreader.menu.priv.estanteria;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tfg.charmreader.R;
import com.tfg.charmreader.Utilidades;
import com.tfg.charmreader.databinding.ActivityNuevaEstanteriaBinding;
import com.tfg.charmreader.interfacesAPI.I_ApiEstanteria;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.Estanteria;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CargarNuevaEstanteria extends AppCompatActivity {
    private ActivityNuevaEstanteriaBinding binding;
    private final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private final I_ApiEstanteria apiEstanteria = API.getInstancia().create(I_ApiEstanteria.class);

    // Color por defecto (el lila de tu app)
    private String colorSeleccionado = "#F3E5F5";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNuevaEstanteriaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (firebaseUser == null) {
            finish();
            return;
        }

        configurarSelectorColores();

        // Actualizamos los IDs a los nuevos del XML (btnGuardar)
        binding.btnGuardar.setOnClickListener(v -> guardar());
    }

    private void configurarSelectorColores() {
        // Asignamos listeners a cada View de color
        View.OnClickListener colorListener = v -> {
            // Resetear escalas (efecto visual de selección)
            resetearEscalasColores();
            v.setScaleX(1.3f);
            v.setScaleY(1.3f);

            // Cambiar el color del preview de arriba
            if (v.getId() == R.id.color1) colorSeleccionado = "#F3E5F5";
            if (v.getId() == R.id.color2) colorSeleccionado = "#E3F2FD";
            if (v.getId() == R.id.color3) colorSeleccionado = "#E8F5E9";
            if (v.getId() == R.id.color4) colorSeleccionado = "#FFF3E0";
            if (v.getId() == R.id.color5) colorSeleccionado = "#FFEBEE";

            binding.ivIconoPreview.getBackground().setColorFilter(
                    android.graphics.Color.parseColor(colorSeleccionado),
                    PorterDuff.Mode.SRC_IN
            );
        };

        binding.color1.setOnClickListener(colorListener);
        binding.color2.setOnClickListener(colorListener);
        binding.color3.setOnClickListener(colorListener);
        binding.color4.setOnClickListener(colorListener);
        binding.color5.setOnClickListener(colorListener);
    }

    private void resetearEscalasColores() {
        binding.color1.setScaleX(1f); binding.color1.setScaleY(1f);
        binding.color2.setScaleX(1f); binding.color2.setScaleY(1f);
        binding.color3.setScaleX(1f); binding.color3.setScaleY(1f);
        binding.color4.setScaleX(1f); binding.color4.setScaleY(1f);
        binding.color5.setScaleX(1f); binding.color5.setScaleY(1f);
    }

    private void guardar() {
        // Usamos etTitulo (TextInputEditText) en lugar del Titulo antiguo
        String titulo = binding.etTitulo.getText().toString().trim();
        if (titulo.isEmpty()){
            binding.tilTitulo.setError("Introduce un nombre");
            return;
        } else {
            binding.tilTitulo.setError(null);
        }

        new Thread(() -> {
            int idUsuario = Utilidades.obtenerIdUsuarioDesdeAPI();
            runOnUiThread(() -> {
                if (idUsuario != -1) {
                    ejecutarGuardadoEstanteria(idUsuario, titulo);
                } else {
                    mostrarAlerta("Error", "No se pudo obtener el ID del usuario");
                }
            });
        }).start();
    }

    private void ejecutarGuardadoEstanteria(int idUsuario, String titulo) {
        Estanteria nueva = new Estanteria(idUsuario, titulo, colorSeleccionado);

        apiEstanteria.anadirEstanteria(nueva).enqueue(new Callback<Estanteria>() {
            @Override
            public void onResponse(Call<Estanteria> call, Response<Estanteria> response) {
                if (response.isSuccessful()) {
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            }
            @Override
            public void onFailure(Call<Estanteria> call, Throwable t) {
                Log.e("API", "Error al guardar");
            }
        });
    }

    public void mostrarAlerta(String titulo, String contenido) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(contenido)
                .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                .show();
    }
}