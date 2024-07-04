package com.example.leenk;

public class UserTransaction {
    private String type;
    private double amount;
    private long timestamp;
    private String description;
    private String paymentMethod;

    // Default constructor
    public UserTransaction() {
    }

    // Constructor with 4 parameters
    public UserTransaction(String type, double amount, long timestamp, String description) {
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
        this.description = description;
        this.paymentMethod = ""; // Default empty string for payment method
    }

    // Constructor with 5 parameters
    public UserTransaction(String type, double amount, long timestamp, String description, String paymentMethod) {
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
        this.description = description;
        this.paymentMethod = paymentMethod;
    }

    // Getters and setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}