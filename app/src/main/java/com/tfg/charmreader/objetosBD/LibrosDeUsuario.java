package com.tfg.charmreader.objetosBD;

import com.google.gson.annotations.SerializedName;

public class LibrosDeUsuario {
    @SerializedName("id")
    private CCLibrosDeUsuario id;
    @SerializedName("capitulo")
    private int capitulo;
    @SerializedName("ruta")
    private String ruta;

    public LibrosDeUsuario(CCLibrosDeUsuario id, int capituloActual, String rutaEpub) {
        this.id = id;
        this.capitulo = capituloActual;
        this.ruta = rutaEpub;
    }

    public CCLibrosDeUsuario getId() {
        return id;
    }

    public void setId(CCLibrosDeUsuario id) {
        this.id = id;
    }

    public int getCapitulo() {
        return capitulo;
    }

    public void setCapitulo(int capitulo) {
        this.capitulo = capitulo;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }
}
