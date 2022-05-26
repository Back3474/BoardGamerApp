package com.example.boardgamerapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class UserAccountActivity extends AppCompatActivity {
    private Button changePass;
    private Button changeAccData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        overridePendingTransition(com.google.android.material.R.anim.abc_popup_enter, com.google.android.material.R.anim.abc_popup_exit);

        changePass = findViewById(R.id.acc_change_pass);
        changeAccData = findViewById(R.id.acc_change_data);

        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserAccountActivity.this, ChangePasswordActivity.class));
            }
        });

        changeAccData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserAccountActivity.this, DataUpdateActivity.class));
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(UserAccountActivity.this, MainActivity.class));
    }

}