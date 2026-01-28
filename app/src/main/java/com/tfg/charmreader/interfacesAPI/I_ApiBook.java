package com.tfg.charmreader.interfacesAPI;

import com.tfg.charmreader.objetosBD.BookEn;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface I_ApiBook {

    @POST("/books/nuevo")
    Call<BookEn> anadirBook(@Body BookEn book);

    @GET("/books/todos")
    Call<List<BookEn>> obtenerTodosLosBooks();

    @GET("/books/{id}")
    Call<BookEn> obtenerBookPorId(@Path("id") int id);

    @GET("/books/usuario/{usuarioId}")
    Call<List<BookEn>> obtenerBooksPorUsuario(@Path("usuarioId") int usuarioId);
}