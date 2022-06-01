package com.example.boardgamerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPassword extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText email_txt;
    private ImageButton confirm_reset;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        overridePendingTransition(com.google.android.material.R.anim.abc_popup_enter, com.google.android.material.R.anim.abc_popup_exit);

        auth = FirebaseAuth.getInstance();
        email_txt = findViewById(R.id.your_acc_email_editText);
        confirm_reset = findViewById(R.id.confirm_reset_pass_email);

        confirm_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(email_txt.getText().toString())){
                    Toast.makeText(ResetPassword.this, R.string.reset_enter_acc_email, Toast.LENGTH_SHORT).show();
                } else {
                    auth.sendPasswordResetEmail(email_txt.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ResetPassword.this, R.string.reset_email_sent_to + email_txt.getText().toString(), Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ResetPassword.this, LoginActivity.class));
                                finish();
                            } else {
                                Toast.makeText(ResetPassword.this, R.string.reset_pass_error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });


    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ResetPassword.this, LoginActivity.class));
        finish();
    }
}