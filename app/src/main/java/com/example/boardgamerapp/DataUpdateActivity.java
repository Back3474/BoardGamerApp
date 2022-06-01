package com.example.boardgamerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class DataUpdateActivity extends AppCompatActivity {
    private EditText firstname;
    private EditText lastname;
    private EditText email;
    private EditText address;
    private ImageButton saveData;
    private FirebaseDatabase db;
    private FirebaseAuth auth;
    private HashMap<String, Object> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_update);

        overridePendingTransition(com.google.android.material.R.anim.abc_popup_enter, com.google.android.material.R.anim.abc_popup_exit);

        firstname = findViewById(R.id.updateFirstname);
        lastname = findViewById(R.id.updateLastname);
        email = findViewById(R.id.updateEmail);
        address = findViewById(R.id.updateAddress);
        saveData = findViewById(R.id.saveDataBtn);
        db = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        saveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt_fn = firstname.getText().toString();
                String txt_ln = lastname.getText().toString();
                String txt_email = email.getText().toString();
                String txt_adr = address.getText().toString();

                map = new HashMap<String, Object>();
                if (!TextUtils.isEmpty(txt_fn)) {map.put("firstname", txt_fn);}
                if (!TextUtils.isEmpty(txt_ln)) {map.put("lastname", txt_ln);}
                if (!TextUtils.isEmpty(txt_email)) {map.put("email", txt_email);}
                if (!TextUtils.isEmpty(txt_adr)) {map.put("address", txt_adr);}

                updateData (map);
                map.clear();
                startActivity(new Intent(DataUpdateActivity.this, UserAccountActivity.class));
                finish();

            }
        });

    }

    private void updateData(HashMap map) {
        DatabaseReference ref = db.getReference("users/"+auth.getUid().toString());
        ref.updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Toast.makeText(DataUpdateActivity.this, R.string.data_update_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DataUpdateActivity.this, R.string.data_update_fail, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!lastname.getText().toString().isEmpty() || !firstname.getText().toString().isEmpty() || (!email.getText().toString().isEmpty() || (!address.getText().toString().isEmpty()))){
            AlertDialog.Builder builder = new AlertDialog.Builder(DataUpdateActivity.this);
            builder.setCancelable(true);
            builder.setTitle(R.string.discard_title);
            builder.setMessage(R.string.discard_msg);
            builder.setPositiveButton(R.string.discard_yes,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(DataUpdateActivity.this, UserAccountActivity.class));
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
            startActivity(new Intent(DataUpdateActivity.this, UserAccountActivity.class));
            finish();
        }
    }
}