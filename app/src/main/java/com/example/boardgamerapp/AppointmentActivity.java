package com.example.boardgamerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppointmentActivity extends AppCompatActivity {
    private FirebaseDatabase db;
    private FirebaseAuth auth;
    private LocalDate today;
    private DateTimeFormatter dtf;
    private Button editMeetingBtn, cancelMeetingBtn, confirmMeetingEndBtn;
    private TextView nxtMeetingDay, nxtMeetingTime, nxtMeetingHost, nxtMeetingAddress, meetingLabel, meetingIsCanceled;
    private Boolean meetingCanceled, clickedForEdit;
    private ViewSwitcher viewSwitcherDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        overridePendingTransition(com.google.android.material.R.anim.abc_popup_enter, com.google.android.material.R.anim.abc_popup_exit);

        editMeetingBtn = findViewById(R.id.meeting_edit_mtng_btn);
        cancelMeetingBtn = findViewById(R.id.meeting_cancel_mtng_btn);
        confirmMeetingEndBtn = findViewById(R.id.meeting_confirm_mtng_end_btn);
        nxtMeetingDay = findViewById(R.id.meeting_nxt_mtng_day);
        nxtMeetingTime = findViewById(R.id.meeting_nxt_mtng_time);
        nxtMeetingHost = findViewById(R.id.meeting_nxt_mtng_host);
        nxtMeetingAddress = findViewById(R.id.meeting_nxt_mtng_address);
        meetingLabel = findViewById(R.id.meeting_label);
        meetingIsCanceled = findViewById(R.id.meetingIsCanceled_txt);
        meetingCanceled = true;
        clickedForEdit = false;
        dtf = DateTimeFormatter.ofPattern((String) getText(R.string.date_format));
        today = LocalDate.now();
        viewSwitcherDay = findViewById(R.id.dayViewSwitcher);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance("https://board-gamer-app-ff958-default-rtdb.firebaseio.com");

        DatabaseReference refNextMeeting = db.getReference("next meeting");

        refNextMeeting.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                meetingCanceled = snapshot.child("isCanceled").getValue(Boolean.class);
                if(meetingCanceled){
                    meetingIsCanceled.setText("Meeting has been canceled!");
                    meetingIsCanceled.setTextColor(Color.RED);
                } else {
                    meetingIsCanceled.setText(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        editMeetingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!clickedForEdit){
                    clickedForEdit = true;
                    viewSwitcherDay.showNext();
                    EditText editDay = viewSwitcherDay.findViewById(R.id.meeting_nxt_mtng_day_edit);
                    editMeetingBtn.setText(R.string.appointment_cancel);
                        editDay.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                if(charSequence.toString().length() != 0){
                                    editMeetingBtn.setText(R.string.appointment_confirm_changes);
                                } else {
                                    editMeetingBtn.setText(R.string.appointment_cancel);
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {

                            }
                        });
                    } else {
                    viewSwitcherDay.showPrevious();
                    clickedForEdit = false;
                    editMeetingBtn.setText(R.string.meeting_edit_mtng_btn);
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(AppointmentActivity.this, MainActivity.class));
        finish();
    }
}
