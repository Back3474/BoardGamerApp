package com.example.boardgamerapp;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppointmentActivity extends AppCompatActivity {
    private FirebaseDatabase db;
    private FirebaseAuth auth;
    FirebaseMessaging firebaseMessaging;
    private LocalDate today;
    private DateTimeFormatter dtf;
    private Button editMeetingBtn, cancelMeetingBtn, confirmMeetingEndBtn, selectTimeBtn, selectDateBtn;
    private TextView nxtMeetingDay, nxtMeetingTime, nxtMeetingHost, nxtMeetingAddress, meetingLabel, meetingIsCanceled, hostLabel;
    private Boolean meetingCanceled, clickedForEdit, done, dateSelected, timeSelected, newMeetingGenerated;
    private ViewSwitcher viewSwitcherDay, viewSwitcherTime, viewSwitcherAddress;
    private ImageButton confirmChanges;
    private EditText editAddress;
    private int hour, minute, nextMtngHour, nextMtngMinute, appointmentDefHour, appointmentDefMinute, changes;
    private String date, appointmentDefDay, defDayShort, newHostId, newHostName, newHostAddress, newHost, lastGamenightDate, lastGamenightHost, lastGamenightHostId;
    private LocalDate inputDate, nextMeetingDate;
    private DatePickerDialog datePickerDialog;
    private LinearLayout appontmentLayout;
    private RelativeLayout appointmentLoading;
    ArrayList nextMeetingParticipants = new ArrayList<String>();
    ArrayList users = new ArrayList<String>();
    Map lastMtngParticipants = new HashMap<String, String>();

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
        hostLabel = findViewById(R.id.label_meeting_nxt_mtng_host);
        meetingIsCanceled = findViewById(R.id.meetingIsCanceled_txt);
        meetingCanceled = false;
        clickedForEdit = false;
        dateSelected = false;
        timeSelected = false;
        newMeetingGenerated = false;
        dtf = DateTimeFormatter.ofPattern((String) getText(R.string.date_format));
        today = LocalDate.now();
        viewSwitcherDay = findViewById(R.id.dayViewSwitcher);
        viewSwitcherTime = findViewById(R.id.timeViewSwitcher);
        viewSwitcherAddress = findViewById(R.id.addressViewSwitcher);
        confirmChanges = findViewById(R.id.confirmMeetingChangesBtn);
        selectDateBtn = viewSwitcherDay.findViewById(R.id.meeting_nxt_mtng_date_btn);
        editAddress = viewSwitcherAddress.findViewById(R.id.meeting_nxt_mtng_address_edit);
        selectTimeBtn = viewSwitcherTime.findViewById(R.id.meeting_nxt_mtng_time_btn);
        appontmentLayout = findViewById(R.id.layoutAppointment);
        appointmentLoading = findViewById(R.id.loadingPanelAppointment);
        appointmentLoading.setVisibility(View.GONE);
        initDatePicker();


        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance("https://board-gamer-app-ff958-default-rtdb.firebaseio.com");
        firebaseMessaging = FirebaseMessaging.getInstance();

        DatabaseReference refNextMeeting = db.getReference("next meeting");
        DatabaseReference refUsers = db.getReference("users");
        DatabaseReference refLastGamenight = db.getReference("last gamenight");
        DatabaseReference refDefAppointment = db.getReference("default periodic appointment");

        refDefAppointment.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appointmentDefDay = snapshot.child("day").getValue().toString();
                defDayShort = makeDefDayShort(appointmentDefDay);
                appointmentDefHour = snapshot.child("hour").getValue(Integer.class);
                appointmentDefMinute = snapshot.child("minute").getValue(Integer.class);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        View.OnClickListener generateNewMeetingOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(AppointmentActivity.this);
                builder.setCancelable(true);
                builder.setTitle(R.string.appointment_generate_new_meeting_title);
                builder.setMessage(R.string.appointment_generate_new_meeting_msg);
                builder.setPositiveButton(R.string.discard_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        appointmentLoading.setVisibility(View.VISIBLE);
                        appontmentLayout.setVisibility(View.GONE);

                        for(int uI = 0; uI < users.size(); uI++){
                            String userUid = users.get(uI).toString();

                            refUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String userName = snapshot.child(userUid.toString()).child("firstname").getValue().toString() + " " + snapshot.child(userUid.toString()).child("lastname").getValue().toString();
                                    refNextMeeting.child("participants").child(userUid).child("name").setValue(userName);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            refNextMeeting.child("participants").child(userUid).child("isTakingPart").setValue(true);
                            refNextMeeting.child("participants").child(userUid).child("latetime").setValue(0);
                        }

                        int dayOfWeek = getDayOfWeek(appointmentDefDay);
                        nextMeetingDate = inputDate.with(TemporalAdjusters.next(DayOfWeek.of(dayOfWeek)));
                        if(DAYS.between(inputDate, nextMeetingDate) < 5){
                            nextMeetingDate = nextMeetingDate.with(TemporalAdjusters.next(DayOfWeek.of(dayOfWeek)));
                        }


                        refUsers.child(newHost).child("isHost").setValue(true);

                        refNextMeeting.child("hostId").setValue(newHostId);
                        refNextMeeting.child("host").setValue(newHostName);
                        refNextMeeting.child("address").setValue(newHostAddress);

                        refNextMeeting.child("votes").removeValue();
                        refNextMeeting.child("games").removeValue();
                        refNextMeeting.child("isCanceled").setValue(false);

                        refNextMeeting.child("hour").setValue(appointmentDefHour);
                        refNextMeeting.child("minute").setValue(appointmentDefMinute);
                        refNextMeeting.child("day").setValue(defDayShort);
                        refNextMeeting.child("date").setValue(nextMeetingDate.toString());

                        refNextMeeting.child("changes").setValue(0);

                        refUsers.child(auth.getUid()).child("isHost").setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    startActivity(new Intent(AppointmentActivity.this, MainActivity.class));
                                    finish();
                                    Toast.makeText(AppointmentActivity.this, R.string.appointment_new_meeting_generated, Toast.LENGTH_SHORT).show();
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
        };
        View.OnClickListener confirmMeetingEndOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalDateTime now = LocalDateTime.now();
                LocalTime meetingTime = LocalTime.of(hour, minute);
                LocalDateTime meetingDateTime = LocalDateTime.of(inputDate, meetingTime);
                if (HOURS.between(meetingDateTime, now) < 0) {
                    Toast.makeText(AppointmentActivity.this, R.string.appointment_confrim_end_error, Toast.LENGTH_LONG).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AppointmentActivity.this);
                    builder.setCancelable(true);
                    builder.setTitle(R.string.appointment_confirm_meeting_end_title);
                    builder.setMessage(R.string.appointment_confirm_meeting_end_msg);
                    builder.setPositiveButton(R.string.discard_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            refLastGamenight.child("participants").removeValue();
                            refLastGamenight.child("participants").setValue(lastMtngParticipants);
                            refLastGamenight.child("ratings").removeValue();
                            refLastGamenight.child("date").setValue(lastGamenightDate);
                            refLastGamenight.child("host").setValue(lastGamenightHost);
                            refLastGamenight.child("hostId").setValue(lastGamenightHostId);

                            appointmentLoading.setVisibility(View.VISIBLE);
                            appontmentLayout.setVisibility(View.GONE);

                            for(int uI = 0; uI < users.size(); uI++){
                                String userUid = users.get(uI).toString();

                                refUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String userName = snapshot.child(userUid.toString()).child("firstname").getValue().toString() + " " + snapshot.child(userUid.toString()).child("lastname").getValue().toString();
                                        refNextMeeting.child("participants").child(userUid).child("name").setValue(userName);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                refNextMeeting.child("participants").child(userUid).child("isTakingPart").setValue(true);
                                refNextMeeting.child("participants").child(userUid).child("latetime").setValue(0);
                            }

                            int dayOfWeek = getDayOfWeek(appointmentDefDay);
                            nextMeetingDate = inputDate.with(TemporalAdjusters.next(DayOfWeek.of(dayOfWeek)));
                            if(DAYS.between(inputDate, nextMeetingDate) < 5){
                                nextMeetingDate = nextMeetingDate.with(TemporalAdjusters.next(DayOfWeek.of(dayOfWeek)));
                            }


                            refUsers.child(newHost).child("isHost").setValue(true);

                            refNextMeeting.child("hostId").setValue(newHostId);
                            refNextMeeting.child("host").setValue(newHostName);
                            refNextMeeting.child("address").setValue(newHostAddress);

                            refNextMeeting.child("votes").removeValue();
                            refNextMeeting.child("games").removeValue();
                            refNextMeeting.child("isCanceled").setValue(false);

                            refNextMeeting.child("hour").setValue(appointmentDefHour);
                            refNextMeeting.child("minute").setValue(appointmentDefMinute);
                            refNextMeeting.child("day").setValue(defDayShort);
                            refNextMeeting.child("date").setValue(nextMeetingDate.toString());

                            refNextMeeting.child("changes").setValue(0);


                            refUsers.child(auth.getUid()).child("isHost").setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        startActivity(new Intent(AppointmentActivity.this, MainActivity.class));
                                        finish();
                                        Toast.makeText(AppointmentActivity.this, R.string.appointment_new_meeting_generated, Toast.LENGTH_SHORT).show();
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
        };

        refNextMeeting.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                date = snapshot.child("date").getValue().toString();
                inputDate = LocalDate.parse(date);
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern(getText(R.string.date_format).toString());
                String date1 = inputDate.format(dtf);
                String dayOfWeek = snapshot.child("day").getValue().toString();
                int id = getResources().getIdentifier(dayOfWeek, "string", "com.example.boardgamerapp");
                nxtMeetingDay.setText(date1 + "\n" + "(" + getText(id) + ")");
                hour = snapshot.child("hour").getValue(Integer.class);
                minute = snapshot.child("minute").getValue(Integer.class);
                nxtMeetingTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
                nxtMeetingHost.setText(snapshot.child("host").getValue().toString());
                nxtMeetingAddress.setText(snapshot.child("address").getValue().toString());
                meetingCanceled = snapshot.child("isCanceled").getValue(Boolean.class);

                for(DataSnapshot dataSnapshot : snapshot.child("participants").getChildren()){
                    nextMeetingParticipants.add(dataSnapshot.getKey());
                }

                if(meetingCanceled){
                    meetingIsCanceled.setText(R.string.appointment_meeting_canceled_label);
                    meetingIsCanceled.setTextColor(Color.RED);
                    cancelMeetingBtn.setClickable(false);
                    cancelMeetingBtn.setAlpha(0.3f);
                    editMeetingBtn.setClickable(false);
                    editMeetingBtn.setAlpha(0.3f);
                    confirmMeetingEndBtn.setText(R.string.appointment_generate_new_meeting);
                    confirmMeetingEndBtn.setOnClickListener(generateNewMeetingOnClickListener);
                } else {
                    meetingIsCanceled.setText(null);
                    cancelMeetingBtn.setClickable(true);
                    cancelMeetingBtn.setAlpha(1f);
                    editMeetingBtn.setClickable(true);
                    editMeetingBtn.setAlpha(1f);
                    confirmMeetingEndBtn.setText(R.string.meeting_confirm_mtng_end_btn);
                    confirmMeetingEndBtn.setOnClickListener(confirmMeetingEndOnClickListener);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        refUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String userUid = dataSnapshot.child("id").getValue().toString();
                    String userName = dataSnapshot.child("firstname").getValue().toString() + " " + dataSnapshot.child("lastname").getValue().toString();

                    users.add(userUid);

                    Collections.sort(users, String.CASE_INSENSITIVE_ORDER);
                    int currentHostIndex = users.indexOf(auth.getUid());
                    if (currentHostIndex + 1 == users.size()) {
                        newHost = users.get(0).toString();
                    } else {
                        newHost = users.get(currentHostIndex + 1).toString();
                    }

                    newHostId = snapshot.child(newHost).child("id").getValue().toString();
                    newHostName = snapshot.child(newHost).child("firstname").getValue().toString() + " " + snapshot.child(newHost).child("lastname").getValue().toString();
                    newHostAddress = snapshot.child(newHost).child("address").getValue().toString();
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        refNextMeeting.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.child("participants").getChildren()) {
                    if(dataSnapshot.child("isTakingPart").getValue(Boolean.class)){
                        String uid = dataSnapshot.getKey();
                        String name = dataSnapshot.child("name").getValue().toString();
                        lastMtngParticipants.put(uid, name);
                    }
                }
                lastGamenightDate = snapshot.child("date").getValue().toString();
                lastGamenightHost = snapshot.child("host").getValue().toString();
                lastGamenightHostId = snapshot.child("hostId").getValue().toString();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        cancelMeetingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AppointmentActivity.this);
                builder.setCancelable(true);
                builder.setTitle(R.string.appointment_cancel_meeting_title);
                builder.setMessage(R.string.appointment_cancel_meeting_msg);
                builder.setPositiveButton(R.string.discard_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(DAYS.between(today, inputDate) < 2){
                            Toast.makeText(AppointmentActivity.this, R.string.appointment_cant_be_canceled, Toast.LENGTH_SHORT).show();
                        } else {
                            if(!meetingCanceled){
                                refNextMeeting.child("isCanceled").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            cancelMeetingBtn.setClickable(false);
                                            Toast.makeText(AppointmentActivity.this, R.string.appointment_cancel_success, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
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
        });

        editMeetingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!clickedForEdit){
                    AlertDialog.Builder builder = new AlertDialog.Builder(AppointmentActivity.this);
                    builder.setTitle(R.string.appointment_change_meeting_details_title);
                    builder.setMessage(R.string.appointment_change_meeting_details_msg);
                    builder.setCancelable(true);
                    builder.setPositiveButton(R.string.discard_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            clickedForEdit = true;
                            cancelMeetingBtn.setAlpha(0.3f);
                            cancelMeetingBtn.setClickable(false);
                            confirmMeetingEndBtn.setAlpha(0.3f);
                            confirmMeetingEndBtn.setClickable(false);
                            hostLabel.setVisibility(View.GONE);
                            nxtMeetingHost.setVisibility(View.GONE);
                            viewSwitcherDay.showNext();
                            viewSwitcherTime.showNext();
                            viewSwitcherAddress.showNext();
                            confirmChanges.setVisibility(View.VISIBLE);
                            confirmChanges.setAlpha(0.3f);
                            confirmChanges.setOnClickListener(null);
                            selectDateBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    openDatePicker(view);

                                }
                            });
                            selectDateBtn.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                }

                                @Override
                                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                    confrimAllChanges();
                                    dateSelected = true;
                                }

                                @Override
                                public void afterTextChanged(Editable editable) {

                                }
                            });
                            editAddress.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                }

                                @Override
                                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                    if(charSequence.toString().length() == 0){
                                        confirmChanges.setAlpha(0.3f);
                                        confirmChanges.setOnClickListener(null);
                                    } else {
                                        confrimAllChanges();
                                    }
                                }

                                @Override
                                public void afterTextChanged(Editable editable) {

                                }
                            });
                            selectTimeBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    popTimePicker(view);

                                }
                            });
                            selectTimeBtn.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                }

                                @Override
                                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                    confrimAllChanges();
                                    timeSelected = true;
                                }

                                @Override
                                public void afterTextChanged(Editable editable) {

                                }
                            });
                            editMeetingBtn.setText(R.string.appointment_cancel);
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    } else {
                    if(!selectDateBtn.getText().toString().equals(getText(R.string.appointment_select_date)) || !TextUtils.isEmpty(editAddress.getText()) || !selectTimeBtn.getText().toString().equals(getText(R.string.appointment_select_time))){
                        AlertDialog.Builder builder = new AlertDialog.Builder(AppointmentActivity.this);
                        builder.setTitle(R.string.discard_title);
                        builder.setMessage(R.string.discard_msg);
                        builder.setCancelable(true);
                        builder.setPositiveButton(R.string.discard_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                hostLabel.setVisibility(View.VISIBLE);
                                nxtMeetingHost.setVisibility(View.VISIBLE);
                                viewSwitcherDay.showPrevious();
                                viewSwitcherTime.showPrevious();
                                viewSwitcherAddress.showPrevious();
                                clickedForEdit = false;
                                editMeetingBtn.setText(R.string.meeting_edit_mtng_btn);
                                confirmChanges.setVisibility(View.GONE);
                                selectDateBtn.setText(R.string.appointment_select_date);
                                editAddress.setText(null);
                                selectTimeBtn.setText(R.string.appointment_select_time);
                                cancelMeetingBtn.setAlpha(1f);
                                cancelMeetingBtn.setClickable(true);
                                confirmMeetingEndBtn.setAlpha(1f);
                                confirmMeetingEndBtn.setClickable(true);
                            }
                        });
                        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } else {
                        hostLabel.setVisibility(View.VISIBLE);
                        nxtMeetingHost.setVisibility(View.VISIBLE);
                        viewSwitcherDay.showPrevious();
                        viewSwitcherTime.showPrevious();
                        viewSwitcherAddress.showPrevious();
                        clickedForEdit = false;
                        editMeetingBtn.setText(R.string.meeting_edit_mtng_btn);
                        confirmChanges.setVisibility(View.GONE);
                        selectDateBtn.setText(R.string.appointment_select_date);
                        editAddress.setText(null);
                        selectTimeBtn.setText(R.string.appointment_select_time);
                        cancelMeetingBtn.setAlpha(1f);
                        cancelMeetingBtn.setClickable(true);
                        confirmMeetingEndBtn.setAlpha(1f);
                        confirmMeetingEndBtn.setClickable(true);
                    }

                }

            }
        });
    }


    private int getDayOfWeek(String appointmentDefDay) {
        int dayOfWeek = 0;
        if(appointmentDefDay.equals("Monday")){
            dayOfWeek = 1;
        }
        if(appointmentDefDay.equals("Tuesday")){
            dayOfWeek = 2;
        }
        if(appointmentDefDay.equals("Wednesday")){
            dayOfWeek = 3;
        }
        if(appointmentDefDay.equals("Thursday")){
            dayOfWeek = 4;
        }
        if(appointmentDefDay.equals("Friday")){
            dayOfWeek = 5;
        }
        if(appointmentDefDay.equals("Saturday")){
            dayOfWeek = 6;
        }
        if(appointmentDefDay.equals("Sunday")){
            dayOfWeek = 7;
        }
        return dayOfWeek;
    }

    private String makeDefDayShort(String appointmentDefDay) {
        String defDayShort = appointmentDefDay.substring(0, 3);
        char c[] = defDayShort.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        defDayShort = new String(c);
        return defDayShort;
    }

    private void confrimAllChanges() {
        changes = 0;
        DatabaseReference refChanges = db.getReference("next meeting/changes");
        refChanges.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                changes = snapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        confirmChanges.setAlpha(1.0f);
        if(!confirmChanges.hasOnClickListeners()){
            confirmChanges.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AppointmentActivity.this);
                    builder.setTitle(R.string.appointment_confirm_meeting_changes_title);
                    builder.setMessage(R.string.appointment_confirm_meeting_changes_msg);
                    builder.setCancelable(true);
                    builder.setPositiveButton(R.string.discard_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int year = datePickerDialog.getDatePicker().getYear();
                            int month = datePickerDialog.getDatePicker().getMonth();
                            month = month + 1;
                            int day = datePickerDialog.getDatePicker().getDayOfMonth();
                            LocalDate newLocalDate = LocalDate.of(year, month, day);
                            LocalDate today = LocalDate.now();
                            String newDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day);
                            String dayOfWeek = LocalDate.parse(newDate).getDayOfWeek().name().toString();
                            if(dayOfWeek == "MONDAY"){
                                dayOfWeek = "mon";
                            }
                            if(dayOfWeek == "TUESDAY"){
                                dayOfWeek = "tue";
                            }
                            if(dayOfWeek == "WEDNESDAY"){
                                dayOfWeek = "wed";
                            }
                            if(dayOfWeek == "THURSDAY"){
                                dayOfWeek = "thu";
                            }
                            if(dayOfWeek == "FRIDAY"){
                                dayOfWeek = "fri";
                            }
                            if(dayOfWeek == "SATURDAY"){
                                dayOfWeek = "sat";
                            }
                            if(dayOfWeek == "SUNDAY"){
                                dayOfWeek = "sun";
                            }

                            int newHour = hour;
                            int newMinute = minute;

                            String newAddress = editAddress.getText().toString();

                            DatabaseReference ref = db.getReference("next meeting");
                            Map newMeeting = new HashMap<String, Object>();

                            newMeeting.clear();
                            done = false;

                            if(dateSelected){
                                if(today.compareTo(newLocalDate) < 0){
                                    newMeeting.put("date", newDate);
                                    newMeeting.put("day", dayOfWeek);
                                    if(!TextUtils.isEmpty(editAddress.getText())){
                                        if(!addressValidates(newAddress)){
                                            Toast.makeText(AppointmentActivity.this, R.string.appointment_invalid_address, Toast.LENGTH_SHORT).show();
                                        } else {
                                            newMeeting.put("address", newAddress);
                                            if(timeSelected){
                                                newMeeting.put("hour", newHour);
                                                newMeeting.put("minute", newMinute);
                                                done = true;
                                            } else {
                                                done = true;
                                            }
                                        }
                                    } else if(timeSelected){
                                            newMeeting.put("hour", newHour);
                                            newMeeting.put("minute", newMinute);
                                            done = true;
                                    } else {
                                        done = true;
                                    }
                                } else {
                                    Toast.makeText(AppointmentActivity.this, R.string.appointment_select_date_future, Toast.LENGTH_SHORT).show();
                                }
                            } else if(!TextUtils.isEmpty(editAddress.getText())){
                                    if(!addressValidates(newAddress)){
                                        Toast.makeText(AppointmentActivity.this, R.string.appointment_invalid_address, Toast.LENGTH_SHORT).show();
                                    } else {
                                        newMeeting.put("address", newAddress);
                                        if(timeSelected){
                                            newMeeting.put("hour", newHour);
                                            newMeeting.put("minute", newMinute);
                                            done = true;
                                        } else {
                                            done = true;
                                        }
                                    }
                            } else if(timeSelected){
                                newMeeting.put("hour", newHour);
                                newMeeting.put("minute", newMinute);
                                done = true;
                            }

                            if(done){
                                ref.updateChildren(newMeeting).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if(task.isSuccessful()){
                                            hostLabel.setVisibility(View.VISIBLE);
                                            nxtMeetingHost.setVisibility(View.VISIBLE);
                                            viewSwitcherDay.showPrevious();
                                            viewSwitcherTime.showPrevious();
                                            viewSwitcherAddress.showPrevious();
                                            clickedForEdit = false;
                                            editMeetingBtn.setText(R.string.meeting_edit_mtng_btn);
                                            confirmChanges.setVisibility(View.GONE);
                                            selectDateBtn.setText(R.string.appointment_select_date);
                                            editAddress.setText(null);
                                            selectTimeBtn.setText(R.string.appointment_select_time);
                                            cancelMeetingBtn.setAlpha(1f);
                                            cancelMeetingBtn.setClickable(true);
                                            confirmMeetingEndBtn.setAlpha(1f);
                                            confirmMeetingEndBtn.setClickable(true);
                                            done = false;
                                            dateSelected = false;
                                            timeSelected = false;
                                        } else {
                                            Toast.makeText(AppointmentActivity.this, "failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                int changesN = changes + 1;
                                ref.child("changes").setValue(changesN);
                            }
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
            });
        }
    }

    private boolean addressValidates(String txt_adr) {
        Pattern pattern = Pattern.compile("[\\w]+\\.?\\s[\\w]+,\\s[\\w]+\\s[\\w]+");
        Matcher matcher = pattern.matcher(txt_adr);
        return matcher.matches();
    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                String date = makeDateString(day, month, year);
                selectDateBtn.setText(date);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(this, dateSetListener, year, month, day);
    }

    private String makeDateString(int day, int month, int year) {
        String stringDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day);
        LocalDate localDate = LocalDate.parse(stringDate);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(getText(R.string.date_format).toString());
        String formattedDate = localDate.format(dtf);
        return formattedDate;
    }

    private void openDatePicker(View view) {
        datePickerDialog.show();

    }

    private void popTimePicker(View view) {

        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                hour = selectedHour;
                minute = selectedMinute;
                selectTimeBtn.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
            }
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hour, minute, true);

        timePickerDialog.show();
    }

    @Override
    public void onBackPressed() {
        if(clickedForEdit == true){
            if(!selectDateBtn.getText().toString().equals(getText(R.string.appointment_select_date)) || !TextUtils.isEmpty(editAddress.getText()) || !selectTimeBtn.getText().toString().equals(getText(R.string.appointment_select_time))) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AppointmentActivity.this);
                builder.setTitle(R.string.discard_title);
                builder.setMessage(R.string.discard_msg);
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.discard_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        hostLabel.setVisibility(View.VISIBLE);
                        nxtMeetingHost.setVisibility(View.VISIBLE);
                        viewSwitcherDay.showPrevious();
                        viewSwitcherTime.showPrevious();
                        viewSwitcherAddress.showPrevious();
                        clickedForEdit = false;
                        editMeetingBtn.setText(R.string.meeting_edit_mtng_btn);
                        confirmChanges.setVisibility(View.GONE);
                        selectDateBtn.setText(R.string.appointment_select_date);
                        editAddress.setText(null);
                        selectTimeBtn.setText(R.string.appointment_select_time);
                        cancelMeetingBtn.setAlpha(1f);
                        cancelMeetingBtn.setClickable(true);
                        confirmMeetingEndBtn.setAlpha(1f);
                        confirmMeetingEndBtn.setClickable(true);
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                hostLabel.setVisibility(View.VISIBLE);
                nxtMeetingHost.setVisibility(View.VISIBLE);
                viewSwitcherDay.showPrevious();
                viewSwitcherTime.showPrevious();
                viewSwitcherAddress.showPrevious();
                clickedForEdit = false;
                editMeetingBtn.setText(R.string.meeting_edit_mtng_btn);
                confirmChanges.setVisibility(View.GONE);
                selectDateBtn.setText(R.string.appointment_select_date);
                editAddress.setText(null);
                selectTimeBtn.setText(R.string.appointment_select_time);
                cancelMeetingBtn.setAlpha(1f);
                cancelMeetingBtn.setClickable(true);
                confirmMeetingEndBtn.setAlpha(1f);
                confirmMeetingEndBtn.setClickable(true);
            }
        } else {
            startActivity(new Intent(AppointmentActivity.this, MainActivity.class));
            finish();
        }
    }
}
