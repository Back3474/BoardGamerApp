package com.example.boardgamerapp;

public class Rating {

    private String ratingUserName;
    private int mealRating, gamenightRating;
    private String comment;

    public Rating(String ratingUserName, int mealRating, int gamenightRating) {
        this.ratingUserName = ratingUserName;
        this.mealRating = mealRating;
        this.gamenightRating = gamenightRating;
        this.comment = "---";
    }

    public Rating(String ratingUserName, int mealRating, int gamenightRating, String comment) {
        this.ratingUserName = ratingUserName;
        this.mealRating = mealRating;
        this.gamenightRating = gamenightRating;
        this.comment = comment;
    }

    public String getRatingUserName() {
        return ratingUserName;
    }

    public void setRatingUserName(String ratingUserName) {
        this.ratingUserName = ratingUserName;
    }

    public int getMealRating() {
        return mealRating;
    }

    public void setMealRating(int mealRating) {
        this.mealRating = mealRating;
    }

    public int getGamenightRating() {
        return gamenightRating;
    }

    public void setGamenightRating(int gamenightRating) {
        this.gamenightRating = gamenightRating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
