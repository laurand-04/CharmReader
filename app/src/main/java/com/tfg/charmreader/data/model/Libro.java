package com.tfg.charmreader.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Libro implements Serializable {
    @SerializedName("id")
    private int id;
    @SerializedName("isbn")
    private String isbn;
    @SerializedName("nombre")
    private String nombre;
    @SerializedName("autor")
    private String autor;
    @SerializedName("paginas")
    private int paginas;
    @SerializedName("urlImagen")
    private String urlImagen;
    @SerializedName("urlLibro")
    private String urlLibro;


    public Libro(String isbn, String nombre, String autor, int paginas) {
        this.isbn = isbn;
        this.nombre = nombre;
        this.autor = autor;
        this.paginas = paginas;
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }
    public int getPaginas() { return paginas; }
    public void setPaginas(int paginas) { this.paginas = paginas; }

    public String getUrlImagen() { return urlImagen; }
    public void setUrlImagen(String url) { this.urlImagen = url; }

    public String getUrlLibro() {
        return urlLibro;
    }

    public void setUrlLibro(String urlLibro) {
        this.urlLibro = urlLibro;
    }
}
