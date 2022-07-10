package com.example.boardgamerapp;

import android.app.Application;

public class ApplicationWide extends Application {
    private Boolean subscribed = false;

    public Boolean getSubscribed() {
        return subscribed;
    }

    public void setSubscribed(Boolean subscribed) {
        this.subscribed = subscribed;
    }
}
