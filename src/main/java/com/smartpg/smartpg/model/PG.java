package com.smartpg.smartpg.model;

import jakarta.persistence.*;

@Entity
public class PG {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String location;
    private String city;
    private int price;
    private String image;

    private double rating;



    @ManyToOne
    private User owner;




    // GETTERS
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getCity() {
        return city;
    }
    public int getPrice() {
        return price;
    }

    public String getImage() {
        return image;
    }
    public User getOwner() {
        return owner;
    }

    public double getRating() {
        return rating;
    }


    // SETTERS

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    public void setCity(String city) {
        this.city = city;
    }

    public void setPrice(int price) {
        this.price = price;
    }
    public void setImage(String image) {
        this.image = image;
    }
    public void setOwner(User owner) {
        this.owner = owner;
    }
    public void setRating(double rating) {
        this.rating = rating;
    }
}