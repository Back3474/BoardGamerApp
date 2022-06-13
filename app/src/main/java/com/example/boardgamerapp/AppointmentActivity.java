package com.example.boardgamerapp;

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
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ViewSwitcher;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;

public class AppointmentActivity extends AppCompatActivity {
    private FirebaseDatabase db;
    private FirebaseAuth auth;
    private LocalDate today;
    private DateTimeFormatter dtf;
    private Button editMeetingBtn, cancelMeetingBtn, confirmMeetingEndBtn, selectTimeBtn, selectDateBtn;
    private TextView nxtMeetingDay, nxtMeetingTime, nxtMeetingHost, nxtMeetingAddress, meetingLabel, meetingIsCanceled, hostLabel;
    private Boolean meetingCanceled, clickedForEdit;
    private ViewSwitcher viewSwitcherDay, viewSwitcherTime, viewSwitcherAddress;
    private ImageButton confirmChanges;
    private EditText editAddress;
    private int hour, minute, nextMtngHour, nextMtngMinute;
    private String date;
    private LocalDate inputDate;
    private DatePickerDialog datePickerDialog;

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
        meetingCanceled = true;
        clickedForEdit = false;
        dtf = DateTimeFormatter.ofPattern((String) getText(R.string.date_format));
        today = LocalDate.now();
        viewSwitcherDay = findViewById(R.id.dayViewSwitcher);
        viewSwitcherTime = findViewById(R.id.timeViewSwitcher);
        viewSwitcherAddress = findViewById(R.id.addressViewSwitcher);
        confirmChanges = findViewById(R.id.confirmMeetingChangesBtn);
        selectDateBtn = viewSwitcherDay.findViewById(R.id.meeting_nxt_mtng_date_btn);
        editAddress = viewSwitcherAddress.findViewById(R.id.meeting_nxt_mtng_address_edit);
        selectTimeBtn = viewSwitcherTime.findViewById(R.id.meeting_nxt_mtng_time_btn);
        initDatePicker();

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance("https://board-gamer-app-ff958-default-rtdb.firebaseio.com");

        DatabaseReference refNextMeeting = db.getReference("next meeting");

        refNextMeeting.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                date = snapshot.child("date").getValue().toString();
                inputDate = LocalDate.parse(date);
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern(getText(R.string.date_format).toString());
                String date1 = inputDate.format(dtf);
                nxtMeetingDay.setText(date1);
                hour = snapshot.child("hour").getValue(Integer.class);
                minute = snapshot.child("minute").getValue(Integer.class);
                nxtMeetingTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
                nxtMeetingHost.setText(snapshot.child("host").getValue().toString());
                nxtMeetingAddress.setText(snapshot.child("address").getValue().toString());

                meetingCanceled = snapshot.child("isCanceled").getValue(Boolean.class);
                if(meetingCanceled){
                    meetingIsCanceled.setText(R.string.appointment_meeting_canceled_label);
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
                                                        // WORK WORK WORK
                                                        hostLabel.setVisibility(View.VISIBLE);
                                                        nxtMeetingHost.setVisibility(View.VISIBLE);
                                                        viewSwitcherDay.showPrevious();
                                                        viewSwitcherTime.showPrevious();
                                                        viewSwitcherAddress.showPrevious();
                                                        clickedForEdit = false;
                                                        editMeetingBtn.setText(R.string.meeting_edit_mtng_btn);
                                                        confirmChanges.setVisibility(View.GONE);
                                                        selectDateBtn.setText(null);
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
                                            }
                                        });
                                    }
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
                                                }
                                            });
                                        }
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
                                            }
                                        });
                                    }

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
                        builder.setTitle("Warning");
                        builder.setMessage("This will discard all your changes! Do you want to continue?");
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

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
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
        startActivity(new Intent(AppointmentActivity.this, MainActivity.class));
        finish();
    }
}
