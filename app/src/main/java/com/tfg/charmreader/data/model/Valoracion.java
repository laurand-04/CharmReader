package com.tfg.charmreader.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Valoracion implements Serializable {
    @SerializedName("idValoracion")
    private int idValoracion;

    @SerializedName("idUsuario")
    private int idUsuario;

    @SerializedName("idGrupo")
    private int idGrupo;

    @SerializedName("calificacion")
    private int calificacion;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("tipo")
    private TipoValoracion tipo;

    @SerializedName("idReferencia")
    private int idReferencia; // int en API

    public enum TipoValoracion implements Serializable {
        @SerializedName("GRUPO") GRUPO,
        @SerializedName("LIBRO") LIBRO
    }

    public int getIdValoracion() {
        return idValoracion;
    }

    public void setIdValoracion(int idValoracion) {
        this.idValoracion = idValoracion;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(int calificacion) {
        this.calificacion = calificacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public TipoValoracion getTipo() {
        return tipo;
    }

    public void setTipo(TipoValoracion tipo) {
        this.tipo = tipo;
    }

    public int getIdReferencia() {
        return idReferencia;
    }

    public void setIdReferencia(int idReferencia) {
        this.idReferencia = idReferencia;
    }

    public int getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(int idGrupo) {
        this.idGrupo = idGrupo;
    }
}