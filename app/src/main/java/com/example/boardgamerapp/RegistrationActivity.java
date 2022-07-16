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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {
    private EditText fn;
    private EditText ln;
    private EditText adr;
    private EditText email;
    private EditText pass;
    private EditText passConf;
    private ImageButton save;
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
        save = findViewById(R.id.regis_save_btn);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance("https://board-gamer-app-ff958-default-rtdb.firebaseio.com");

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt_fn = fn.getText().toString();
                String txt_ln = ln.getText().toString();
                String txt_adr = adr.getText().toString();
                String txt_email = email.getText().toString();
                String txt_password = pass.getText().toString();
                String txt_passwordConf = passConf.getText().toString();



                if (TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password) || TextUtils.isEmpty(txt_fn) || TextUtils.isEmpty(txt_ln) || TextUtils.isEmpty(txt_adr)){
                    Toast.makeText(RegistrationActivity.this, R.string.check_entries, Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(txt_passwordConf)) {
                    Toast.makeText(RegistrationActivity.this, R.string.confirm_your_password, Toast.LENGTH_SHORT).show();
                } else if (!addressValidates(txt_adr)) {
                    Toast.makeText(RegistrationActivity.this, R.string.regis_invalid_address, Toast.LENGTH_SHORT).show();
                } else if (!txt_password.equals(txt_passwordConf)) {
                    Toast.makeText(RegistrationActivity.this, R.string.confirm_your_password_failed, Toast.LENGTH_SHORT).show();
                } else if (txt_password.length() < 8) {
                    Toast.makeText(RegistrationActivity.this, R.string.password_too_short, Toast.LENGTH_SHORT).show();
                } else if (txt_password.length() > 20) {
                    Toast.makeText(RegistrationActivity.this, R.string.password_too_long, Toast.LENGTH_SHORT).show();
                } else if (!passwordValidates(txt_password)) {
                    Toast.makeText(RegistrationActivity.this, R.string.regis_invalid_password, Toast.LENGTH_LONG).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
                    builder.setCancelable(true);
                    builder.setTitle(R.string.regis_registration_title);
                    builder.setMessage(getText(R.string.regis_check_regis_data) + "\n" + "\n" + txt_fn + txt_ln + "\n" + txt_email + "\n" + txt_adr + "\n" + "\n" + getText(R.string.regis_check_regis_data_continue));
                    builder.setPositiveButton(R.string.discard_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            registerUser(txt_email, txt_password, txt_fn, txt_ln, txt_adr);
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
        });


    }

    private boolean addressValidates(String txt_adr) {
        Pattern pattern = Pattern.compile("[\\w]+\\.?\\s[\\w]+,\\s[\\w]+\\s[\\w]+");
        Matcher matcher = pattern.matcher(txt_adr);
        return matcher.matches();
    }

    private void registerUser(String email, String password, String fn, String ln, String adr) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    auth.signInWithEmailAndPassword(email, password);
                    DatabaseReference ref = db.getReference("users/"+auth.getUid());
                    Map<String, Object> user = new HashMap<>();
                    user.put("firstname", fn);
                    user.put("lastname", ln);
                    user.put("address", adr);
                    user.put("email", email);
                    user.put("status", "active");
                    user.put("id", auth.getUid().toString());
                    user.put("isAdmin", false);
                    user.put("isHost", false);

                    ref.setValue(user);

                    Toast.makeText(RegistrationActivity.this, R.string.regis_successfull, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(RegistrationActivity.this, R.string.regis_failed, Toast.LENGTH_SHORT).show();
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
            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
            finish();
        }
    }
}
