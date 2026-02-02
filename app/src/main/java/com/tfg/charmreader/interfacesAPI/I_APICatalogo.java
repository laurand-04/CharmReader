package com.tfg.charmreader.interfacesAPI;

import com.tfg.charmreader.objetosBD.BookEn;
import com.tfg.charmreader.objetosBD.CatalogoLectura;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface I_APICatalogo {
    @POST("catalogo/añadir")
    Call<CatalogoLectura> añadirLibro(@Body CatalogoLectura lectura);

    @GET("catalogo/grupo/{idGrupo}")
    Call<List<CatalogoLectura>> verCatalogo(@Path("idGrupo") int idGrupo);

    @GET("catalogo/grupo/{idGrupo}/actual")
    Call<BookEn> obtenerLibroActual(@Path("idGrupo") int idGrupo);

    @GET("catalogo/grupo/{idGrupo}/propuestas")
    Call<List<BookEn>> obtenerLibroPropuestas(@Path("idGrupo") int idGrupo);

    @GET("catalogo/grupo/{idGrupo}/historial")
    Call<List<BookEn>> obtenerHistorial(@Path("idGrupo") int idGrupo);

    @GET("catalogo/grupo2/{idGrupo}/historial")
    Call<List<CatalogoLectura>> obtenerHistorial2(@Path("idGrupo") int idGrupo);
}
