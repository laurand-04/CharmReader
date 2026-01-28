package com.tfg.charmreader.interfacesAPI;

import com.tfg.charmreader.objetosBD.CCLibrosSinEstrenar;
import com.tfg.charmreader.objetosBD.LibrosSinEstrenar;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface I_ApiLibrosSinEstrenar {
    @GET("/libros_sin_estrenar/usuario/{id}")
    Call<ArrayList<LibrosSinEstrenar>> getLibrosSinEstrenarPorUsuario(@Path("id") int id);

    @POST("/libros_sin_estrenar/nuevo")
    Call<LibrosSinEstrenar> guardarLibrosSinEstrenar(@Body LibrosSinEstrenar librosSinEstrenar);

    @PUT("/libros_sin_estrenar/{idu}/{nombre}")
    Call<LibrosSinEstrenar> actualizarLibrosSinEstrenar(@Body LibrosSinEstrenar librosSinEstrenar, @Path("idu") int idu, @Path("nombre") String nombre);

    @DELETE("/libros_sin_estrenar")
    Call<String> eliminarLibrosSinEstrenar(@Body CCLibrosSinEstrenar cclibrosSinEstrenar);
}
