package com.tfg.charmreader.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CCLibrosDeUsuario implements Serializable {
    @SerializedName("idU")
    private int idU;
    @SerializedName("idL")
    private int idL;

    public CCLibrosDeUsuario(Integer idU, Integer idL) {
        this.idU = idU;
        this.idL = idL;
    }

    public Integer getIdU() {
        return idU;
    }

    public void setIdU(Integer idU) {
        this.idU = idU;
    }

    public Integer getIdL() {
        return idL;
    }

    public void setIdL(Integer idL) {
        this.idL = idL;
    }
}
