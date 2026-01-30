package com.tfg.charmreader.objetosBD;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;

public class CatalogoLectura implements Serializable {
    @SerializedName("idGrupo")
    private int idGrupo;

    @SerializedName("idBook")
    private int idBook; // int en API

    @SerializedName("estado")
    private EstadoLectura estado;

    @SerializedName("fechaComienzo")
    private Date fechaComienzo; // Date en API

    @SerializedName("fechaFinalizacion")
    private Date fechaFinalizacion; // Date en API

    public enum EstadoLectura implements Serializable {
        @SerializedName("PROPUESTO") PROPUESTO,
        @SerializedName("ACTUAL") ACTUAL,
        @SerializedName("FINALIZADO") FINALIZADO
    }

    public CatalogoLectura() {
    }

    public int getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(int idGrupo) {
        this.idGrupo = idGrupo;
    }

    public int getIdBook() {
        return idBook;
    }

    public void setIdBook(int idBook) {
        this.idBook = idBook;
    }

    public EstadoLectura getEstado() {
        return estado;
    }

    public void setEstado(EstadoLectura estado) {
        this.estado = estado;
    }

    public Date getFechaComienzo() {
        return fechaComienzo;
    }

    public void setFechaComienzo(Date fechaComienzo) {
        this.fechaComienzo = fechaComienzo;
    }

    public Date getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(Date fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }
}