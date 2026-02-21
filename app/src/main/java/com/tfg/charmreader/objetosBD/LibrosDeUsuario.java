package com.tfg.charmreader.objetosBD;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

public class LibrosDeUsuario implements Serializable {
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
    private double valoracion;
    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("fechaInicio")
    private Date fechaInicio;

    @SerializedName("fechaFin")
    private Date fechaFin;

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

    public LibrosDeUsuario(CCLibrosDeUsuario id, int capituloActual, String rutaEpub, float scroll, int idEstanteria, double valoracion, String descripcion) {
        this.id = id;
        this.capitulo = capituloActual;
        this.ruta = rutaEpub;
        this.scroll = scroll;
        this.idEstanteria = idEstanteria;
        this.valoracion = valoracion;
        this.descripcion = descripcion;
    }

    public LibrosDeUsuario(CCLibrosDeUsuario id, int capitulo, String ruta, float scroll, int idEstanteria, double valoracion, String descripcion, Date  fechaInicio, Date fechaFin) {
        this.id = id;
        this.capitulo = capitulo;
        this.ruta = ruta;
        this.scroll = scroll;
        this.idEstanteria = idEstanteria;
        this.valoracion = valoracion;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
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

    public void setValoracion(double valoracion) {
        this.valoracion = valoracion;
    }

    public double getValoracion() {
        return valoracion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getIdEstanteria() {
        return idEstanteria;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }
}
