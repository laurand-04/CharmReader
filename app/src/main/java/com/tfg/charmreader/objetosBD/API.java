package com.tfg.charmreader.objetosBD;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class API {
    private static Retrofit retrofit;

    private API(){
        retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.67:2025/") // IP de mi API
                .addConverterFactory(GsonConverterFactory.create())
                .build();

    }

    public static synchronized Retrofit getInstancia() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("http://192.168.1.67:2025/") // IP de mi API
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
