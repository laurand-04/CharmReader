package com.tfg.charmreader.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

public class Obras implements Serializable {

    @SerializedName("id")
    private Integer id;

    // Usamos "usuario" porque así lo serializa Spring Boot por tus getters/setters
    @SerializedName("idUsuario")
    private int idUsuario;

    @SerializedName("idLibro")
    private int idLibro;

    @SerializedName("ruta")
    private String ruta;

    @SerializedName("autor")
    private String autor;

    @SerializedName("finalizado")
    private boolean finalizado;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("url_imagen")
    private String url_imagen;

    @SerializedName("sinopsis")
    private String sinopsis;

    @SerializedName("fecha_ultima_modificacion")
    private Date fecha_ultima_modificacion;

    // Constructor vacío requerido por Retrofit/Gson
    public Obras() {
    }

    public Obras(int idUsuario, String ruta, boolean finalizado, String nombre, String url_imagen, Date fecha_ultima_modificacion, String autor, String sinopsis) {
        this.idUsuario = idUsuario;
        this.ruta = ruta;
        this.finalizado = finalizado;
        this.nombre = nombre;
        this.url_imagen = url_imagen;
        this.fecha_ultima_modificacion = fecha_ultima_modificacion;
        this.autor = autor;
        this.idLibro = -1;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public boolean getFinalizado() {
        return finalizado;
    }

    public void setFinalizado(boolean finalizado) {
        this.finalizado = finalizado;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUrl_imagen() {
        return url_imagen;
    }

    public void setUrl_imagen(String url_imagen) {
        this.url_imagen = url_imagen;
    }

    public Date getFecha_ultima_modificacion() {
        return fecha_ultima_modificacion;
    }

    public void setFecha_ultima_modificacion(Date fecha_ultima_modificacion) {
        this.fecha_ultima_modificacion = fecha_ultima_modificacion;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getSinopsis() {
        return sinopsis;
    }

    public void setSinopsis(String sinopsis) {
        this.sinopsis = sinopsis;
    }

    public int getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(int idLibro) {
        this.idLibro = idLibro;
    }
}
