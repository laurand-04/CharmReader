package com.tfg.charmreader.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;

public class Sesion implements Serializable {
    @SerializedName("idGrupo")
    private int idGrupo;

    @SerializedName("fecha")
    private Date fecha; // LocalDate en API

    @SerializedName("hora")
    private String hora; // LocalTime en API

    @SerializedName("capituloInicio")
    private int capituloInicio;

    @SerializedName("capituloFinalizacion")
    private int capituloFinalizacion;

    public int getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(int idGrupo) {
        this.idGrupo = idGrupo;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public int getCapituloInicio() {
        return capituloInicio;
    }

    public void setCapituloInicio(int capituloInicio) {
        this.capituloInicio = capituloInicio;
    }

    public int getCapituloFinalizacion() {
        return capituloFinalizacion;
    }

    public void setCapituloFinalizacion(int capituloFinalizacion) {
        this.capituloFinalizacion = capituloFinalizacion;
    }
}