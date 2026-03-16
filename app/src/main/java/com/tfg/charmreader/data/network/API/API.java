package com.tfg.charmreader.data.network.API;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class API {
    private static Retrofit retrofit;

    private API(){
        retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.0.28:2025/") // IP de mi API
                .addConverterFactory(GsonConverterFactory.create())
                .build();

    }

    public static synchronized Retrofit getInstancia() {
        if (retrofit == null) {
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd")
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl("http://192.168.0.28:2025/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}
