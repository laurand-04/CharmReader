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
        binding.btnVolver.setOnClickListener(v -> finish());
    }

    private void recuperacion() {
        String email = binding.emailEditText.getText().toString().trim();
        if (email.isEmpty()) {
            mostrarAlerta("Campo vacío", "Por favor, introduce tu correo electrónico para enviarte las instrucciones.");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Mensaje mejorado con aviso de SPAM
                        String mensajeExito = "Si existe una cuenta asociada a este email, recibirás un correo en unos instantes.\n\n" +
                                "⚠️ IMPORTANTE: Si no lo encuentras en tu bandeja de entrada, por favor, revisa tu carpeta de Correo no deseado o Spam.";

                        mostrarAlerta("Correo enviado", mensajeExito, () -> finish());
                    } else {
                        mostrarError(task.getException());
                    }
                });
    }

    // --- MÉTODOS DE APOYO (ALERTAS) ---

    public void mostrarAlerta(String titulo, String contenido, Runnable accionAlCerrar) {
        // Usamos MaterialAlertDialogBuilder si quieres que se vea más moderno (estilo Admin)
        // o mantenemos AlertDialog.Builder para ser coherentes con el resto de la app
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titulo);
        builder.setMessage(contenido);
        builder.setCancelable(false);
        builder.setPositiveButton("Entendido", (dialog, which) -> {
            dialog.dismiss();
            if (accionAlCerrar != null) accionAlCerrar.run();
        });
        builder.show();
    }

    public void mostrarAlerta(String titulo, String contenido) {
        mostrarAlerta(titulo, contenido, null);
    }

    public void mostrarError(Exception e){
        String mensajeError = "Ha ocurrido un error inesperado. Inténtalo de nuevo más tarde.";
        if (e instanceof FirebaseAuthInvalidUserException) {
            mensajeError = "No hemos encontrado ninguna cuenta vinculada a este correo electrónico.";
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            mensajeError = "El formato del correo electrónico no es válido.";
        } else if (e != null) {
            mensajeError = e.getMessage();
        }
        mostrarAlerta("No se pudo enviar", mensajeError);
    }
}