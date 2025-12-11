package com.tfg.charmreader.interfacesAPI;

import com.tfg.charmreader.objetosBD.Libro;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface I_ApiLibro {
    @POST("/libros/nuevo")
    Call<Libro> añadirLibro(@Body Libro libro);

    @GET("/libros/varios")
    Call<List<Libro>> obtenerLibrosPorIds(@Query("ids") List<Integer> ids);
}
