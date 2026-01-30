package com.tfg.charmreader.interfacesAPI;

import com.tfg.charmreader.objetosBD.GrupoLectura;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface I_ApiGrupoLectura {
    @POST("/grupo/nuevo")
    Call<GrupoLectura> crearGrupo (@Body GrupoLectura grupo);
    @GET("/grupo/todos")
    Call<List<GrupoLectura>> obtenerGrupos();
    @GET("/grupo/usuario/{id}")
    Call<List<GrupoLectura>> obtenerGruposDeUsuario(@Path("id") int idUsuario);

    @GET("/grupo/buscar/{nombre}")
    Call<GrupoLectura> buscarGrupoPorNombre(@Path("nombre") String nombre);
}
