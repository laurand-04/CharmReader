package com.tfg.charmreader.interfacesAPI;

import com.tfg.charmreader.objetosBD.Usuario;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface I_ApiUsuario {
    @GET("/usuarios/correo/{correo}")
    Call<Usuario> getIdUsuarioPorCorreo(@Path("correo") String correo);

    @GET("/usuarios/id-maximo")
    Call<Integer> obtenerIdMaximoUsuario();

    @GET("usuarios/obtenerTodos")
    Call<List<Usuario>> obtenerUsuarios();

    @POST("/usuarios")
    Call<Usuario> guardarUsuario(@Body Usuario usuario);

    @DELETE("usuarios/eliminar/{id}")
    Call<Void> eliminarUsuario(@Path("id") int id);
}
