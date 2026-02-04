package com.tfg.charmreader.objetosBD;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class BookEn implements Serializable {
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
    private TemaLibro tema;

    @SerializedName("resumen")
    private String resumen;

    public enum TemaLibro {
        AVENTURAS, CIENCIA_FICCION, DRAMA, FANTASIA,
        HISTORICA, HUMOR, POLICIACA, ROMANCE,
        SUSPENSE, TERROR, INFANTIL, JUVENIL,
        BIOGRAFIA, AUTOAYUDA, ENSAYO, OTRO
    }

    // Constructor que realiza la conversión de BookRe a BookEn
    public BookEn(Book re, int idU) {
        this.titulo = re.getTitle();
        this.subtitulo = re.getSubtitle();
        this.fechaPublicacion = re.getPublishYear();
        this.coverId = re.getCoverId();

        // Autores
        this.autor = (re.getAuthorNames() != null && !re.getAuthorNames().isEmpty())
                ? re.getAuthorNames().get(0) : "Desconocido";

        // 🔹 NUEVO PLANTEAMIENTO DE TEMA
        String temaDeAPI = (re.getSubjects() != null && !re.getSubjects().isEmpty())
                ? re.getSubjects().get(0) : "";

        // Convertimos el String de la API en nuestro Enum
        this.tema = mapearTema(temaDeAPI);

        // Resumen
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

    public TemaLibro getTema() {
        return tema;
    }

    public void setTema(TemaLibro tema) {
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

    public static TemaLibro mapearTema(String temaExterno) {
        if (temaExterno == null || temaExterno.isEmpty()) return TemaLibro.OTRO;

        // Pasamos a minúsculas para comparar sin errores
        String t = temaExterno.toLowerCase();

        // Lógica de clasificación por palabras clave
        if (t.contains("adventure") || t.contains("aventura")) return TemaLibro.AVENTURAS;
        if (t.contains("science fiction") || t.contains("sci-fi") || t.contains("ciencia ficción")) return TemaLibro.CIENCIA_FICCION;
        if (t.contains("drama") || t.contains("tragedy")) return TemaLibro.DRAMA;
        if (t.contains("fantasy") || t.contains("fantasía") || t.contains("magic")) return TemaLibro.FANTASIA;
        if (t.contains("history") || t.contains("histórica") || t.contains("historia")) return TemaLibro.HISTORICA;
        if (t.contains("humor") || t.contains("comedy") || t.contains("comedia")) return TemaLibro.HUMOR;
        if (t.contains("police") || t.contains("crime") || t.contains("policiaca") || t.contains("detective")) return TemaLibro.POLICIACA;
        if (t.contains("romance") || t.contains("love") || t.contains("amor")) return TemaLibro.ROMANCE;
        if (t.contains("suspense") || t.contains("thriller") || t.contains("misterio")) return TemaLibro.SUSPENSE;
        if (t.contains("horror") || t.contains("terror") || t.contains("scary")) return TemaLibro.TERROR;
        if (t.contains("children") || t.contains("infantil") || t.contains("kids")) return TemaLibro.INFANTIL;
        if (t.contains("juvenile") || t.contains("young adult") || t.contains("juvenil") || t.contains("teen")) return TemaLibro.JUVENIL;
        if (t.contains("biography") || t.contains("autobiography") || t.contains("biografía")) return TemaLibro.BIOGRAFIA;
        if (t.contains("self-help") || t.contains("autoayuda") || t.contains("personal growth")) return TemaLibro.AUTOAYUDA;
        if (t.contains("essay") || t.contains("ensayo") || t.contains("philosophy")) return TemaLibro.ENSAYO;

        // Si no encaja en ninguna de las anteriores
        return TemaLibro.OTRO;
    }
}