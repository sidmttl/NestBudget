package com.example.nestbudget;

public class Transaction {

    private String id;
    private String name;
    private String category;
    private String amount;
    private String location;
    private String date;

    private String user;

    public Transaction() {
        this.id = "";
        this.name = "";
        this.category = "";
        this.amount = "";
        this.location = "";
        this.date = "";
        this.user = "";
    }


    public Transaction(String id, String name, String category, String amount, String location, String date, String user) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.location = location;
        this.date = date;
        this.user = user;
    }

    public String getTransactionId() {
        return id;
    }

    public String getTransactionName() {
        return name;
    }

    public String getTransactionCategory() {
        return category;
    }

    public String getTransactionAmount() {
        return amount;
    }

    public String getTransactionLocation() {
        return location;
    }

    public String getTransactionDate() {
        return date;
    }

    public String getUser() {
        return user;
    }

    public void setTransactionId(String id) {
        this.id = id;
    }

    public void setTransactionName(String name) {
        this.name = name;
    }

    public void setTransactionCategory(String category) {
        this.category = category;
    }

    public void setTransactionAmount(String amount) {
        this.amount = amount;
    }

    public void setTransactionLocation(String location) {
        this.location = location;
    }

    public void setTransactionDate(String date) {
        this.date = date;
    }

    public void setUser(String user) {
        this.user =  user;
    }
}

