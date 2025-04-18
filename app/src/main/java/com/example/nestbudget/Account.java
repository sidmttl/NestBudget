package com.example.nestbudget;

public class Account {
    private String id;
    private String name;
    private String type; // "Cash", "Savings", "Credit Card", "Investment"
    private double balance;
    private String institution;
    private String accountNumber; // Last 4 digits only for security
    private String createdBy;

    // Default constructor required for Firebase
    public Account() {
    }

    public Account(String id, String name, String type, double balance, String institution, String accountNumber, String createdBy) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.balance = balance;
        this.institution = institution;
        this.accountNumber = accountNumber;
        this.createdBy = createdBy;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}