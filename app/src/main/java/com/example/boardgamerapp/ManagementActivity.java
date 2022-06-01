package com.example.boardgamerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ManagementActivity extends AppCompatActivity {
    private Spinner dayspinner;
    private Spinner userspinner;
    private Button timeButton;
    private int hour, minute;
    private String userName;
    private String userUid;
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private ArrayList<User> userList;
    private Button adminStatus;
    private Button deleteUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management);

        overridePendingTransition(com.google.android.material.R.anim.abc_popup_enter, com.google.android.material.R.anim.abc_popup_exit);

        dayspinner = findViewById(R.id.day_spinner);
        userspinner = findViewById(R.id.user_spinner);
        timeButton = findViewById(R.id.time_picker);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        userList = new ArrayList<User>();
        adminStatus = findViewById(R.id.adminStatus);
        deleteUser = findViewById(R.id.deleteUser);

        adminStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView item = findViewById(R.id.nameTextView);
                if(item.getText().toString().equals("choose a user")){
                    Toast.makeText(ManagementActivity.this, "please choose a user!", Toast.LENGTH_SHORT).show();
                } else {
                String uid = findViewById(R.id.nameTextView).getTag().toString();
                DatabaseReference ref = db.getReference("users/");
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String status = snapshot.child(uid).child("status").getValue().toString();
                        if(!status.equals("admin")) {
                            ref.child(uid).child("status").setValue("admin");
                        } else {
                            Toast.makeText(ManagementActivity.this, "admin", Toast.LENGTH_SHORT).show();
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                }

            }
        });

        userList.add(new User("1","choose a user"));
        DatabaseReference ref = db.getReference("users/");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    userUid = dataSnapshot.child("id").getValue().toString();
                    userName = dataSnapshot.child("firstname").getValue().toString() + " " + dataSnapshot.child("lastname").getValue().toString();
                    userList.add(new User(userUid, userName));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        SpinnerAdapter customAdapter = new UserSpinnerAdapter(ManagementActivity.this, R.layout.user_spinner_adapter, userList);
        userspinner.setAdapter(customAdapter);

        TextView chooser = findViewById(R.id.choose_user);

        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(this,
                R.array.days_array, R.layout.spinner_item);
        dayspinner.setAdapter(dayAdapter);

        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popTimePicker(view);
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