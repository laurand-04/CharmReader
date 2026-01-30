package com.tfg.charmreader.interfacesAPI;

import com.tfg.charmreader.objetosBD.Votacion;
import retrofit2.Call;
import retrofit2.http.*;

public interface I_APIVotacion {
    @POST("votaciones/votar")
    Call<Votacion> votar(@Body Votacion votacion);

    @GET("votaciones/resultado/{idGrupo}/{idBook}")
    Call<Long> verVotosPositivos(@Path("idGrupo") int idGrupo, @Path("idBook") int idBook);
}
