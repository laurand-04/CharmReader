package com.tfg.charmreader.menu.priv.estanteria;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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

    private String colorSeleccionado = "#F3E5F5";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNuevaEstanteriaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            getWindow().getAttributes().setBlurBehindRadius(20);
        }

        if (firebaseUser == null) {
            finish();
            return;
        }

        // 🔥 Listener para la 'X' con confirmación
        binding.btnBackNuevaEstanteria.setOnClickListener(v -> comprobarYSalir());

        // 🔥 Manejar también el botón 'Atrás' físico del sistema
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                comprobarYSalir();
            }
        });

        configurarSelectorColores();
        binding.btnGuardar.setOnClickListener(v -> guardar());
    }

    /**
     * Comprueba si el usuario ha escrito algo antes de cerrar la pantalla
     */
    private void comprobarYSalir() {
        String titulo = binding.etTitulo.getText().toString().trim();

        if (!titulo.isEmpty()) {
            // Si hay texto, preguntamos
            new MaterialAlertDialogBuilder(this)
                    .setTitle("¿Descartar estantería?")
                    .setMessage("Tienes cambios sin guardar. Si sales ahora, perderás la información introducida.")
                    .setNegativeButton("Seguir editando", (dialog, which) -> dialog.dismiss())
                    .setPositiveButton("Descartar", (dialog, which) -> finish())
                    .show();
        } else {
            // Si está vacío, cerramos directamente
            finish();
        }
    }

    private void configurarSelectorColores() {
        View.OnClickListener colorListener = v -> {
            resetearEscalasColores();
            v.setScaleX(1.3f);
            v.setScaleY(1.3f);

            if (v.getId() == R.id.color1) colorSeleccionado = "#F3E5F5";
            else if (v.getId() == R.id.color2) colorSeleccionado = "#E3F2FD";
            else if (v.getId() == R.id.color3) colorSeleccionado = "#E8F5E9";
            else if (v.getId() == R.id.color4) colorSeleccionado = "#FFF3E0";
            else if (v.getId() == R.id.color5) colorSeleccionado = "#FFEBEE";

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
        String titulo = binding.etTitulo.getText().toString().trim();
        if (titulo.isEmpty()){
            binding.tilTitulo.setError("Introduce un nombre");
            return;
        } else {
            binding.tilTitulo.setError(null);
        }

        new Thread(() -> {
            SharedPreferences prefs = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
            int idUsuario = prefs.getInt("idUsuario", -1);
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