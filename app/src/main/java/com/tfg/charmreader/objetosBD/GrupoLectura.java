package com.tfg.charmreader.objetosBD;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

public class GrupoLectura implements Serializable {
    @SerializedName("idGrupo")
    private int idGrupo;
    @SerializedName("idUsuario")
    private int idUsuario;
    @SerializedName("nombre")
    private String nombre;
    @SerializedName("ubicacion")
    private String ubicacion;
    @SerializedName("descripcion")
    private String descripcion;
    @SerializedName("fechaCreacion")
    private Date fechaCreacion;
    @SerializedName("frecuenciaReunion")
    private Frecuencia frecuenciaReunion;

    // El Enum debe coincidir exactamente con los nombres de la API
    public enum Frecuencia {
        SEMANAL, QUINCENAL, MENSUAL
    }

    public GrupoLectura() {
    }

    // Getters y Setters
    public int getIdGrupo() { return idGrupo; }
    public void setIdGrupo(int idGrupo) { this.idGrupo = idGrupo; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Frecuencia getFrecuenciaReunion() { return frecuenciaReunion; }
    public void setFrecuenciaReunion(Frecuencia frecuenciaReunion) { this.frecuenciaReunion = frecuenciaReunion; }
}
