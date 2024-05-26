package com.example.wittelibrary;

import com.google.gson.annotations.SerializedName;

public class Book {
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("image")
    private String image;

    @SerializedName("path")
    private String path;

    @SerializedName("description")
    private String description;

    @SerializedName("author")
    private String author;

    @SerializedName("publisher")
    private String publisher;

    // Поле для хранения содержимого книги в виде массива байтов
    private byte[] contentBytes;

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return "http://188.32.174.97/" + image;
    }

    public String getPath() {
        return "http://188.32.174.97/" + path;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    public String getPublisher() {
        return publisher;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    // Метод для установки содержимого книги в виде массива байтов
    public void setContentBytes(byte[] contentBytes) {
        this.contentBytes = contentBytes;
    }

    // Метод для получения содержимого книги в виде массива байтов
    public byte[] getContentBytes() {
        return contentBytes;
    }
}
