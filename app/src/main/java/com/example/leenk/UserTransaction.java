package com.example.leenk;

public class UserTransaction {
    private String type;
    private double amount;
    private long timestamp;
    private String paymentMethod;

    // Default constructor required for calls to DataSnapshot.getValue(UserTransaction.class)
    public UserTransaction() {
    }

    // Constructor matching your parameters
    public UserTransaction(String type, double amount, long timestamp, String paymentMethod) {
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
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
}
