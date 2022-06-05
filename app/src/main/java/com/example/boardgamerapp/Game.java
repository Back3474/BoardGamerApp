package com.example.boardgamerapp;

public class Game {

    private String name;
    private int votes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public Game(String name, int votes) {
        this.name = name;
        this.votes = votes;
    }
}
