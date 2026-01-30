package com.tfg.charmreader.interfacesAPI;

import com.tfg.charmreader.objetosBD.Miembro;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface I_APIMiembro {
    @POST("miembros/unirse")
    Call<Miembro> unirse(@Body Miembro miembro);

    @GET("miembros/grupo/{idGrupo}")
    Call<List<Miembro>> obtenerPorGrupo(@Path("idGrupo") int idGrupo);

    @GET("miembros/usuario/{idUsuario}")
    Call<List<Miembro>> obtenerGruposDeUsuario(@Path("idUsuario") int idUsuario);

    @DELETE("miembros/salir")
    Call<Void> salirDeGrupo(@Query("idGrupo") int idGrupo, @Query("idUsuario") int idUsuario);

    @GET("miembros/grupo/{idGrupo}/contar")
    Call<Long> contarMiembros(@Path("idGrupo") int idGrupo);
}