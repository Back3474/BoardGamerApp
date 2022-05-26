package com.example.boardgamerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    private ImageButton menu;
    private Button late;
    private Button rating;
    private Button games;
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private MenuItem acc;
    private MenuItem logout;
    private MenuItem mngmnt;
    private LinearLayout meeting_layout;
    private TextView day;
    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        overridePendingTransition(com.google.android.material.R.anim.abc_popup_enter, com.google.android.material.R.anim.abc_popup_exit);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        menu = findViewById(R.id.main_menuBtn);
        late = findViewById(R.id.main_lateBtn);
        rating = findViewById(R.id.main_ratingBtn);
        acc = findViewById(R.id.menu_acc);
        logout = findViewById(R.id.menu_logout);
        mngmnt = findViewById(R.id.menu_mngmnt);
        meeting_layout = findViewById(R.id.main_meetingLayout);
        games = findViewById(R.id.main_gamesBtn);
        day = findViewById(R.id.main_mtng_day);


        meeting_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AppointmentActivity.class));
            }
        });

        games.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, GamesActivity.class));
            }
        });

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
            }
        });

        late.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LateActivity.class));
            }
        });

        rating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RatingActivity.class));
            }
        });

        /*DatabaseReference ref = db.getReference().child("test");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                day.setText(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/

        thread = new Thread() {

            @Override
            public void run() {
                try {
                    while (!thread.isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DatabaseReference ref = db.getReference().child("test");
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        day.setText(snapshot.getValue().toString());
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        thread.start();

    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.actions);
        popup.show();
    }


    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.menu_acc:
                startActivity(new Intent(MainActivity.this, UserAccountActivity.class));
                return true;
            case R.id.menu_logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(true);
                builder.setTitle(R.string.logout_msg);
                builder.setPositiveButton(R.string.discard_yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                auth.signOut();
                                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            case R.id.menu_mngmnt:
                startActivity(new Intent(MainActivity.this, ManagementActivity.class));
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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