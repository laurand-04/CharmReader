package com.tfg.charmreader.objetosBD;

import com.google.gson.annotations.SerializedName;

public class BookEn {
    @SerializedName("id")
    private int id;
    @SerializedName("idU")
    private int idU;
    @SerializedName("titulo")
    private String titulo;

    @SerializedName("subtitulo")
    private String subtitulo;

    @SerializedName("fechaPublicacion")
    private int fechaPublicacion;

    @SerializedName("coverId")
    private String coverId;

    @SerializedName("autor")
    private String autor;

    @SerializedName("tema")
    private String tema;

    @SerializedName("resumen")
    private String resumen;

    // Constructor que realiza la conversión de BookRe a BookEn
    public BookEn(Book re, int idU) {
        this.titulo = re.getTitle();
        this.subtitulo = re.getSubtitle();
        this.fechaPublicacion = re.getPublishYear();
        this.coverId = re.getCoverId();

        // Conversión de listas a Strings con validación de nulos
        this.autor = (re.getAuthorNames() != null && !re.getAuthorNames().isEmpty())
                ? re.getAuthorNames().get(0) : "Desconocido";

        this.tema = (re.getSubjects() != null && !re.getSubjects().isEmpty())
                ? re.getSubjects().get(0) : "Sin tema";

        this.resumen = (re.getFirstSentence() != null && !re.getFirstSentence().isEmpty())
                ? re.getFirstSentence().get(0) : "Sin resumen";

        this.idU = idU;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getSubtitulo() {
        return subtitulo;
    }

    public void setSubtitulo(String subtitulo) {
        this.subtitulo = subtitulo;
    }

    public int getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(int fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public String getCoverId() {
        return coverId;
    }

    public void setCoverId(String coverId) {
        this.coverId = coverId;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public String getResumen() {
        return resumen;
    }

    public void setResumen(String resumen) {
        this.resumen = resumen;
    }

    public int getIdU() {
        return idU;
    }

    public void setIdU(int idU) {
        this.idU = idU;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}