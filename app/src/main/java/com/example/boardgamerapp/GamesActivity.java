package com.example.boardgamerapp;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class GamesActivity extends AppCompatActivity {
    private ListView listView;
    private String gameName;
    private int gameVotes, userSuggestedGames;
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
    private String votedGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games);

        overridePendingTransition(com.google.android.material.R.anim.abc_popup_enter, com.google.android.material.R.anim.abc_popup_exit);

        db = FirebaseDatabase.getInstance("https://board-gamer-app-ff958-default-rtdb.firebaseio.com");
        auth = FirebaseAuth.getInstance();

        listView = findViewById(R.id.gamesList);
        gamesList = new ArrayList<>();
        gamesListItem = findViewById(R.id.games_list_item_layout);
        suggestGame = findViewById(R.id.suggestGameBtn);
        suggestedGame = findViewById(R.id.suggestedGame);
        voteBtn = findViewById(R.id.vote_game_btn);
        clickedForVote = false;
        votedGame = "defaultValue";

        listView.setClickable(false);

        DatabaseReference refNextMeeting = db.getReference("next meeting/");
        DatabaseReference refVotes = refNextMeeting.child("votes");
        DatabaseReference refGames = refNextMeeting.child("games");

        refNextMeeting.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("votes")){
                    votes = snapshot.child("votes").child(auth.getUid()).getChildrenCount();
                }
                if (votes == 0){
                    voteBtn.setText(R.string.games_suggest_game_two_votes_left);
                } else if (votes == 1) {
                    voteBtn.setText(R.string.games_suggest_game_one_vote_left);
                } else {
                    voteBtn.setText(R.string.games_vote_btn_no_more_votes);
                }

                int hour = snapshot.child("hour").getValue(Integer.class);
                int min = snapshot.child("minute").getValue(Integer.class);
                LocalTime meetingTime = LocalTime.of(hour, min);
                LocalDate meetingDate = LocalDate.parse(snapshot.child("date").getValue().toString());
                LocalDateTime meeting = LocalDateTime.of(meetingDate, meetingTime);
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime endOfVoting = meeting.minus(1, DAYS);
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern(getText(R.string.date_time_format).toString());
                String endOfVoting_formatted = endOfVoting.format(dtf);
                TextView endOfVoting_txt = findViewById(R.id.end_of_voting);
                endOfVoting_txt.setText(getText(R.string.games_end_of_voting) + " " + endOfVoting_formatted);

                if(HOURS.between(now, meeting) < 24){
                    endOfVoting_txt.setText("");
                    TextView gamesLabel = findViewById(R.id.games_sug_games);
                    gamesLabel.setText(R.string.games_no_more_votes);
                    findViewById(R.id.suggest_game_layout).setVisibility(View.GONE);
                    voteBtn.setVisibility(View.GONE);

                    listView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            listView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            for(int i = 0; i <= gamesList.size() - 1; i++){
                                if(i > 2){
                                    gamesList.remove(i);
                                }
                            }
                            gamesList.remove(3);
                            ArrayAdapter gamesListAdapter = new GamesListAdapter(GamesActivity.this, 0, gamesList);
                            listView.setAdapter(gamesListAdapter);
                        }
                    });
                }
                if(snapshot.child("isCanceled").getValue(Boolean.class)){
                    endOfVoting_txt.setText("");
                    TextView gamesLabel = findViewById(R.id.games_sug_games);
                    gamesLabel.setText(R.string.games_meeting_canceled);
                    findViewById(R.id.suggest_game_layout).setVisibility(View.GONE);
                    findViewById(R.id.suggestedGames_layout).setVisibility(View.GONE);
                    voteBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        refGames.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                gamesList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    gameName = dataSnapshot.getKey().toString();
                    gameVotes = dataSnapshot.child("votes").getValue(Integer.class);
                    gamesList.add(new Game(gameName, gameVotes));
                    Collections.sort(gamesList, new Comparator<Game>() {
                        @Override
                        public int compare(Game game, Game t1) {
                            return Integer.valueOf(t1.getVotes()).compareTo(Integer.valueOf(game.getVotes()));
                        }
                    });
                }
                if (gamesList.isEmpty()){
                    gamesList.add(new Game(getText(R.string.games_no_games_suggested).toString(), 0));
                    voteBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(GamesActivity.this, R.string.games_no_games_suggested, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    voteBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if(votes == 2){
                                Toast.makeText(GamesActivity.this, R.string.games_voted_2x_already, Toast.LENGTH_SHORT).show();
                            } else if(clickedForVote == false){
                                Toast.makeText(GamesActivity.this, R.string.games_click_on_game_vote, Toast.LENGTH_SHORT).show();
                                voteBtn.setText(R.string.games_cancel);
                                listView.setBackgroundResource(R.drawable.border_colored);
                                clickedForVote = true;
                                refNextMeeting.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.child("votes").hasChild(auth.getUid())){
                                            votedGame = snapshot.child("votes").child(auth.getUid()).child("vote1").getValue().toString();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                        TextView gameName = view.findViewById(R.id.gameName);

                                        if(votedGame.equals(gameName.getText().toString())){
                                            AlertDialog.Builder builder = new AlertDialog.Builder(GamesActivity.this);
                                            builder.setTitle(R.string.games_vote_not_same_game);
                                            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            });
                                            AlertDialog dialog = builder.create();
                                            dialog.show();
                                        } else {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(GamesActivity.this);
                                            builder.setCancelable(true);
                                            builder.setTitle(gameName.getText());
                                            builder.setMessage(getText(R.string.games_vote_for_game_msg) + " " + gameName.getText() + "?");
                                            builder.setPositiveButton(R.string.discard_yes, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                    refGames.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            int oldVotes = snapshot.child(gameName.getText().toString()).child("votes").getValue(Integer.class);
                                                            int newVotes = oldVotes + 1;
                                                            refGames.child(gameName.getText().toString()).child("votes").setValue(newVotes).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    refVotes.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                            if(snapshot.child(auth.getUid()).hasChild("vote1")){
                                                                                refVotes.child(auth.getUid()).child("vote2").setValue(gameName.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        Toast.makeText(GamesActivity.this, R.string.games_vote_success, Toast.LENGTH_SHORT).show();
                                                                                        listView.setBackgroundResource(R.drawable.border);
                                                                                        clickedForVote = false;
                                                                                        listView.setOnItemClickListener(null);
                                                                                    }
                                                                                });
                                                                            }else {
                                                                                refVotes.child(auth.getUid()).child("vote1").setValue(gameName.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        Toast.makeText(GamesActivity.this, R.string.games_vote_success, Toast.LENGTH_SHORT).show();
                                                                                        listView.setBackgroundResource(R.drawable.border);
                                                                                        clickedForVote = false;
                                                                                        listView.setOnItemClickListener(null);
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


                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                                }
                                            });
                                            Dialog dialog = builder.create();
                                            dialog.show();
                                        }

                                    }
                                });
                            } else {
                                refVotes.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        votes = snapshot.child(auth.getUid()).getChildrenCount();
                                        if (votes == 0){
                                            voteBtn.setText(R.string.games_suggest_game_two_votes_left);
                                        } else if (votes == 1) {
                                            voteBtn.setText(R.string.games_suggest_game_one_vote_left);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                listView.setBackgroundResource(R.drawable.border);
                                clickedForVote = false;
                                listView.setOnItemClickListener(null);
                            }
                        }
                    });
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
                refGames.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userSuggestedGames = 0;
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                if(dataSnapshot.child("recommender").getValue().equals(auth.getUid())){
                                    userSuggestedGames = userSuggestedGames + 1;
                                }
                            }
                        if(TextUtils.isEmpty(suggestedGame.getText())){
                            Toast.makeText(GamesActivity.this, R.string.games_suggest_game_txt, Toast.LENGTH_SHORT).show();
                        } else if(userSuggestedGames > 1){
                            Toast.makeText(GamesActivity.this, R.string.games_suggested_2x_already, Toast.LENGTH_SHORT).show();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(GamesActivity.this);
                            builder.setCancelable(true);
                            builder.setTitle(R.string.games_suggest_game_title);
                            builder.setMessage(getText(R.string.games_suggest_game_msg) + " " + suggestedGame.getText().toString() + "?");
                            builder.setPositiveButton(R.string.discard_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    refNextMeeting.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.child("games").hasChild(suggestedGame.getText().toString())){
                                                Toast.makeText(GamesActivity.this, R.string.games_suggest_game_alreadySuggested, Toast.LENGTH_SHORT).show();
                                            }else {
                                                Map<String, Object> newGame = new HashMap<>();
                                                newGame.put("votes", 0);
                                                newGame.put("recommender", auth.getUid());
                                                refNextMeeting.child("games").child(suggestedGame.getText().toString()).setValue(newGame).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            Toast.makeText(GamesActivity.this, R.string.games_suggest_game_successfull, Toast.LENGTH_SHORT).show();
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

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(GamesActivity.this, MainActivity.class));
        finish();
    }
}