package com.tfg.charmreader.data.model;

import java.io.Serializable;

public class Usuario implements Serializable {
    private Integer id=null;
    private String correo;
    private String nombre;
    private String foto;

    // Constructor vacío requerido por Retrofit/Gson para la deserialización
    public Usuario() {
    }

    public Usuario(int id, String correo) {
        this.correo = correo;
    }

    // Constructor completo para facilitar la creación de objetos
    public Usuario(Integer id, String correo, String nombre, String foto) {
        this.id = id;
        this.correo = correo;
        this.nombre = nombre;
        this.foto = foto;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }
}

