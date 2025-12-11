package com.tfg.charmreader.interfacesAPI;

import com.tfg.charmreader.objetosBD.Usuario;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface I_ApiUsuario {
    @GET("/usuarios/correo/{correo}")
    Call<Usuario> getIdUsuarioPorCorreo(@Path("correo") String correo);

    @GET("/usuarios/id-maximo")
    Call<Integer> obtenerIdMaximoUsuario();

    @POST("/usuarios")
    Call<Usuario> guardarUsuario(@Body Usuario usuario);
}
