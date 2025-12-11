package com.tfg.charmreader.autentication;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.tfg.charmreader.menu.MainActivity;
import com.tfg.charmreader.databinding.ActivityLoginBinding;

import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_login);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.loginButton.setOnClickListener(v -> login());

        binding.registerTextView.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        binding.recoverTextView.setOnClickListener(v ->
                startActivity(new Intent(this, RecuperacionActivity.class)));
    }

    private void login() {
        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()) {
            mostrarAlerta("Alerta", "Rellena todos los campos para poder iniciar sesion");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        mostrarError(task.getException());
                    }
                });
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
            // Usuario no existe o está deshabilitado
            mensajeError = "Usuario no registrado o deshabilitado.";
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            // Contraseña incorrecta o formato de email inválido
            mensajeError = "Credenciales inválidas, revisa el email o la contraseña.";
        } else if (e != null) {
            mensajeError = e.getMessage(); // Mensaje de error original
        }

        mostrarAlerta("Error", mensajeError);
    }

}

