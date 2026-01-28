package com.tfg.charmreader.objetosBD;

import com.tfg.charmreader.interfacesAPI.I_OL_LibroService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class APIexterna {
    private static final String BASE_URL = "https://openlibrary.org/";
    private static Retrofit retrofit = null;

    public static I_OL_LibroService getLibroService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(I_OL_LibroService.class);
    }
}
