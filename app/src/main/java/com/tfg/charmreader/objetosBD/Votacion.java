package com.tfg.charmreader.objetosBD;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Votacion implements Serializable {
    @SerializedName("idUsuario")
    private int idUsuario;

    @SerializedName("idGrupo")
    private int idGrupo;

    @SerializedName("idBook")
    private int idBook; // int en API

    @SerializedName("valor")
    private boolean valor;

    public Votacion(int idUsuario, int idGrupo, int idBook, boolean valor) {
        this.idUsuario = idUsuario;
        this.idGrupo = idGrupo;
        this.idBook = idBook;
        this.valor = valor;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
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

    public boolean isValor() {
        return valor;
    }

    public void setValor(boolean valor) {
        this.valor = valor;
    }
}