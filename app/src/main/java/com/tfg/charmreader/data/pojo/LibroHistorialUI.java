package com.tfg.charmreader.data.pojo;

import android.util.Log;

import com.tfg.charmreader.data.model.BookEn;
import com.tfg.charmreader.data.model.CatalogoLectura;

public class LibroHistorialUI {
    private BookEn libro;
    private CatalogoLectura catalogo;
    private double mediaValoracion;
    private int numSesiones;

    public LibroHistorialUI(BookEn libro, CatalogoLectura catalogo, double mediaValoracion, int numSesiones) {
        this.libro = libro;
        this.catalogo = catalogo;
        this.mediaValoracion = mediaValoracion;
        this.numSesiones = numSesiones;
        Log.d("LibroHistorialUI", "Sesiones contadas: " + numSesiones);
    }

    // Getters
    public BookEn getLibro() { return libro; }
    public CatalogoLectura getCatalogo() { return catalogo; }
    public double getMediaValoracion() { return mediaValoracion; }
    public int getNumSesiones() { return numSesiones; }
}
