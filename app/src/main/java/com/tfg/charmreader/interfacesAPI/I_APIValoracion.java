package com.tfg.charmreader.interfacesAPI;

import com.tfg.charmreader.objetosBD.Valoracion;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface I_APIValoracion {
    @POST("valoraciones/nueva")
    Call<Valoracion> crear(@Body Valoracion valoracion);

    @GET("valoraciones/{tipo}/{id}")
    Call<List<Valoracion>> verValoraciones(
            @Path("tipo") Valoracion.TipoValoracion tipo,
            @Path("id") int id);
}
