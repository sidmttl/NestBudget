package com.example.nestbudget;

public class ToBuyList {

    String id;
    private String name;
    private String content;

    public ToBuyList() {
        // Needed for Firebase
        this.name = "";
        this.content = "";
    }
    public ToBuyList(String id, String name, String content) {
        this.id = id;
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
