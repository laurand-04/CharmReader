package com.tfg.charmreader.objetosBD;

public class Usuario {
    private Integer id=null;
    private String correo;

    public Usuario(int id, String correo) {
        this.correo = correo;
    }

    public int getId() {
        return id;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}

