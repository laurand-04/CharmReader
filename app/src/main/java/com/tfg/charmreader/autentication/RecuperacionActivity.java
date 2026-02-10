package com.tfg.charmreader.autentication;

import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.tfg.charmreader.databinding.ActivityRecuperacionBinding;

public class RecuperacionActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private ActivityRecuperacionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializamos el binding
        binding = ActivityRecuperacionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // 1. Lógica del botón principal de enviar instrucciones
        binding.recuperacionButton.setOnClickListener(v -> recuperacion());

        // 2. Lógica de la FLECHA de volver (btnVolver)
        // Al usar Binding y haber puesto elevation en el XML, esto funcionará ahora sí
        binding.btnVolver.setOnClickListener(v -> finish());
    }

    private void recuperacion() {
        String email = binding.emailEditText.getText().toString().trim();
        if (email.isEmpty()) {
            mostrarAlerta("Alerta", "Rellena el correo para poder iniciar la recuperación de contraseña");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        mostrarAlerta("Envío de correo",
                                "Si existe una cuenta asociada a este email, recibirás un correo de recuperación",
                                () -> finish()); // Cerramos la actividad al aceptar
                    } else {
                        mostrarError(task.getException());
                    }
                });
    }

    // --- MÉTODOS DE APOYO (ALERTAS) ---

    public void mostrarAlerta(String titulo, String contenido, Runnable accionAlCerrar) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titulo);
        builder.setMessage(contenido);
        builder.setCancelable(false);
        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            dialog.dismiss();
            if (accionAlCerrar != null) accionAlCerrar.run();
        });
        builder.show();
    }

    public void mostrarAlerta(String titulo, String contenido) {
        mostrarAlerta(titulo, contenido, null);
    }

    public void mostrarError(Exception e){
        String mensajeError = "Error desconocido";
        if (e instanceof FirebaseAuthInvalidUserException) {
            mensajeError = "No existe ningún usuario con ese email.";
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            mensajeError = "El email introducido no es válido.";
        } else if (e != null) {
            mensajeError = e.getMessage();
        }
        mostrarAlerta("Error", mensajeError);
    }
}