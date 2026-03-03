package com.tfg.charmreader.data.repository;

import android.content.Context;
import android.net.Uri;

import com.cloudinary.android.callback.UploadCallback;
import com.tfg.charmreader.data.model.Usuario;
import com.tfg.charmreader.data.network.API.API;
import com.tfg.charmreader.data.network.API.CloudinaryClient;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiLibro;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiLibrosDeUsuario;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiUsuario;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class UserRepository {
    private final I_ApiUsuario apiUsuario;

    // Con Inyección de Dependencias, esto nos lo daría Hilt
    public UserRepository(Context context) {
        this.apiUsuario = API.getInstancia().create(I_ApiUsuario.class);
        CloudinaryClient.init(context);
    }

    // Añadir estos métodos a data/repository/UserRepository.java
    public void obtenerTodosLosUsuarios(Callback<List<Usuario>> callback) {
        apiUsuario.obtenerUsuarios().enqueue(callback);
    }

    public void eliminarUsuario(int id, Callback<Void> callback) {
        apiUsuario.eliminarUsuario(id).enqueue(callback);
    }

    public void obtenerUsuarioPorEmail(String email, RepositoryCallback<Usuario> callback) {
        apiUsuario.getIdUsuarioPorCorreo(email).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful()) callback.onComplete(response.body());
                else callback.onError("Error en la respuesta");
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void obtenerUsuario(String email, Callback<Usuario> callback) {
        apiUsuario.getIdUsuarioPorCorreo(email).enqueue(callback);
    }

    public void actualizarUsuario(Usuario usuario, Callback<Usuario> callback) {
        apiUsuario.guardarUsuario(usuario).enqueue(callback);
    }

    public void subirImagen(Uri uri, UploadCallback callback) {
        CloudinaryClient.nuevoUpload(uri).callback(callback).dispatch();
    }

    public interface RepositoryCallback<T> {
        void onComplete(T result);
        void onError(String message);
    }
}