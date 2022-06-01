package com.example.boardgamerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class Splashscreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        final Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                startActivity(new Intent(Splashscreen.this, LoginActivity.class));
                finish();
            }
        };
        handler.postDelayed(r, 1000);
    }
}