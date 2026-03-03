package com.tfg.charmreader.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BookResponse {
    // Le decimos a GSON que busque la clave "docs" en el JSON
    @SerializedName("docs")
    private List<Book> books;

    // Este es el métod0 que llamas en el MainActivity
    public List<Book> getBooks() {
        return books;
    }
}
