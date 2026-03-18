package com.tfg.charmreader.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class ObrasModel {

    @SerializedName("id")
    private Integer id;

    // Usamos "usuario" porque así lo serializa Spring Boot por tus getters/setters
    @SerializedName("usuario")
    private int idUsuario;

    @SerializedName("ruta")
    private String ruta;

    @SerializedName("finalizado")
    private boolean finalizado;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("url_imagen")
    private String url_imagen;

    @SerializedName("fecha_ultima_modificacion")
    private Date fecha_ultima_modificacion;

    // Constructor vacío requerido por Retrofit/Gson
    public ObrasModel() {
    }

    public ObrasModel(int idUsuario, String ruta, boolean finalizado, String nombre, String url_imagen, Date fecha_ultima_modificacion) {
        this.idUsuario = idUsuario;
        this.ruta = ruta;
        this.finalizado = finalizado;
        this.nombre = nombre;
        this.url_imagen = url_imagen;
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
}
