package com.example.myshop.Models;

import com.google.firebase.Timestamp;

public class ReviewModel {
    private String userId;
    private float rating;
    private String comment;
    private Timestamp timestamp;

    public ReviewModel() {
    }

    public ReviewModel(String userId, float rating, String comment, Timestamp timestamp) {
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
