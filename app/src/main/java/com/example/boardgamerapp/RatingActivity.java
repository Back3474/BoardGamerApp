package com.example.boardgamerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RatingActivity extends AppCompatActivity {
    private EditText rate_msg;
    private RatingBar rate_meal;
    private RatingBar rate_night;
    private ImageButton sendRating;
    private FirebaseDatabase db;
    private FirebaseAuth auth;
    private int mealRating, gamenightRating;
    private ListView ratingsListView;
    ArrayList<Rating> ratings;

    public RatingActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        overridePendingTransition(com.google.android.material.R.anim.abc_popup_enter, com.google.android.material.R.anim.abc_popup_exit);

        rate_meal = findViewById(R.id.rate_mealRating);
        rate_night = findViewById(R.id.rate_nightRating);
        rate_msg = findViewById(R.id.rate_msg);
        sendRating = findViewById(R.id.rate_sendBtn);
        ratingsListView = findViewById(R.id.all_ratings_list);
        ratings = new ArrayList<Rating>();


        db = FirebaseDatabase.getInstance("https://board-gamer-app-ff958-default-rtdb.firebaseio.com");
        auth = FirebaseAuth.getInstance();

        DatabaseReference ref = db.getReference("last gamenight/ratings");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ratings.clear();
                ratings.add(new Rating("test",1,1));
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String userName = "test";
                    int mealRating = dataSnapshot.child("meal rating").getValue(Integer.class);
                    int gamenightRating = dataSnapshot.child("gamenight rating").getValue(Integer.class);

                    ratings.add(new Rating(userName, mealRating, gamenightRating));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        RatingListAdapter listAdapter = new RatingListAdapter(RatingActivity.this, ratings);
        ratingsListView.setAdapter(listAdapter);

        sendRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rate_meal.getRating() == 0){
                    Toast.makeText(RatingActivity.this, R.string.rate_pls_rate_meal, Toast.LENGTH_SHORT).show();
                } else if (rate_night.getRating() == 0){
                    Toast.makeText(RatingActivity.this, R.string.rate_pls_rate_night, Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RatingActivity.this);
                    builder.setCancelable(true);
                    builder.setTitle(R.string.rate_last_gamenight_title);
                    builder.setMessage(getText(R.string.rate_last_gamenight_msg) + " " + String.valueOf(rate_meal.getRating()) + "/ " + String.valueOf(rate_night.getRating() + ")"));
                    builder.setPositiveButton(R.string.discard_yes,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DatabaseReference ref = db.getReference("last gamenight/ratings");
                                    Map<String, Object> rating = new HashMap<>();
                                    rating.put("meal rating", rate_meal.getRating());
                                    rating.put("gamenight rating", rate_night.getRating());
                                    if (!TextUtils.isEmpty(rate_msg.getText())) {
                                        rating.put("comment", rate_msg.getText().toString());
                                    }

                                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String uid = auth.getUid();
                                            if (snapshot.hasChild(uid)) {
                                                ref.child(String.valueOf(auth.getUid())).setValue(rating);
                                                Toast.makeText(RatingActivity.this, R.string.rate_rating_changed, Toast.LENGTH_SHORT).show();
                                                rate_meal.setRating(0);
                                                rate_night.setRating(0);
                                                rate_msg.setText(null);
                                            } else {
                                                ref.child(String.valueOf(auth.getUid())).setValue(rating);
                                                Toast.makeText(RatingActivity.this, R.string.rate_rating_success, Toast.LENGTH_SHORT).show();
                                                rate_meal.setRating(0);
                                                rate_night.setRating(0);
                                                rate_msg.setText(null);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
            }
        });
    }



    @Override
    public void onBackPressed() {
        if (rate_meal.getRating() != 0 || rate_night.getRating() != 0 || !rate_msg.getText().toString().isEmpty()) {
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