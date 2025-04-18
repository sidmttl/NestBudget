package com.example.nestbudget;

public class FinancialGoal {
    private String id;
    private String name;
    private double targetAmount;
    private double currentAmount;
    private String category;
    private String createdBy;

    // Default constructor required for Firebase
    public FinancialGoal() {
    }

    public FinancialGoal(String id, String name, double targetAmount, double currentAmount, String category, String createdBy) {
        this.id = id;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
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

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
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

    // Calculate progress percentage
    public int getProgressPercentage() {
        if (targetAmount == 0) return 0;
        return (int) ((currentAmount / targetAmount) * 100);
    }

    // Calculate amount remaining
    public double getAmountRemaining() {
        return targetAmount - currentAmount;
    }
}