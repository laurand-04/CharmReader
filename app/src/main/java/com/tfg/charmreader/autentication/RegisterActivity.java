package com.tfg.charmreader.autentication;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.tfg.charmreader.databinding.ActivityRegisterBinding;
import com.tfg.charmreader.interfacesAPI.I_ApiUsuario;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.Usuario;

import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

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
        //setContentView(R.layout.activity_register);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.registerButton.setOnClickListener(v -> register());
    }

    private void register() {
        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();
        String confirmPassword = binding.confirmPasswordEditText.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()) {
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

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    try {
                        if (task.isSuccessful()) {
                            apiUsuario.obtenerIdMaximoUsuario().enqueue(new Callback<Integer>() {
                                @Override
                                public void onResponse(Call<Integer> call, Response<Integer> response) {
                                    if (response.isSuccessful()) {
                                        new Thread(() -> {
                                            try {
                                                // 1️⃣ Obtener el ID máximo
                                                Response<Integer> respIdMax = apiUsuario.obtenerIdMaximoUsuario().execute();
                                                if (respIdMax.isSuccessful() && respIdMax.body() != null) {
                                                    int idMaximo = respIdMax.body();

                                                    // 2️⃣ Crear el usuario
                                                    Usuario nuevoUsuario = new Usuario(0, email);

                                                    // 3️⃣ Guardar el usuario en la API
                                                    Response<Usuario> respGuardar = apiUsuario.guardarUsuario(nuevoUsuario).execute();

                                                    if (!respGuardar.isSuccessful()) {
                                                        Log.e("API_ERROR", "Código: " + respGuardar.code() + " - " + respGuardar.message());
                                                        Log.e("API_ERROR", "Cuerpo: " + respGuardar.errorBody().string());
                                                    }

                                                    if (respGuardar.isSuccessful()) {
                                                        // 4️⃣ Volver al hilo de la UI para cerrar la Activity
                                                        runOnUiThread(() -> RegisterActivity.this.finish());
                                                    } else {
                                                        runOnUiThread(() ->
                                                                mostrarAlerta("Error", "No se pudo guardar el usuario en la base de datos"));
                                                    }
                                                } else {
                                                    runOnUiThread(() ->
                                                            mostrarAlerta("Error", "No se pudo obtener el ID máximo del usuario"));
                                                }
                                            } catch (Exception e) {
                                                runOnUiThread(() ->
                                                        mostrarAlerta("Error", "Error de conexión: " + e.getMessage()));
                                            }
                                        }).start();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Integer> call, Throwable t) {
                                    mostrarAlerta("Error", "No se pudo obtener el ID máximo del usuario: " + t.getMessage());
                                }
                            });

                        } else {
                            mostrarError(task.getException());
                        }
                    } catch (RuntimeException e) {
                        mostrarAlerta("Error", "Error durante la creación del usuario");
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

        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
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
