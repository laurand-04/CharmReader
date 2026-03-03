package com.tfg.charmreader.data.network.interfacesAPI;

import com.tfg.charmreader.data.model.BookResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface I_OL_LibroService {
    @GET("search.json")
    Call<BookResponse> buscarLibroPorTitulo(@Query("q") String query);
}
