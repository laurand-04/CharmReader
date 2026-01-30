package com.tfg.charmreader.interfacesAPI;

import com.tfg.charmreader.objetosBD.Sesion;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface I_APISesion {
    @POST("sesiones/nueva")
    Call<Sesion> nuevaSesion(@Body Sesion sesion);

    @GET("sesiones/grupo/{idGrupo}")
    Call<List<Sesion>> verSesiones(@Path("idGrupo") int idGrupo);
}
