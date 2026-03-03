package com.tfg.charmreader.data.model;

import com.google.gson.annotations.SerializedName;

public class Libro {
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
    @SerializedName("url")
    private String url;

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

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
