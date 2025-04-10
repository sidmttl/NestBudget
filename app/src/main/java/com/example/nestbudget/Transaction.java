package com.example.nestbudget;

public class Transaction {
    private  String name;
    private  String category;

    private  String amount;

    private String location;

    private String date;

    public Transaction(String name, String category, String amount, String location, String date) {
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.location = location;
        this.date = date;
    }
    public String getTransactionName() {
        return name;
    }
    public String getTransactionCategory() {
        return category;
    }

    public String getTransactionAmt() {
        return amount;
    }
    public String getTransactionLocation() {
        return location;
    }

    public String getTransactionDate() {
        return date;
    }


    public void setTransactionName(String name){
        this.name = name;
    }

    public void setTransactionCategory(String category){
        this.category = category;
    }

    public void setTransactionAmt(String amount){this.amount = amount;}

    public void setTransactionLocation(String location){
        this.location = location;
    }
    public void setTransactionDate(String date){
        this.date = date;
    }
}

