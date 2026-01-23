package com.tfg.charmreader.objetosBD;

import com.google.gson.annotations.SerializedName;

public class Estanteria {

    @SerializedName("id")
    private int id;

    @SerializedName("idUsuario")
    private int idU;

    @SerializedName("nombre")
    private String nombre;

    public Estanteria(){}

    public Estanteria(int idU, String nombre) {
        this.idU = idU;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdU() {
        return idU;
    }

    public void setIdU(int idU) {
        this.idU = idU;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
