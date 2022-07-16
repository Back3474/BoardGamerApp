package com.example.boardgamerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
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
import java.util.Locale;

public class ManagementActivity extends AppCompatActivity {
    private Spinner dayspinner;
    private Spinner userspinner;
    private Button timeButton;
    private int hour, minute, currentTimeHour, currentTimeMin;
    private String userName;
    private String userUid;
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private ArrayList<User> userList;
    private Button adminStatusBtn;
    private Button deactivateUser;
    private ImageButton saveChangedAppointmentBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management);

        overridePendingTransition(com.google.android.material.R.anim.abc_popup_enter, com.google.android.material.R.anim.abc_popup_exit);

        dayspinner = findViewById(R.id.day_spinner);
        userspinner = findViewById(R.id.user_spinner);
        timeButton = findViewById(R.id.time_picker);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance("https://board-gamer-app-ff958-default-rtdb.firebaseio.com");
        userList = new ArrayList<User>();
        adminStatusBtn = findViewById(R.id.adminStatus);
        deactivateUser = findViewById(R.id.deactivateUser);
        saveChangedAppointmentBtn = findViewById(R.id.saveChangedAppointmentTime);
        DatabaseReference ref = db.getReference("users/");
        DatabaseReference ref1 = db.getReference("default periodic appointment/");
        
        adminStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView item = findViewById(R.id.nameTextView);
                String userName = item.getText().toString();
                String userUid = item.getTag().toString();

                if (userName.equals(getText(R.string.mngmnt_select_user_admin).toString())){
                    Toast.makeText(ManagementActivity.this, R.string.mngmnt_select_user_admin_toast, Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ManagementActivity.this);
                    builder.setCancelable(true);
                    builder.setTitle(R.string.mngmnt_admin_title);
                    builder.setMessage(getText(R.string.mngmnt_admin_msg) + " (" + userName + ")");
                    builder.setPositiveButton(R.string.discard_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Boolean adminStatus = (Boolean) snapshot.child(userUid).child("isAdmin").getValue();
                                    if (adminStatus == true) {
                                        Toast.makeText(ManagementActivity.this, R.string.mngmnt_is_admin_already, Toast.LENGTH_SHORT).show();
                                    } else {
                                        ref.child(userUid).child("isAdmin").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(ManagementActivity.this, R.string.mngmnt_acc_set_to_admin, Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(ManagementActivity.this, R.string.mngmnt_acc_set_to_admin_failed, Toast.LENGTH_SHORT).show();
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

        deactivateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView item = findViewById(R.id.nameTextView);
                String userName = item.getText().toString();
                String userUid = item.getTag().toString();

                if (userName.equals(getText(R.string.mngmnt_select_user_admin).toString())){
                    Toast.makeText(ManagementActivity.this, R.string.mngmnt_select_user_admin_toast, Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ManagementActivity.this);
                    builder.setCancelable(true);
                    builder.setTitle(R.string.mngmnt_user_deactivate_title);
                    builder.setMessage(getText(R.string.mngmnt_user_deactivate_msg).toString() + " (" +userName + ")");
                    builder.setPositiveButton(R.string.discard_yes,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ref.child(userUid).child("status").setValue("dectivated");
                                    ref.child(userUid).child("isAdmin").setValue(false);
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

        userList.add(new User("1", getText(R.string.mngmnt_select_user_admin).toString()));

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                userList.add(new User("1", getText(R.string.mngmnt_select_user_admin).toString()));
                TextView item = findViewById(R.id.nameTextView);
                item.setText(userList.get(0).getName());
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    userUid = dataSnapshot.child("id").getValue().toString();
                    userName = dataSnapshot.child("firstname").getValue().toString() + " " + dataSnapshot.child("lastname").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    Boolean admin = dataSnapshot.child("isAdmin").getValue(Boolean.class);
                    if(userStatus.equals("active") && !admin) {
                        userList.add(new User(userUid, userName));
                    }
                }

                if(snapshot.child(auth.getUid()).child("status").getValue().toString().equals("deactivated")){
                    startActivity(new Intent(ManagementActivity.this, LoginActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        SpinnerAdapter customAdapter = new UserSpinnerAdapter(ManagementActivity.this, R.layout.user_spinner_adapter, userList);
        userspinner.setAdapter(customAdapter);

        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(this,
                R.array.days_array, R.layout.spinner_item);
        dayspinner.setAdapter(dayAdapter);

        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popTimePicker(view);
            }
        });

        ref1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                TextView day = findViewById(R.id.mngmnt_currentDay);
                TextView time = findViewById(R.id.mngmnt_currentTime);
                String currDay = snapshot.child("day").getValue().toString();
                day.setText(currDay);
                currentTimeHour = snapshot.child("hour").getValue(Integer.class);
                currentTimeMin = snapshot.child("minute").getValue(Integer.class);
                time.setText(String.format(Locale.getDefault(), "%02d:%02d", currentTimeHour, currentTimeMin));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        saveChangedAppointmentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView dayItem = findViewById(R.id.day_spinner_item);

                AlertDialog.Builder builder = new AlertDialog.Builder(ManagementActivity.this);
                builder.setCancelable(true);
                builder.setTitle(R.string.mngmnt_appointment_change_title);
                builder.setMessage(getText(R.string.mngmnt_appointment_change_msg).toString() + " " + dayItem.getText().toString() + ", " + String.format(Locale.getDefault(), "%02d:%02d", hour, minute) + "?");
                builder.setPositiveButton(R.string.discard_yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ref1.child("day").setValue(dayItem.getText().toString());
                                ref1.child("hour").setValue(hour);
                                ref1.child("minute").setValue(minute);
                                dayItem.setText(R.string.mngmnt_select_day);
                                timeButton.setText(R.string.mngmnt_selectTime);
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
        });

    }


    private void popTimePicker(View view) {

        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                hour = selectedHour;
                minute = selectedMinute;
                timeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
            }
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hour, minute, true);

        timePickerDialog.show();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ManagementActivity.this, MainActivity.class));
        finish();
    }
}