package com.tfg.charmreader.objetosBD;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Book {

    @SerializedName("key")
    private String key;
    @SerializedName("title")
    private String title;

    @SerializedName("subtitle")
    private String subtitle;

    @SerializedName("first_publish_year")
    private int publishYear;

    @SerializedName("cover_i")
    private String coverId;

    @SerializedName("author_name")
    private List<String> authorNames;

    @SerializedName("subject")
    private List<String> subjects;

    @SerializedName("first_sentence")
    private List<String> firstSentence;

    // Getters necesarios para la conversión
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle != null ? subtitle : ""; }
    public int getPublishYear() { return publishYear; }
    public String getCoverId() { return coverId; }
    public List<String> getAuthorNames() { return authorNames; }
    public List<String> getSubjects() { return subjects; }
    public List<String> getFirstSentence() { return firstSentence; }

    // Helpers para el Adapter
    public String getFormattedAuthors() {
        if (authorNames == null || authorNames.isEmpty()) return "Autor desconocido";
        return String.join(", ", authorNames);
    }
    public String getFirstAuthor() {
        if (authorNames != null && !authorNames.isEmpty()) {
            return authorNames.get(0); // Devuelve el primer nombre de la lista
        }
        return "Autor desconocido";
    }

    public String getKey() { return key; }
    public String getFullOpenLibraryUrl() {
        if (key != null) {
            return "https://openlibrary.org" + key;
        }
        return null;
    }
}