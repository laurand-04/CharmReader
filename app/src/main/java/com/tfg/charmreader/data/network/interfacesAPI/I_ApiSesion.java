package com.tfg.charmreader.data.network.interfacesAPI;

import com.tfg.charmreader.data.model.Sesion;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface I_ApiSesion {
    @POST("sesiones/nueva")
    Call<Sesion> nuevaSesion(@Body Sesion sesion);

    @GET("sesiones/grupo/{idGrupo}")
    Call<List<Sesion>> verSesiones(@Path("idGrupo") int idGrupo);

    @GET("sesiones/grupo/{idGrupo}/proxima")
    Call<Sesion> obtenerProximaSesion(@Path("idGrupo") int idGrupo);

    @GET("sesiones/contar/{idGrupo}/{inicio}/{fin}")
    Call<Long> contarSesionesPorRango(
            @Path("idGrupo") int idGrupo,
            @Path("inicio") String fechaInicio, // Formato "yyyy-MM-dd"
            @Path("fin") String fechaFin
    );

    @DELETE("sesiones/eliminar/{idGrupo}/{fecha}/{hora}")
    Call<Void> eliminarSesion(
            @Path("idGrupo") int idGrupo,
            @Path("fecha") String fechaFormateada,
            @Path("hora") String hora
    );
}
