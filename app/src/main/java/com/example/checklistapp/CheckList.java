package com.example.checklistapp;

public class CheckList {
    private String id;
    private String description;
    private boolean checked;

    public CheckList() {
        // Construtor vazio necessário para Firebase Firestore
    }

    public CheckList(String id, String description, boolean checked) {
        this.id = id;
        this.description = description;
        this.checked = checked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}