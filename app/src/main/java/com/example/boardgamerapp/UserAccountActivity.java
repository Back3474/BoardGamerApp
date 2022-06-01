package com.example.boardgamerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserAccountActivity extends AppCompatActivity {
    private Button changePass;
    private Button changeAccData;
    private TextView accName;
    private TextView accEmail;
    private TextView accAddress;
    private FirebaseDatabase db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        overridePendingTransition(com.google.android.material.R.anim.abc_popup_enter, com.google.android.material.R.anim.abc_popup_exit);

        changePass = findViewById(R.id.acc_change_pass);
        changeAccData = findViewById(R.id.acc_change_data);
        accName = findViewById(R.id.acc_name);
        accEmail = findViewById(R.id.acc_email);
        accAddress = findViewById(R.id.acc_address);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        DatabaseReference ref = db.getReference("users/"+auth.getUid().toString());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                accName.setText(snapshot.child("firstname").getValue().toString() + " " + snapshot.child("lastname").getValue().toString());
                accEmail.setText(snapshot.child("email").getValue().toString());
                accAddress.setText(snapshot.child("address").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserAccountActivity.this, ChangePasswordActivity.class));
                finish();
            }
        });

        changeAccData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserAccountActivity.this, DataUpdateActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(UserAccountActivity.this, MainActivity.class));
        finish();
    }

}