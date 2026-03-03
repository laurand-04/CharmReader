package com.tfg.charmreader.data.repository.autentication;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.core.Repo;
import com.tfg.charmreader.data.model.Usuario;
import com.tfg.charmreader.data.network.API.API;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiUsuario;
import com.tfg.charmreader.data.repository.UserRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private static AuthRepository instance = null;
    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final I_ApiUsuario apiUsuario = API.getInstancia().create(I_ApiUsuario.class);
    private static int idUsuario = -1;
    private static Usuario usuario = null;
    private final SharedPreferences preferences;

    private AuthRepository(Context context) {
        this.preferences = context.getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
    }

    public static AuthRepository getInstance(Context context) {
        if (instance == null)
            instance = new AuthRepository(context);
        return instance;

    }

    public String getEmail() {
        return mAuth.getCurrentUser().getEmail();
    }

    public int getIdUsuario() {
        if (idUsuario !=-1)
            return idUsuario;
        RepositoryCallback<Usuario> callback = new RepositoryCallback<Usuario>() {
            @Override
            public void onComplete(Usuario usu) {
                // AQUÍ es donde usas el ID (ej: cargar datos del usuario)
                Log.d("DEBUG", "El ID obtenido es: " + usu.getId());
                usuario = usu;
                idUsuario = usu.getId();
            }

            @Override
            public void onError(String message) {
                Log.e("DEBUG", "Error al obtener el ID: " + message);
            }
        };
        if (usuario == null) {
            obtenerUsuario(callback);
        }
        return idUsuario;
    }

    public void obtenerUsuario(RepositoryCallback<Usuario> callback) {
        // 1. Intentamos obtenerlo de SharedPreferences primero (es más rápido)
        int idCacheado = preferences.getInt("idUsuario", -1);

        // 2. Si no está en caché, lo pedimos a la API de forma asíncrona (.enqueue)
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            apiUsuario.getIdUsuarioPorCorreo(user.getEmail()).enqueue(new Callback<Usuario>() {
                @Override
                public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Usuario usuario = response.body();
                        idUsuario = usuario.getId();
                        // Guardamos en SharedPreferences para la próxima vez
                        preferences.edit().putInt("idUsuario", usuario.getId()).apply();
                        callback.onComplete(usuario);
                    } else {
                        callback.onError("No se encontró el ID de usuario");
                    }
                }

                @Override
                public void onFailure(Call<Usuario> call, Throwable t) {
                    callback.onError(t.getMessage());
                }
            });
        } else {
            callback.onError("No hay usuario autenticado");
        }
    }

    public boolean isUserLoggedIn() {
        Log.d("Iniciando sesion", "isUserLoggedIn: " + preferences.getBoolean("logeado", false) + ". Current user: " + mAuth.getCurrentUser());
        return mAuth.getCurrentUser() != null && preferences.getBoolean("logeado", false);
    }

    public boolean isAdmin() {
        return preferences.getBoolean("esAdmin", false);
    }

    public void signIn(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(mAuth.getCurrentUser());
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }

    public void registrar(String email, String password, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) callback.onSuccess(mAuth.getCurrentUser());
                    else callback.onError(task.getException());
                });
    }

    public void guardarUsuarioEnSQL(String email, UserRepository.RepositoryCallback<Usuario> callback) {
        Usuario nuevoUsuario = new Usuario(0, email);
        apiUsuario.guardarUsuario(nuevoUsuario).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful()) callback.onComplete(response.body());
                else callback.onError("Error al guardar en BD");
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Añadir esto a data/repository/AuthRepository.java
    public void resetearPassword(String email, RepositoryCallback<Void> callback) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onComplete(null);
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }
/*
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
    }*/

    public void enviarCorreoBienvenida(String emailUsuario) {
        apiUsuario.enviarCorreoBienvenida(emailUsuario).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // TODO PARA RESPONDER CUANDO SE ENVIE BIEN EL CORREO
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // TODO PARA RESPONDER CUANDO NO SE ENVIE BIEN EL CORREO
            }
        });
    }

    public void saveSession(boolean isAdmin, String email, int idUsuario) {
        preferences.edit()
                .putBoolean("logeado", true)
                .putBoolean("esAdmin", isAdmin)
                .putString("correoUsuario", email)
                .putInt("idUsuario", idUsuario)
                .commit();
    }

    public void logout() {
        mAuth.signOut();
        preferences.edit().clear().commit(); // Usamos commit para asegurar la limpieza inmediata
    }

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);

        void onError(Exception e);
    }

    public interface RepositoryCallback<T> {
        void onComplete(T result);

        void onError(String message);
    }
}