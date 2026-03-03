package com.tfg.charmreader.data.repository.priv.proximamente;

import com.tfg.charmreader.data.model.BookResponse;
import com.tfg.charmreader.data.network.API.APIexterna;

import retrofit2.Callback;

public class SearchRepository {

    public void buscarEnOpenLibrary(String query, Callback<BookResponse> cb) {
        APIexterna.getLibroService().buscarLibroPorTitulo(query).enqueue(cb);
    }

}