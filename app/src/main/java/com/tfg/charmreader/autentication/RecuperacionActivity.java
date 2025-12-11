package com.tfg.charmreader.autentication;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.tfg.charmreader.databinding.ActivityRecuperacionBinding;

import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;

public class RecuperacionActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private ActivityRecuperacionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_register);
        binding = ActivityRecuperacionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.recuperacionButton.setOnClickListener(v -> recuperacion());
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
                        mostrarAlerta("Envío de correo", "Si existe una cuenta asociaada a este email, recibirás un correo de recuperación", () -> {
                            finish(); // Se ejecuta después de que el usuario pulse en el botón del diálogo
                        });
                    } else {
                        mostrarError(task.getException());
                    }
                });
    }

    public void mostrarAlerta(String titulo, String contenido, Runnable accionAlCerrar) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titulo);
        builder.setMessage(contenido);
        builder.setCancelable(false);

        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            dialog.dismiss();
            if (accionAlCerrar != null) {
                accionAlCerrar.run(); // Ejecuta la acción (en este caso, finish)
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void mostrarAlerta(String titulo, String contenido) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titulo);
        builder.setMessage(contenido);

        builder.setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
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
