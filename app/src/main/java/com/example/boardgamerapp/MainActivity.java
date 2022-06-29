package com.example.boardgamerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    private ImageButton menu, editMeetingBtn;
    private Button late, rating, games, cannotTakePartBtn;
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private MenuItem acc, logout, mngmnt;
    private TextView day, time, host, address, greetText, allParticipants, nextMeeting, notTakingPart, lateParticipantsLabel, lateParticipants;
    private LocalDate inputDate;
    private String date, userLatetime_txt, allAdmins;
    private int hour, minute;
    private HashMap<String, Object> participants;
    private Boolean meetingCanceled, isHost;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
        firebaseMessaging.subscribeToTopic("next_meeting");

        overridePendingTransition(com.google.android.material.R.anim.abc_popup_enter, com.google.android.material.R.anim.abc_popup_exit);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance("https://board-gamer-app-ff958-default-rtdb.firebaseio.com/");
        menu = findViewById(R.id.main_menuBtn);
        late = findViewById(R.id.main_lateBtn);
        rating = findViewById(R.id.main_ratingBtn);
        acc = findViewById(R.id.menu_acc);
        logout = findViewById(R.id.menu_logout);
        mngmnt = findViewById(R.id.menu_mngmnt);
        games = findViewById(R.id.main_gamesBtn);
        day = findViewById(R.id.main_mtng_day);
        time = findViewById(R.id.main_mtng_time);
        host = findViewById(R.id.main_mtng_host);
        address = findViewById(R.id.main_mtng_address);
        greetText = findViewById(R.id.greetText);
        date = "01/01/2000";
        participants = new HashMap<>();
        allParticipants = findViewById(R.id.main_mtng_allParticipants);
        cannotTakePartBtn = findViewById(R.id.main_cannotTakePart);
        nextMeeting = findViewById(R.id.main_nextMeeting);
        notTakingPart = findViewById(R.id.not_taking_part_txt);
        editMeetingBtn = findViewById(R.id.editMeetingBtn);
        lateParticipantsLabel = findViewById(R.id.late_time_label);
        lateParticipants = findViewById(R.id.late_participants);
        userLatetime_txt = null;
        allAdmins = null;

        lateParticipants.setVisibility(View.GONE);
        lateParticipantsLabel.setVisibility(View.GONE);

        findViewById(R.id.loadingPanelMain).setVisibility(View.VISIBLE);
        findViewById(R.id.linearLayoutMain).setVisibility(View.GONE);

        DatabaseReference refUser = db.getReference("users");
        DatabaseReference refLastGamenight = db.getReference("last gamenight");
        DatabaseReference refParticipants = db.getReference("next meeting/participants");
        DatabaseReference refNextMeeting = db.getReference("next meeting");
        DatabaseReference refCurrentUser = db.getReference("users/"+auth.getUid());

        refUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allAdmins = null;
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Boolean isAdmin = dataSnapshot.child("isAdmin").getValue(Boolean.class);
                    String name = dataSnapshot.child("firstname").getValue().toString() + " " + dataSnapshot.child("lastname").getValue().toString();
                    if(isAdmin){
                        if(allAdmins == null){
                            allAdmins = name;
                        } else {
                            allAdmins = allAdmins + ", " + name;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        refNextMeeting.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                date = snapshot.child("date").getValue().toString();
                inputDate = LocalDate.parse(date);
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern(getText(R.string.date_format).toString());
                String date1 = inputDate.format(dtf);
                String dayOfWeek = snapshot.child("day").getValue().toString();
                int id = getResources().getIdentifier(dayOfWeek, "string", "com.example.boardgamerapp");
                day.setText(date1 + "\n" + "(" + getText(id) + ")");
                hour = snapshot.child("hour").getValue(Integer.class);
                minute = snapshot.child("minute").getValue(Integer.class);
                time.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
                host.setText(snapshot.child("host").getValue().toString());
                address.setText(snapshot.child("address").getValue().toString());

                meetingCanceled = snapshot.child("isCanceled").getValue(Boolean.class);
                if(meetingCanceled){
                    notTakingPart.setText(R.string.appointment_meeting_canceled_label);
                    notTakingPart.setTextColor(Color.RED);
                } else {
                    if(!snapshot.child("participants").hasChild(auth.getUid())){
                        notTakingPart.setText(R.string.user_not_taking_part);
                        notTakingPart.setTextColor(Color.RED);
                    } else {
                        notTakingPart.setText(null);
                    }
                }
                findViewById(R.id.loadingPanelMain).setVisibility(View.GONE);
                findViewById(R.id.linearLayoutMain).setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        refParticipants.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                participants.clear();
                allParticipants.setText(null);
                lateParticipants.setText(null);
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String uid = dataSnapshot.getKey().toString();
                    String name = dataSnapshot.child("name").getValue().toString();
                    participants.put(uid, name);
                    String newAllParticipants;
                    if(TextUtils.isEmpty(allParticipants.getText())){
                        newAllParticipants = name;
                    } else {
                        newAllParticipants = allParticipants.getText() + ", " + name;
                    }
                    allParticipants.setText(newAllParticipants);
                    if(dataSnapshot.hasChild("latetime")){
                        if(dataSnapshot.child("latetime").getValue(Integer.class) != 0){
                            userLatetime_txt = String.valueOf(dataSnapshot.child("latetime").getValue(Integer.class));
                            if(TextUtils.isEmpty(lateParticipants.getText())){
                                lateParticipants.setText(name + ": " + userLatetime_txt + " " + getText(R.string.late_late_participants_min));
                            } else {
                                lateParticipants.setText(lateParticipants.getText().toString() + ", " + name + ": " + userLatetime_txt + " " + getText(R.string.late_late_participants_min));
                            }
                        }
                    }

                }
                if(participants.isEmpty()){
                    allParticipants.setText(R.string.main_canceled);
                    allParticipants.setTextColor(Color.RED);
                }
                if(!TextUtils.isEmpty(lateParticipants.getText())){
                    lateParticipantsLabel.setVisibility(View.VISIBLE);
                    lateParticipants.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        refCurrentUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                greetText.setText(getText(R.string.greetText) +" "+ snapshot.child("firstname").getValue().toString()+"!");
                isHost = snapshot.child("isHost").getValue(Boolean.class);
                if(isHost == false ){
                    editMeetingBtn.setVisibility(View.GONE);
                }
                if(snapshot.child("status").getValue().toString().equals("deactivated")){
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        cannotTakePartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refParticipants.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(isHost){
                            Toast.makeText(MainActivity.this, R.string.main_cannot_take_part_host, Toast.LENGTH_SHORT).show();
                        }else if(!snapshot.hasChild(auth.getUid())){
                            Toast.makeText(MainActivity.this, R.string.user_not_taking_part_already, Toast.LENGTH_SHORT).show();
                        } else if(meetingCanceled){
                            Toast.makeText(MainActivity.this, R.string.appointment_meeting_canceled_label, Toast.LENGTH_SHORT).show();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setCancelable(true);
                            builder.setTitle(R.string.main_cannotTakePart);
                            builder.setMessage(R.string.main_cannot_take_part_msg);
                            builder.setPositiveButton(R.string.discard_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    refParticipants.child(auth.getUid()).removeValue();
                                }
                            });
                            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

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

        editMeetingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AppointmentActivity.class));
                finish();
            }
        });

        games.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refParticipants.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(!snapshot.hasChild(auth.getUid())){
                            Toast.makeText(MainActivity.this, R.string.user_not_taking_part, Toast.LENGTH_SHORT).show();
                        } else {
                            startActivity(new Intent(MainActivity.this, GamesActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
            }
        });

        late.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refParticipants.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(isHost){
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setCancelable(true);
                            builder.setTitle(R.string.main_late_host_title);
                            builder.setMessage(R.string.main_late_host_msg);
                            builder.setPositiveButton(R.string.discard_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startActivity(new Intent(MainActivity.this, AppointmentActivity.class));
                                }
                            });
                            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        } else if(!snapshot.hasChild(auth.getUid())){
                            Toast.makeText(MainActivity.this, R.string.user_not_taking_part, Toast.LENGTH_SHORT).show();
                        } else if(meetingCanceled){
                            Toast.makeText(MainActivity.this, R.string.appointment_meeting_canceled_label, Toast.LENGTH_SHORT).show();
                        } else {
                            startActivity(new Intent(MainActivity.this, LateActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        rating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RatingActivity.class));
                finish();
            }
        });



    }



    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.actions);
        popup.show();
    }


    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.menu_acc:
                startActivity(new Intent(MainActivity.this, UserAccountActivity.class));
                finish();
                return true;
            case R.id.menu_logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(true);
                builder.setTitle(R.string.logout_msg);
                builder.setPositiveButton(R.string.discard_yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                auth.signOut();
                                FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
                                firebaseMessaging.unsubscribeFromTopic("next_meeting");
                                startActivity(new Intent(MainActivity.this, LoginActivity.class));
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
                return true;
            case R.id.menu_mngmnt:
                DatabaseReference ref = db.getReference("users/"+auth.getUid().toString());
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Boolean adminStatus = (Boolean) snapshot.child("isAdmin").getValue();
                        if (adminStatus == true) {
                            startActivity(new Intent(MainActivity.this, ManagementActivity.class));
                            finish();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle(R.string.main_contact_admin_label);
                            builder.setMessage(getText(R.string.main_contact_admin_msg).toString() +"\r\n" + allAdmins);
                            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

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

                    return true;

        }
        return false;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(true);
        builder.setTitle(R.string.leave_app);
        builder.setPositiveButton(R.string.discard_yes,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent a = new Intent(Intent.ACTION_MAIN);
                a.addCategory(Intent.CATEGORY_HOME);
                a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(a);
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

    }

}