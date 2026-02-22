package com.tfg.charmreader.interfacesAPI;

import retrofit2.Call;
import retrofit2.http.GET;

public interface I_ApiAdmin {
    @GET("admin/usuarios-total")
    Call<Long> getTotalUsuarios();

    @GET("admin/lecturas-activas")
    Call<Long> getLecturasActivas();

    @GET("admin/grupo-top")
    Call<String> getNombreGrupoTop();

    @GET("admin/densidad-participacion")
    Call<Double> getDensidad();

    @GET("admin/tiempo-medio")
    Call<Double> getTiempoMedioLectura();

    @GET("admin/finalizados-mes")
    Call<Long> getLibrosFinalizadosMes();
}
