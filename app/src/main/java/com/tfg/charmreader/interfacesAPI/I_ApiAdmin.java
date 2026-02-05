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
}
