package com.smartpg.smartpg.model;

import jakarta.persistence.*;

@Entity
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int stars;

    private String review;

    @ManyToOne
    private User user;

    @ManyToOne
    private PG pg;

    // getters
    public int getId() {
        return id;
    }

    public int getStars() {
        return stars;
    }

    public User getUser() {
        return user;
    }

    public PG getPg() {
        return pg;
    }

    public String getReview() {
        return review;
    }
    // setters
    public void setId(int id) {
        this.id = id;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setPg(PG pg) {
        this.pg = pg;
    }
    public void setReview(String review) {
        this.review = review;
    }
}