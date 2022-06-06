package com.example.boardgamerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
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

public class GamesActivity extends AppCompatActivity {
    private ListView listView;
    private String gameName;
    private int gameVotes;
    private FirebaseDatabase db;
    private FirebaseAuth auth;
    private ArrayList<Game> gamesList;
    private ImageButton suggestGame;
    private EditText suggestedGame;
    private Button voteBtn;
    private Button cancel;
    private long votes;
    private Boolean clickedForVote;
    private LinearLayout gamesListItem;

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
        voteBtn = findViewById(R.id.vote_game_btn);
        cancel = findViewById(R.id.cancelBtn);
        clickedForVote = false;

        listView.setClickable(false);

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

                votes = snapshot.child("votes").child(auth.getUid()).getChildrenCount();
                if (votes == 0){
                    voteBtn.setText("Vote game (2 votes left)");
                } else if (votes == 1) {
                    voteBtn.setText("Vote game (1 vote left)");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        voteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(clickedForVote == false){
                    if(votes == 2){
                        Toast.makeText(GamesActivity.this, "voted 2x already", Toast.LENGTH_SHORT).show();
                    } else {
                        clickedForVote = true;
                        Toast.makeText(GamesActivity.this, "click on game for vote", Toast.LENGTH_SHORT).show();
                        voteBtn.setText("confirm votes");
                        listView.setBackgroundResource(R.drawable.border_colored);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                if (!view.isSelected()){
                                    view.setBackgroundResource(R.color.design_light);
                                    view.setSelected(true);
                                }
                            }
                        });

                    }
                    cancel.setVisibility(View.VISIBLE);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (votes == 0){
                                voteBtn.setText("Vote game (2 votes left)");
                            } else if (votes == 1) {
                                voteBtn.setText("Vote game (1 vote left)");
                            }
                            cancel.setVisibility(View.INVISIBLE);
                            listView.setBackgroundResource(R.drawable.border);
                            clickedForVote = false;
                        }
                    });
                } else {
                    Toast.makeText(GamesActivity.this, "test", Toast.LENGTH_SHORT).show();
                    clickedForVote = false;
                }
                
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
                    builder.setMessage(getText(R.string.games_suggest_game_msg) + " " + suggestedGame.getText().toString() + "?");
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