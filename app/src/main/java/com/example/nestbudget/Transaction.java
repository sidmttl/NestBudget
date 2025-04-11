package com.example.nestbudget;

public class Transaction {

    private String id;
    private String name;
    private String category;
    private String amount;
    private String location;
    private String date;

    public Transaction() {
        this.id = "";
        this.name = "";
        this.category = "";
        this.amount = "";
        this.location = "";
        this.date = "";
    }


    public Transaction(String id, String name, String category, String amount, String location, String date) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.location = location;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getAmount() {
        return amount;
    }

    public String getLocation() {
        return location;
    }

    public String getDate() {
        return date;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

