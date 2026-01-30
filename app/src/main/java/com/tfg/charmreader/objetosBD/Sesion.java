package com.tfg.charmreader.objetosBD;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class Sesion implements Serializable {
    @SerializedName("idGrupo")
    private int idGrupo;

    @SerializedName("fecha")
    private LocalDate fecha; // LocalDate en API

    @SerializedName("hora")
    private LocalTime hora; // LocalTime en API

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

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
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