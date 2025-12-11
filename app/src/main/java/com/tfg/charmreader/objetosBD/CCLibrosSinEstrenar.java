package com.tfg.charmreader.objetosBD;

public class CCLibrosSinEstrenar {
    private int idu;      // ID del usuario
    private int idlibro;  // ID del libro

    public CCLibrosSinEstrenar(int idu, int idlibro) {
        this.idu = idu;
        this.idlibro = idlibro;
    }

    public int getIdu() {
        return idu;
    }

    public int getIdlibro() {
        return idlibro;
    }

    public void setIdlibro(int idlibro) {
        this.idlibro = idlibro;
    }
}
