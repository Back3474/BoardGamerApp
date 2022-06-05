package com.example.boardgamerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
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

public class GamesActivity extends AppCompatActivity {
    private ListView listView;
    private String gameName;
    private int gameVotes;
    private FirebaseDatabase db;
    private FirebaseAuth auth;
    private ArrayList<Game> gamesList;
    private ImageButton suggestGame;
    private EditText suggestedGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games);

        overridePendingTransition(com.google.android.material.R.anim.abc_popup_enter, com.google.android.material.R.anim.abc_popup_exit);

        db = FirebaseDatabase.getInstance("https://board-gamer-app-ff958-default-rtdb.firebaseio.com");
        auth = FirebaseAuth.getInstance();

        listView = findViewById(R.id.gamesList);
        gamesList = new ArrayList<>();
        suggestGame = findViewById(R.id.suggestGameBtn);
        suggestedGame = findViewById(R.id.suggestedGame);

        DatabaseReference ref = db.getReference("next meeting/games");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                gamesList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    gameName = dataSnapshot.getKey().toString();
                    gameVotes = dataSnapshot.child("votes").getValue(Integer.class);
                    gamesList.add(new Game(gameName, gameVotes));
                }
                if (gamesList.isEmpty()){
                    gamesList.add(new Game("no games suggested", 0));
                }
                ArrayAdapter gamesListAdapter = new GamesListAdapter(GamesActivity.this, 0, gamesList);
                listView.setAdapter(gamesListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        suggestGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(suggestedGame.getText())){
                    Toast.makeText(GamesActivity.this, "enter game for suggestion", Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(GamesActivity.this);
                    builder.setCancelable(true);
                    builder.setTitle(R.string.games_suggest_game_title);
                    builder.setMessage(getText(R.string.games_suggest_game_msg) + suggestedGame.getText().toString() + "?");
                    builder.setPositiveButton(R.string.discard_yes,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.hasChild(suggestedGame.getText().toString())){
                                                Toast.makeText(GamesActivity.this, "game already suggested", Toast.LENGTH_SHORT).show();
                                            }else {
                                                ref.child(suggestedGame.getText().toString()).child("votes").setValue(0).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            Toast.makeText(GamesActivity.this, "game added", Toast.LENGTH_SHORT).show();
                                                            suggestedGame.setText(null);
                                                        }
                                                    }
                                                });
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
        startActivity(new Intent(GamesActivity.this, MainActivity.class));
        finish();
    }
}