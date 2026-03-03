package com.tfg.charmreader.data.network.interfacesAPI;

import com.tfg.charmreader.data.model.LibrosDeUsuario;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
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

    @GET("/libros_de_usuarios/estanteria/{idEstanteria}/usuario/{idUsuario}")
    Call<List<LibrosDeUsuario>> obtenerLibrosDeEstanteria(@Path("idEstanteria") int idEstanteria, @Path("idUsuario")int idUsuario);

    @GET("/libros_de_usuarios/contarPorEstanteria/{idEstanteria}")
    Call<Integer> contarLibrosEnEstanteria(@Path("idEstanteria") int idEstanteria);

    @PUT("/libros_de_usuarios/usuario/{idU}/libro/{idL}/estanteria/{idEst}")
    Call<Boolean> asignarLibroAEstanteria(@Path("idU") int idUsuario, @Path("idL") int idLibro, @Path("idEst") int idEstanteria);

    @PUT("/libros_de_usuarios/usuario/{idU}/libro/{idL}/estanteria/0")
    Call<Boolean> desvincularLibroDeEstanteria(@Path("idU") int idU, @Path("idL") int idL);

    @DELETE("/libros_de_usuarios/eliminar/{idU}/{idL}")
    Call<ResponseBody> eliminarLibro(@Path("idU") int idU, @Path("idL") int idL);
}
