package com.example.snehil.ui;

public class FileInitializer {
    private String fileName, path;
    private int id;

    public FileInitializer() {
    }

    public FileInitializer(String fileName, String path) {
        this.fileName = fileName;
        this.path = path;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getId() {
        return id;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPath() {
        return path;
    }
}
