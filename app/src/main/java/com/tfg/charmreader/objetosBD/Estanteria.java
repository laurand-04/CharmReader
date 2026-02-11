package com.tfg.charmreader.objetosBD;

import com.google.gson.annotations.SerializedName;

public class Estanteria {

    @SerializedName("id")
    private int id;

    @SerializedName("idUsuario")
    private int idU;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("color")
    private String color;

    private int cantidadLibros = 0;

    public Estanteria(){}

    public Estanteria(int idU, String nombre, String color) {
        this.idU = idU;
        this.nombre = nombre;
        this.color = color;
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

    public int getCantidadLibros() { return cantidadLibros; }
    public void setCantidadLibros(int cantidadLibros) { this.cantidadLibros = cantidadLibros; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
