package com.example.boardgamerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    private ListView ratingsListView;
    private ArrayList<Rating> ratings;
    private String userName;
    private int mealRating, nightRating;
    private Rating rate;
    private Boolean ratedAlready;
    private Map<String, Object> rating;
    private String name;

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
        ratedAlready = false;
        rating = new HashMap<>();


        db = FirebaseDatabase.getInstance("https://board-gamer-app-ff958-default-rtdb.firebaseio.com");
        auth = FirebaseAuth.getInstance();

        ratings = new ArrayList<>();

        LinearLayout allRatingsLayout = findViewById(R.id.allRatings_layout);
        allRatingsLayout.setVisibility(View.GONE);

        DatabaseReference ref = db.getReference("last gamenight/ratings");
        DatabaseReference refName = db.getReference("users/"+auth.getUid());

        refName.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                name = snapshot.child("firstname").getValue().toString() + " " + snapshot.child("lastname").getValue().toString();
                }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ratedAlready = snapshot.hasChild(auth.getUid());
                ratings.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    userName = dataSnapshot.child("name").getValue().toString();
                    mealRating = dataSnapshot.child("meal rating").getValue(Integer.class);
                    nightRating = dataSnapshot.child("gamenight rating").getValue(Integer.class);
                    if(dataSnapshot.hasChild("comment")){
                        String comment = dataSnapshot.child("comment").getValue().toString();
                        ratings.add(new Rating(userName, mealRating, nightRating, comment));
                    } else {
                        ratings.add(new Rating(userName, mealRating, nightRating));
                    }
                }
                if (!ratings.isEmpty()){
                    allRatingsLayout.setVisibility(View.VISIBLE);
                }
                ArrayAdapter ratingListAdapter = new RatingListAdapter(RatingActivity.this, ratings);
                ratingsListView.setAdapter(ratingListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sendRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rate_meal.getRating() == 0){
                    Toast.makeText(RatingActivity.this, R.string.rate_pls_rate_meal, Toast.LENGTH_SHORT).show();
                } else if (rate_night.getRating() == 0){
                    Toast.makeText(RatingActivity.this, R.string.rate_pls_rate_night, Toast.LENGTH_SHORT).show();
                } else if (ratedAlready == true){
                    AlertDialog.Builder builder = new AlertDialog.Builder(RatingActivity.this);
                    builder.setCancelable(true);
                    builder.setTitle(R.string.rate_change_rating_title);
                    if (TextUtils.isEmpty(rate_msg.getText())){
                        builder.setMessage(getText(R.string.change_rate_last_gamenight_msg) + " " + String.valueOf(rate_meal.getRating()) + "/ " + String.valueOf(rate_night.getRating() + "?"));
                    } else {
                        builder.setMessage(getText(R.string.change_rate_last_gamenight_msg) + " " + String.valueOf(rate_meal.getRating()) + "/ " + String.valueOf(rate_night.getRating() + ", " + getText(R.string.rate_user_comment) +": " + rate_msg.getText() + "?"));
                    }
                    builder.setPositiveButton(R.string.discard_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            rating.put("name", name);
                            rating.put("meal rating", rate_meal.getRating());
                            rating.put("gamenight rating", rate_night.getRating());
                            if (!TextUtils.isEmpty(rate_msg.getText())) {
                                String adjustedComment = rate_msg.getText().toString().replaceAll("(?m)^[ \t]*\r?\n", "");
                                rating.put("comment", adjustedComment);
                            }
                            ref.child(auth.getUid()).setValue(rating).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(RatingActivity.this, R.string.rate_rating_changed, Toast.LENGTH_SHORT).show();
                                        rating.clear();
                                    }
                                }
                            });
                            rate_meal.setRating(0);
                            rate_night.setRating(0);
                            rate_msg.setText(null);
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RatingActivity.this);
                    builder.setCancelable(true);
                    builder.setTitle(R.string.rate_last_gamenight_title);
                    if (TextUtils.isEmpty(rate_msg.getText())){
                        builder.setMessage(getText(R.string.rate_last_gamenight_msg) + " " + String.valueOf(rate_meal.getRating()) + "/ " + String.valueOf(rate_night.getRating()) + ")");
                    } else {
                        builder.setMessage(getText(R.string.rate_last_gamenight_msg) + " " + String.valueOf(rate_meal.getRating()) + "/ " + String.valueOf(rate_night.getRating() + ", " + getText(R.string.rate_user_comment) +": " + rate_msg.getText()) + ")");
                    }
                    builder.setPositiveButton(R.string.discard_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            rating.put("name", name);
                            rating.put("meal rating", rate_meal.getRating());
                            rating.put("gamenight rating", rate_night.getRating());
                            if (!TextUtils.isEmpty(rate_msg.getText())) {
                                String adjustedComment = rate_msg.getText().toString().replaceAll("(?m)^[ \t]*\r?\n", "");
                                rating.put("comment", adjustedComment);
                            }

                            ref.child(auth.getUid()).setValue(rating).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(RatingActivity.this, R.string.rate_rating_success, Toast.LENGTH_SHORT).show();
                                        rating.clear();
                                    }
                                }
                            });
                            rate_meal.setRating(0);
                            rate_night.setRating(0);
                            rate_msg.setText(null);
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