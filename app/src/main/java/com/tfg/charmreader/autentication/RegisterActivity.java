package com.tfg.charmreader.autentication;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
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

        // BOTÓN VOLVER
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

        // 1. Crear usuario en Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        // 2. Enviar correo de verificación
                        enviarCorreoVerificacion(user);

                        // 3. Guardar en SQL
                        guardarUsuarioEnServidor(email);
                    } else {
                        mostrarError(task.getException());
                    }
                });
    }

    private void enviarCorreoVerificacion(FirebaseUser user) {
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("DEBUG_REG", "Correo de verificación enviado correctamente.");
                        } else {
                            Log.e("DEBUG_REG", "Error al enviar correo: " + task.getException().getMessage());
                        }
                    });
        }
    }

    private void guardarUsuarioEnServidor(String email) {
        new Thread(() -> {
            try {
                // Crear el objeto Usuario para tu BD SQL
                Usuario nuevoUsuario = new Usuario(0, email);

                // Guardar en la API
                Response<Usuario> respGuardar = apiUsuario.guardarUsuario(nuevoUsuario).execute();

                if (respGuardar.isSuccessful()) {
                    runOnUiThread(() ->
                            mostrarAlertaVerificacionPendiente(email)
                    );
                } else {
                    Log.e("API_ERROR", "Error al guardar en SQL: " + respGuardar.code());
                    runOnUiThread(() ->
                            mostrarAlerta("Error", "Usuario creado en Firebase, pero hubo un problema con la base de datos local.")
                    );
                }
            } catch (Exception e) {
                Log.e("API_ERROR", "Excepción: " + e.getMessage());
                runOnUiThread(() ->
                        mostrarAlerta("Error", "Error de conexión al guardar los datos.")
                );
            }
        }).start();
    }

    private void mostrarAlertaVerificacionPendiente(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Acceso Restringido");
        builder.setMessage("Hemos enviado un enlace a: " + email + "\n\nDebes verificar tu cuenta en tu correo para poder continuar. Una vez lo hagas, pulsa 'YA HE VERIFICADO'.");
        builder.setCancelable(false);

        // BOTÓN DE COMPROBACIÓN
        builder.setPositiveButton("YA HE VERIFICADO", null);

        // BOTÓN DE REENVÍO
        builder.setNeutralButton("REENVIAR CORREO", (dialog, which) -> {
            enviarCorreoVerificacion(mAuth.getCurrentUser());
            mostrarAlerta("Enviado", "Se ha reenviado el enlace.");
        });

        // BOTÓN PARA SALIR
        builder.setNegativeButton("CANCELAR REGISTRO", (dialog, which) -> {
            mAuth.signOut();
            finish();
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        // Lógica personalizada para el botón positivo
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                mAuth.getCurrentUser().reload().addOnCompleteListener(task -> {

                    String emailUsuario = mAuth.getCurrentUser().getEmail();

                    if (mAuth.getCurrentUser().isEmailVerified()) {
                        // Llamamos a la API para enviar correo de bienvenida
                        apiUsuario.enviarCorreoBienvenida(emailUsuario).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                dialog.dismiss();
                                mostrarAlerta("¡Todo listo!", "Bienvenido a CharmReader. Ya puedes iniciar sesión.", () -> finish());
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                // Si falla el correo de bienvenida, cerramos igual porque ya está verificado en Firebase
                                dialog.dismiss();
                                finish();
                            }
                        });
                    } else {
                        dialog.setMessage("⚠️ No detectamos la verificación.\n\nPor favor, haz clic en el enlace que enviamos a " + emailUsuario + " y vuelve a intentarlo.");
                    }
                });
            }
        });
    }

    // MÉTODOS DE ALERTA SOBRECARGADOS
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