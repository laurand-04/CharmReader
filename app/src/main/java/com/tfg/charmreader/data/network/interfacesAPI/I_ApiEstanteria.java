package com.tfg.charmreader.data.network.interfacesAPI;

import com.tfg.charmreader.data.model.Estanteria;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface I_ApiEstanteria {
    @POST("/estanterias/nueva")
    Call<Estanteria> anadirEstanteria(@Body Estanteria estanteria);

    @GET("/estanterias/id/{id}")
    Call<Estanteria> obtenerEstanteriaPorId(@Path("id") int id);

    @GET("/estanterias/usuario/{idUsuario}")
    Call<List<Estanteria>> obtenerEstanteriasDeUsuario(@Path("idUsuario") int idUsuario);

    @DELETE("/estanterias/eliminar/{id}")
    Call<Boolean> eliminarEstanteria(@Path("id") int id);
}
