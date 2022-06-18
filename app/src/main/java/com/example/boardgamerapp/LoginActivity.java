package com.example.boardgamerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private EditText email;
    private EditText password;
    private ImageButton login_btn;
    private ImageButton regis_btn;
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private TextView resetPass;
    String statusLogin;
    String statusAutoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        overridePendingTransition(com.google.android.material.R.anim.abc_popup_enter, com.google.android.material.R.anim.abc_popup_exit);

        email = findViewById(R.id.email_login);
        password = findViewById(R.id.password_login);
        login_btn = findViewById(R.id.login_loginBtn);
        regis_btn = findViewById(R.id.login_regisBtn);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        resetPass = findViewById(R.id.resetPass);


        resetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setCancelable(true);
                builder.setTitle(R.string.reset_pass_msg);
                builder.setPositiveButton(R.string.discard_yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(LoginActivity.this, ResetPassword.class));
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
            }
        });

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();
                loginUser(txt_email, txt_password);
            }
        });


        regis_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
                finish();
            }
        });

        password.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_ENTER){

                    loginUser(email.getText().toString(), password.getText().toString());
                    return true;
                }
                return false;
            }
        });
    }

    private void loginUser(String email, String password) {
        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(LoginActivity.this, R.string.enter_login_data, Toast.LENGTH_SHORT).show();
        } else {
        findViewById(R.id.loginLayout).setVisibility(View.GONE);
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    DatabaseReference ref = db.getReference("users/" + auth.getUid().toString());
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            statusLogin = snapshot.child("status").getValue().toString();
                            if (statusLogin.equals("deactivated")) {
                                auth.signOut();
                                findViewById(R.id.loginLayout).setVisibility(View.VISIBLE);
                                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                                Toast.makeText(LoginActivity.this, R.string.acc_deactivated, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                                findViewById(R.id.loginLayout).setVisibility(View.VISIBLE);
                                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else {
                    findViewById(R.id.loginLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, R.string.login_wrong_email_or_password, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

    @Override
    protected void onStart() {
        super.onStart();

        if (auth.getCurrentUser() != null) {
            findViewById(R.id.loginLayout).setVisibility(View.GONE);
            DatabaseReference ref = db.getReference("users/" + auth.getUid().toString());
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    statusAutoLogin = snapshot.child("status").getValue().toString();
                    if (statusAutoLogin.equals("deactivated")) {
                        auth.signOut();
                        Toast.makeText(LoginActivity.this, R.string.acc_deactivated, Toast.LENGTH_SHORT).show();
                    } else {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));

                        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setCancelable(true);
        builder.setTitle(R.string.leave_app);
        builder.setPositiveButton(R.string.discard_yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent a = new Intent(Intent.ACTION_MAIN);
                        a.addCategory(Intent.CATEGORY_HOME);
                        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(a);
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

    }

}