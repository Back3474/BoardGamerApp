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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText currentPass;
    private EditText newPass;
    private EditText confNewPass;
    private ImageButton save;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);


        overridePendingTransition(com.google.android.material.R.anim.abc_popup_enter, com.google.android.material.R.anim.abc_popup_exit);


        currentPass = findViewById(R.id.currentPass);
        newPass = findViewById(R.id.newPass);
        confNewPass = findViewById(R.id.confirmNewPass);
        save = findViewById(R.id.saveChangedPass);
        auth = FirebaseAuth.getInstance();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(currentPass.getText())){
                    Toast.makeText(ChangePasswordActivity.this, R.string.enter_curr_pass, Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(newPass.getText())) {
                    Toast.makeText(ChangePasswordActivity.this, R.string.enter_new_pass, Toast.LENGTH_SHORT).show();
                } else if (newPass.getText().length() < 8) {
                    Toast.makeText(ChangePasswordActivity.this, R.string.password_too_short, Toast.LENGTH_SHORT).show();
                } else if (newPass.getText().length() > 20) {
                    Toast.makeText(ChangePasswordActivity.this, R.string.password_too_long, Toast.LENGTH_SHORT).show();
                } else if (!passwordValidates(newPass.getText().toString())) {
                    Toast.makeText(ChangePasswordActivity.this, R.string.regis_invalid_password, Toast.LENGTH_LONG).show();
                } else if (newPass.getText().toString().equals(confNewPass.getText().toString())) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    AuthCredential credential = EmailAuthProvider
                            .getCredential(user.getEmail().toString(), currentPass.getText().toString());

                    user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        user.updatePassword(newPass.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(ChangePasswordActivity.this, R.string.pass_change_success, Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(ChangePasswordActivity.this, UserAccountActivity.class));
                                                    finish();
                                                } else {
                                                    Toast.makeText(ChangePasswordActivity.this, R.string.pass_change_err, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(ChangePasswordActivity.this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(ChangePasswordActivity.this, R.string.pass_conf_failed, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public boolean passwordValidates(String pass) {
        int count = 0;

        if(pass.matches(".*\\d.*"))
            count ++;
        if(pass.matches(".*[a-z].*"))
            count ++;
        if(pass.matches(".*[A-Z].*"))
            count ++;

        return count == 3;
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
                            finish();
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
            finish();
        }
    }
}