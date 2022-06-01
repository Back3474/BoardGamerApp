package com.example.boardgamerapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;

public class RatingActivity extends AppCompatActivity {
    private EditText eval_msg;
    private RatingBar eval_meal;
    private RatingBar eval_night;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        overridePendingTransition(com.google.android.material.R.anim.abc_popup_enter, com.google.android.material.R.anim.abc_popup_exit);

        eval_meal = findViewById(R.id.rate_mealRating);
        eval_night = findViewById(R.id.rate_nightRating);
        eval_msg = findViewById(R.id.rate_msg);
    }

    @Override
    public void onBackPressed() {
        if (eval_meal.getRating() != 0 || eval_night.getRating() != 0 || !eval_msg.getText().toString().isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(RatingActivity.this);
            builder.setCancelable(true);
            builder.setTitle(R.string.discard_title);
            builder.setMessage(R.string.discard_msg);
            builder.setPositiveButton(R.string.discard_yes,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(RatingActivity.this, MainActivity.class));
                            finish();
                        }
                    });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        } else {
            startActivity(new Intent(RatingActivity.this, MainActivity.class));
            finish();
        }
    }
}