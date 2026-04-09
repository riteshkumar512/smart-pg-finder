package com.smartpg.smartpg.model;


import jakarta.persistence.*;

@Entity
@Table(name = "wishlist",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "pg_id"}))
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "pg_id")
    private Long pgId;

    // Constructors
    public Wishlist() {}

    public Wishlist(Long userId, Long pgId) {
        this.userId = userId;
        this.pgId = pgId;
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPgId() {
        return pgId;
    }

    public void setPgId(Long pgId) {
        this.pgId = pgId;
    }
}