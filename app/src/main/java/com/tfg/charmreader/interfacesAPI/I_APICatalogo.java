package com.tfg.charmreader.interfacesAPI;

import com.tfg.charmreader.objetosBD.CatalogoLectura;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface I_APICatalogo {
    @POST("catalogo/añadir")
    Call<CatalogoLectura> añadirLibro(@Body CatalogoLectura lectura);

    @GET("catalogo/grupo/{idGrupo}")
    Call<List<CatalogoLectura>> verCatalogo(@Path("idGrupo") int idGrupo);
}
