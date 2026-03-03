package com.tfg.charmreader.data.network.interfacesAPI;

import com.tfg.charmreader.data.model.Votacion; // Asegúrate de tener este objeto
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface I_ApiVotacion {

    @POST("votaciones/alternar")
    Call<Map<String, String>> alternarVoto(@Body Votacion votacion);

    @GET("votaciones/estado/{idUsuario}/{idGrupo}/{idBook}")
    Call<Boolean> comprobarEstado(
            @Path("idUsuario") int idUsuario,
            @Path("idGrupo") int idGrupo,
            @Path("idBook") int idBook
    );

    @GET("votaciones/resultado/{idGrupo}/{idBook}")
    Call<Long> obtenerConteoVotos(
            @Path("idGrupo") int idGrupo,
            @Path("idBook") int idBook
    );
}