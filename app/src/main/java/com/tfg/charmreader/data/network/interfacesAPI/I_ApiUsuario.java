package com.tfg.charmreader.data.network.interfacesAPI;

import com.tfg.charmreader.data.model.Usuario;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface I_ApiUsuario {
    @GET("/usuarios/correo/{correo}")
    Call<Usuario> getIdUsuarioPorCorreo(@Path("correo") String correo);

    @GET("/usuarios/id/{id}")
    Call<Usuario> obtenerUsuarioPorId(@Path("id") int id);

    @GET("/usuarios/id-maximo")
    Call<Integer> obtenerIdMaximoUsuario();

    @GET("/usuarios/obtenerTodos")
    Call<List<Usuario>> obtenerUsuarios();

    @POST("/usuarios")
    Call<Usuario> guardarUsuario(@Body Usuario usuario);

    @POST("/usuarios/enviar-bienvenida")
    Call<Void> enviarCorreoBienvenida(@Query("email") String email);

    @DELETE("/usuarios/eliminar/{id}")
    Call<Void> eliminarUsuario(@Path("id") int id);
}
