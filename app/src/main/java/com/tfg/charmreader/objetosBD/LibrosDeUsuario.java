package com.tfg.charmreader.objetosBD;

import com.google.gson.annotations.SerializedName;

public class LibrosDeUsuario {
    @SerializedName("id")
    private CCLibrosDeUsuario id;
    @SerializedName("capitulo")
    private int capitulo;
    @SerializedName("ruta")
    private String ruta;
    @SerializedName("scroll")
    private float scroll;
    @SerializedName("idEstanteria")
    private int idEstanteria;
    @SerializedName("valoracion")
    private int valoracion;
    @SerializedName("descripcion")
    private String descripcion;

    public LibrosDeUsuario(CCLibrosDeUsuario id, int capituloActual, String rutaEpub) {
        this.id = id;
        this.capitulo = capituloActual;
        this.ruta = rutaEpub;
    }

    public LibrosDeUsuario(CCLibrosDeUsuario id, int capituloActual, String rutaEpub, float scroll) {
        this.id = id;
        this.capitulo = capituloActual;
        this.ruta = rutaEpub;
        this.scroll = scroll;
    }

    public LibrosDeUsuario(CCLibrosDeUsuario id, int capituloActual, String rutaEpub, float scroll, int idEstanteria, int valoracion, String descripcion) {
        this.id = id;
        this.capitulo = capituloActual;
        this.ruta = rutaEpub;
        this.scroll = scroll;
        this.idEstanteria = idEstanteria;
        this.valoracion = valoracion;
        this.descripcion = descripcion;
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

    public void setScroll(float scroll) {
        this.scroll = scroll;
    }
    public float getScroll() {
        return scroll;
    }



    public void setIdEstanteria(int idEstanteria) {
        this.idEstanteria = idEstanteria;
    }

    public void setValoracion(int valoracion) {
        this.valoracion = valoracion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
