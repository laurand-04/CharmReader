package com.tfg.charmreader.autentication;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.tfg.charmreader.databinding.ActivityRegisterBinding;
import com.tfg.charmreader.interfacesAPI.I_ApiUsuario;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.Usuario;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private ActivityRegisterBinding binding;
    private I_ApiUsuario apiUsuario = API.getInstancia().create(I_ApiUsuario.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar Binding
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // BOTÓN VOLVER - Usamos directamente el binding para asegurar que conecte
        binding.btnVolver.setOnClickListener(v -> {
            Log.d("DEBUG_REG", "Botón volver pulsado");
            finish();
        });

        // BOTÓN REGISTRO
        binding.registerButton.setOnClickListener(v -> register());
    }

    private void register() {
        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();
        String confirmPassword = binding.confirmPasswordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            mostrarAlerta("Alerta", "Rellena todos los campos para poder registrarte");
            return;
        }

        if (password.length() < 6) {
            mostrarAlerta("Error", "La contraseña debe tener al menos 6 caracteres");
            return;
        }

        if (!password.equals(confirmPassword)) {
            mostrarAlerta("Error", "Las contraseñas no coinciden");
            return;
        }

        // Crear usuario en Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        guardarUsuarioEnServidor(email);
                    } else {
                        mostrarError(task.getException());
                    }
                });
    }

    private void guardarUsuarioEnServidor(String email) {
        // Ejecutamos en un hilo secundario la lógica de red síncrona
        new Thread(() -> {
            try {
                // 1. Crear el objeto Usuario (el ID suele ser autoincremental en el servidor)
                Usuario nuevoUsuario = new Usuario(0, email);

                // 2. Guardar en la API
                Response<Usuario> respGuardar = apiUsuario.guardarUsuario(nuevoUsuario).execute();

                if (respGuardar.isSuccessful()) {
                    runOnUiThread(() ->
                            mostrarAlerta("Éxito", "Cuenta creada correctamente", () -> finish())
                    );
                } else {
                    Log.e("API_ERROR", "Error al guardar: " + respGuardar.code());
                    runOnUiThread(() ->
                            mostrarAlerta("Error", "Firebase OK, pero no se pudo sincronizar con la BD local")
                    );
                }
            } catch (Exception e) {
                Log.e("API_ERROR", "Excepción: " + e.getMessage());
                runOnUiThread(() ->
                        mostrarAlerta("Error", "Error de conexión: " + e.getMessage())
                );
            }
        }).start();
    }

    // Método para mostrar alerta con acción al cerrar (ej. cerrar activity)
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

    // Método para alerta simple
    public void mostrarAlerta(String titulo, String contenido) {
        mostrarAlerta(titulo, contenido, null);
    }

    public void mostrarError(Exception e) {
        String mensajeError = "Error desconocido";
        if (e instanceof FirebaseAuthUserCollisionException) {
            mensajeError = "Este usuario ya está registrado.";
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            mensajeError = "Email inválido o contraseña no cumple requisitos.";
        } else if (e != null) {
            mensajeError = e.getMessage();
        }
        mostrarAlerta("Error", mensajeError);
    }
}