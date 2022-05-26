package com.example.boardgamerapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {
    private EditText fn;
    private EditText ln;
    private EditText adr;
    private EditText email;
    private EditText pass;
    private EditText passConf;

    private FirebaseDatabase db;
    private FirebaseAuth auth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        overridePendingTransition(com.google.android.material.R.anim.abc_popup_enter, com.google.android.material.R.anim.abc_popup_exit);


        fn = findViewById(R.id.regis_firstname);
        ln = findViewById(R.id.regis_lastname);
        adr = findViewById(R.id.regis_address);
        email = findViewById(R.id.regis_email);
        pass = findViewById(R.id.regis_password);
        passConf = findViewById(R.id.regis_password_conf);

    }

    @Override
    public void onBackPressed() {
        if (!fn.getText().toString().isEmpty() || !ln.getText().toString().isEmpty() || !adr.getText().toString().isEmpty() || !email.getText().toString().isEmpty() || !pass.getText().toString().isEmpty() || !passConf.getText().toString().isEmpty()){
            AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
            builder.setCancelable(true);
            builder.setTitle(R.string.discard_title);
            builder.setMessage(R.string.discard_msg);
            builder.setPositiveButton(R.string.discard_yes,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
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
            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
        }
    }
}
