package com.tfg.charmreader.interfacesAPI;

import com.tfg.charmreader.objetosBD.LibrosDeUsuario;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface I_ApiLibrosDeUsuario {
    @POST("/libros_de_usuarios/progreso")
    Call<LibrosDeUsuario> guardarProgreso(@Body LibrosDeUsuario librosDeUsuario);

    @GET("/libros_de_usuarios/usuario/{idUsuario}")
    Call<List<LibrosDeUsuario>> obtenerLibrosDeUsuario(@Path("idUsuario") int idUsuario);

    @GET("/libros_de_usuarios/usuario/{idUsuario}/libro/{idLibro}")
    Call<LibrosDeUsuario> getLibrodeUsuario(@Path("idUsuario") int idUsuario, @Path("idLibro") int idLibro);

    @GET("/libros_de_usuarios/estanteria/{idEstanteria}")
    Call<List<LibrosDeUsuario>> obtenerLibrosDeEstanteria(@Path("idEstanteria") int idEstanteria);

    @GET("/libros_de_usuarios/contarPorEstanteria/{idEstanteria}")
    Call<Integer> contarLibrosEnEstanteria(@Path("idEstanteria") int idEstanteria);

    @PUT("/libros_de_usuarios/usuario/{idU}/libro/{idL}/estanteria/{idEst}")
    Call<Boolean> asignarLibroAEstanteria(@Path("idU") int idUsuario, @Path("idL") int idLibro, @Path("idEst") int idEstanteria);
}
