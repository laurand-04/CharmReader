package com.tfg.charmreader.objetosBD;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

public class LibrosSinEstrenar implements Serializable {
    @SerializedName("id")
    private CCLibrosSinEstrenar id;
    @SerializedName("fechaPublicacion")
    private Date fechaPublicacion;
    @SerializedName("autor")
    private String autor;

    public CCLibrosSinEstrenar getId() {
        return id;
    }

    public void setId(CCLibrosSinEstrenar id) {
        this.id = id;
    }
    public Date getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(Date fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }


}

