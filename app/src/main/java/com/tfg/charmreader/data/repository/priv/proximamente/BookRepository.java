package com.tfg.charmreader.data.repository.priv.proximamente;

import com.tfg.charmreader.data.model.BookEn;
import com.tfg.charmreader.data.network.API.API;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiBook;

import java.util.List;
import retrofit2.Callback;

public class BookRepository {
    private final I_ApiBook api;

    public BookRepository() {
        this.api = API.getInstancia().create(I_ApiBook.class);
    }

    public void anadirBook(BookEn book, Callback<BookEn> cb) {
        api.anadirBook(book).enqueue(cb);
    }

    public void obtenerTodosLosBooks(Callback<List<BookEn>> cb) {
        api.obtenerTodosLosBooks().enqueue(cb);
    }

    public void obtenerBookPorId(int id, Callback<BookEn> cb) {
        api.obtenerBookPorId(id).enqueue(cb);
    }

    public void obtenerBooksPorUsuario(int usuarioId, Callback<List<BookEn>> cb) {
        api.obtenerBooksPorUsuario(usuarioId).enqueue(cb);
    }

    public void eliminarBook(int id, Callback<Boolean> cb) {
        api.eliminarBook(id).enqueue(cb);
    }
}