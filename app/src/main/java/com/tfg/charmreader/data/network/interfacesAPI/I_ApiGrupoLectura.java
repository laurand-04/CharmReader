package com.tfg.charmreader.data.network.interfacesAPI;

import com.tfg.charmreader.data.model.GrupoLectura;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface I_ApiGrupoLectura {
    @POST("/grupo/nuevo")
    Call<GrupoLectura> crearGrupo (@Body GrupoLectura grupo);
    @GET("/grupo/todos")
    Call<List<GrupoLectura>> obtenerGrupos();
    @GET("/grupo/usuario/{id}")
    Call<List<GrupoLectura>> obtenerGruposPorAdmin(@Path("id") int id);

    @GET("/grupo/buscarNombre/{nombre}")
    Call<GrupoLectura> buscarGrupoPorNombre(@Path("nombre") String nombre);

    @GET("/grupo/buscarId/{id}")
    Call<GrupoLectura> obtenerGrupoPorId(@Path("id") int idGrupo);

    @PUT("/grupo/actualizar")
    Call<GrupoLectura> actualizar(@Body GrupoLectura grupo);

    @PUT("/grupo/salir-admin/{idGrupo}/{idAdmin}")
    Call<okhttp3.ResponseBody> gestionarSalidaAdmin(@Path("idGrupo") int idGrupo, @Path("idAdmin") int idAdmin);

    @DELETE("/grupo/eliminar/{id}")
    Call<Void> eliminarGrupo(@Path("id") int idGrupo);
}
