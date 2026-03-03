package com.tfg.charmreader.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;

public class Miembro implements Serializable {
    @SerializedName("idGrupo")
    private int idGrupo;

    @SerializedName("idUsuario")
    private int idUsuario;

    @SerializedName("fechaUnion")
    private Date fechaUnion; // Date en API

    public Miembro(int idGrupo, int idUsuario) {
        this.idGrupo = idGrupo;
        this.idUsuario = idUsuario;
    }

    public int getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(int idGrupo) {
        this.idGrupo = idGrupo;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Date getFechaUnion() {
        return fechaUnion;
    }

    public void setFechaUnion(Date fechaUnion) {
        this.fechaUnion = fechaUnion;
    }
}