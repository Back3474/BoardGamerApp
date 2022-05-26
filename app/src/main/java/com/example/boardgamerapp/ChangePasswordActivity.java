package com.example.boardgamerapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText currentPass;
    private EditText newPass;
    private EditText confNewPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);


        overridePendingTransition(com.google.android.material.R.anim.abc_popup_enter, com.google.android.material.R.anim.abc_popup_exit);


        currentPass = findViewById(R.id.currentPass);
        newPass = findViewById(R.id.newPass);
        confNewPass = findViewById(R.id.confirmNewPass);

    }

    @Override
    public void onBackPressed() {
        if (!currentPass.getText().toString().isEmpty() || (!newPass.getText().toString().isEmpty() || (!confNewPass.getText().toString().isEmpty()))){
            AlertDialog.Builder builder = new AlertDialog.Builder(ChangePasswordActivity.this);
            builder.setCancelable(true);
            builder.setTitle(R.string.discard_title);
            builder.setMessage(R.string.discard_msg);
            builder.setPositiveButton(R.string.discard_yes,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(ChangePasswordActivity.this, UserAccountActivity.class));
                        }
                    });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        } else {
            startActivity(new Intent(ChangePasswordActivity.this, UserAccountActivity.class));
        }
    }
}