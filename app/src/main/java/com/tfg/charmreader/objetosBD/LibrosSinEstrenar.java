package com.tfg.charmreader.objetosBD;

import java.util.Date;

public class LibrosSinEstrenar {
    private CCLibrosSinEstrenar id;
    private Date fechaPublicacion;

    public CCLibrosSinEstrenar getId() {
        return id;
    }

    public Date getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(Date fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }
}

