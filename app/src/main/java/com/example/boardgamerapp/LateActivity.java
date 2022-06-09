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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LateActivity extends AppCompatActivity {
    private EditText custom_time;
    private ImageButton fiveMin, fifteenMin, thirtyMin, confirmCustomTime;
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private int latetime, customLatetime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_late);

        overridePendingTransition(com.google.android.material.R.anim.abc_popup_enter, com.google.android.material.R.anim.abc_popup_exit);

        custom_time = findViewById(R.id.custom_timeEdit);
        fiveMin = findViewById(R.id.fiveMin_btn);
        fifteenMin = findViewById(R.id.fifteenMin_btn);
        thirtyMin = findViewById(R.id.thirtyMin_btn);
        confirmCustomTime = findViewById(R.id.late_confirmBtn);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance("https://board-gamer-app-ff958-default-rtdb.firebaseio.com");
        latetime = 0;
        customLatetime = 0;

        DatabaseReference refNextMeeting = db.getReference("next meeting/");

        refNextMeeting.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("participants").hasChild(auth.getUid())){
                    if(snapshot.child("participants").child(auth.getUid()).hasChild("latetime")){
                        latetime = snapshot.child("participants").child(auth.getUid()).child("latetime").getValue(Integer.class);
                    }
                    fiveMin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(snapshot.child("participants").child(auth.getUid()).hasChild("latetime")){
                                if(snapshot.child("participants").child(auth.getUid()).child("latetime").getValue(Integer.class) == 5){
                                    Toast.makeText(LateActivity.this, R.string.late_time_already_set_5_min, Toast.LENGTH_SHORT).show();
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(LateActivity.this);
                                    builder.setCancelable(true);
                                    builder.setTitle(R.string.late_time_5_min_title);
                                    builder.setMessage(R.string.late_time_5_min_msg_change);
                                    builder.setPositiveButton(R.string.discard_yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            refNextMeeting.child("participants").child(auth.getUid()).child("latetime").setValue(5).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(LateActivity.this, R.string.late_time_changed_to_5_min, Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
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
                            }else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(LateActivity.this);
                                builder.setCancelable(true);
                                builder.setTitle(R.string.late_time_5_min_title);
                                builder.setMessage(R.string.late_time_5_min_msg);
                                builder.setPositiveButton(R.string.discard_yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        refNextMeeting.child("participants").child(auth.getUid()).child("latetime").setValue(5).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(LateActivity.this, R.string.late_time_set_to_5_min, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
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
                    });
                    fifteenMin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(snapshot.child("participants").child(auth.getUid()).hasChild("latetime")){
                                if(snapshot.child("participants").child(auth.getUid()).child("latetime").getValue(Integer.class) == 15){
                                    Toast.makeText(LateActivity.this, R.string.late_time_already_set_15_min, Toast.LENGTH_SHORT).show();
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(LateActivity.this);
                                    builder.setCancelable(true);
                                    builder.setTitle(R.string.late_time_15_min_title);
                                    builder.setMessage(R.string.late_time_15_min_msg_change);
                                    builder.setPositiveButton(R.string.discard_yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            refNextMeeting.child("participants").child(auth.getUid()).child("latetime").setValue(15).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(LateActivity.this, R.string.late_time_changed_to_15_min, Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
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
                            }else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(LateActivity.this);
                                builder.setCancelable(true);
                                builder.setTitle(R.string.late_time_15_min_title);
                                builder.setMessage(R.string.late_time_15_min_msg);
                                builder.setPositiveButton(R.string.discard_yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        refNextMeeting.child("participants").child(auth.getUid()).child("latetime").setValue(15).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(LateActivity.this, R.string.late_time_set_to_15_min, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
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
                    });
                    thirtyMin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(snapshot.child("participants").child(auth.getUid()).hasChild("latetime")){
                                if(snapshot.child("participants").child(auth.getUid()).child("latetime").getValue(Integer.class) == 30){
                                    Toast.makeText(LateActivity.this, R.string.late_time_already_set_30_min, Toast.LENGTH_SHORT).show();
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(LateActivity.this);
                                    builder.setCancelable(true);
                                    builder.setTitle(R.string.late_time_30_min_title);
                                    builder.setMessage(R.string.late_time_30_min_msg_change);
                                    builder.setPositiveButton(R.string.discard_yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            refNextMeeting.child("participants").child(auth.getUid()).child("latetime").setValue(30).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(LateActivity.this, R.string.late_time_changed_to_30_min, Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
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
                            }else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(LateActivity.this);
                                builder.setCancelable(true);
                                builder.setTitle(R.string.late_time_30_min_title);
                                builder.setMessage(R.string.late_time_30_min_msg);
                                builder.setPositiveButton(R.string.discard_yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        refNextMeeting.child("participants").child(auth.getUid()).child("latetime").setValue(30).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(LateActivity.this, R.string.late_time_set_to_30_min, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
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
                    });
                    confirmCustomTime.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            boolean digitsOnly = TextUtils.isDigitsOnly(custom_time.getText());
                            if(TextUtils.isEmpty(custom_time.getText())) {
                                Toast.makeText(LateActivity.this, R.string.late_no_custom_time, Toast.LENGTH_SHORT).show();
                            } else if(digitsOnly == false){
                                Toast.makeText(LateActivity.this, R.string.late_check_custom_time, Toast.LENGTH_SHORT).show();
                            } else {
                                customLatetime = Integer.parseInt(custom_time.getText().toString());
                                if(snapshot.child("participants").child(auth.getUid()).hasChild("latetime")){
                                    if(snapshot.child("participants").child(auth.getUid()).child("latetime").getValue(Integer.class) == customLatetime){
                                        Toast.makeText(LateActivity.this, getText(R.string.late_time_already_set_custom_time1).toString() + " " + customLatetime + " " + getText(R.string.late_time_already_set_custom_time2).toString(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(LateActivity.this);
                                        builder.setCancelable(true);
                                        builder.setTitle(getText(R.string.late_time_custom_time_title1).toString() + " " + customLatetime + " " + getText(R.string.late_time_custom_time_title2).toString());
                                        builder.setMessage(getText(R.string.late_time_custom_time_msg_change1).toString() + " " + customLatetime + " " + getText(R.string.late_time_custom_time_msg_change2).toString());
                                        builder.setPositiveButton(R.string.discard_yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                refNextMeeting.child("participants").child(auth.getUid()).child("latetime").setValue(customLatetime).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            custom_time.setText(null);
                                                            Toast.makeText(LateActivity.this, getText(R.string.late_time_changed_to_custom_time1).toString() + " " + customLatetime + " " + getText(R.string.late_time_changed_to_custom_time2).toString(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
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
                                }else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(LateActivity.this);
                                    builder.setCancelable(true);
                                    builder.setTitle(getText(R.string.late_time_custom_time_title1).toString() + " " + customLatetime + " " + getText(R.string.late_time_custom_time_title2).toString());
                                    builder.setMessage(getText(R.string.late_time_custom_time_msg1).toString() + " " + customLatetime + " " + getText(R.string.late_time_custom_time_msg2).toString());
                                    builder.setPositiveButton(R.string.discard_yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            refNextMeeting.child("participants").child(auth.getUid()).child("latetime").setValue(customLatetime).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        custom_time.setText(null);
                                                        Toast.makeText(LateActivity.this, getText(R.string.late_time_set_to_custom_time1).toString() + " " + customLatetime + " " + getText(R.string.late_time_set_to_custom_time2).toString(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
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
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(LateActivity.this, MainActivity.class));
        finish();
    }
}