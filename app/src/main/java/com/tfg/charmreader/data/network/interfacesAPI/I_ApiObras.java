package com.tfg.charmreader.data.network.interfacesAPI;

import com.tfg.charmreader.data.model.Obras;

import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface I_ApiObras {

    @POST("obras/guardar")
    Call<Obras> guardarObra(@Body Obras nuevaObra);

    @GET("obras/usuario/{idUsuario}")
    Call<List<Obras>> obtenerObrasDeUsuario(@Path("idUsuario") int idUsuario);

    @DELETE("obras/eliminar/{id}")
    Call<ResponseBody> eliminarObra(@Path("id") int idObra);
}