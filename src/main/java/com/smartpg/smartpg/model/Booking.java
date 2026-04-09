package com.smartpg.smartpg.model;

import jakarta.persistence.*;

@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int stars; // 1 to 5

    private String review;

    private String paymentStatus;   // PENDING / SUCCESS / FAILED

    private String paymentId;

    private String paymentMethod;

    @ManyToOne
    private User user;

    @ManyToOne
    private PG pg;

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // getters & setters
    public int getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public PG getPg() { return pg; }
    public void setPg(PG pg) { this.pg = pg; }

    public String getPaymentId() {
        return paymentId;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}