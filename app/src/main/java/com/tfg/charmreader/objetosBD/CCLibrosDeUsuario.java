package com.tfg.charmreader.objetosBD;

import com.google.gson.annotations.SerializedName;

public class CCLibrosDeUsuario {
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
        idU = idU;
    }

    public Integer getIdL() {
        return idL;
    }

    public void setIdL(Integer idL) {
        idL = idL;
    }
}
