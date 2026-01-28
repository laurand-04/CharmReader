package com.tfg.charmreader.objetosBD;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BookResponse {
    // Le decimos a GSON que busque la clave "docs" en el JSON
    @SerializedName("docs")
    private List<Book> books;

    // Este es el método que llamas en el MainActivity
    public List<Book> getBooks() {
        return books;
    }
}
