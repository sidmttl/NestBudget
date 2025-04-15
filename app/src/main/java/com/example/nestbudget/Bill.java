package com.example.nestbudget;

import java.util.Date;

public class Bill {
    private String id;
    private String name;
    private double amount;
    private Date dueDate;
    private boolean isPaid;
    private String category;
    private String createdBy;

    // Default constructor required for Firebase
    public Bill() {
    }

    public Bill(String id, String name, double amount, Date dueDate, boolean isPaid, String category, String createdBy) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.dueDate = dueDate;
        this.isPaid = isPaid;
        this.category = category;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    // Check if bill is overdue
    public boolean isOverdue() {
        if (isPaid) return false;
        return dueDate.before(new Date());
    }

    // Calculate days until due
    public long daysUntilDue() {
        if (isPaid) return 0;

        long diff = dueDate.getTime() - new Date().getTime();
        return diff / (24 * 60 * 60 * 1000);
    }
}