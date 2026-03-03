package com.tfg.charmreader.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CCLibrosSinEstrenar implements Serializable {
    @SerializedName("idu")
    private int idu;
    @SerializedName("nombre")
    private String nombre;

    public CCLibrosSinEstrenar(int idu, String idlibro) {
        this.idu = idu;
        this.nombre = idlibro;
    }

    public int getIdu() {
        return idu;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
