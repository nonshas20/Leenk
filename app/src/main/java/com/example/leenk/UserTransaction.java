package com.example.leenk;

public class UserTransaction {
    private String type;
    private double amount;
    private long timestamp;

    // Default constructor required for Firebase
    public UserTransaction() {}

    public UserTransaction(String type, double amount, long timestamp) {
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
    }
    // Getters
    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Setters (if needed)
    public void setType(String type) {
        this.type = type;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}