package com.example.volunteertracker;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseAuth.AuthStateListener authStateListener;

    private Button signoutBtn;
    public static FirebaseUser user;
    private TextView titleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button mapBtn = findViewById(R.id.mapBtn);
        titleTextView = findViewById(R.id.userName);

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startIntent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(startIntent);
            }
        });

        Button timeSheetBtn = findViewById(R.id.timeSheetBtn);
        timeSheetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startIntent = new Intent(getApplicationContext(), TimeSheetActivity.class);
                startActivity(startIntent);
            }
        });

        Button leaderBoardbtn = findViewById(R.id.leaderBoardBtn);
        leaderBoardbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startIntent = new Intent(getApplicationContext(), LeaderboardActivity.class);
                startActivity(startIntent);
            }
        });

        Button signoutBtn = findViewById(R.id.signoutBtn);
        signoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
                Intent startIntent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(startIntent);
            }
        });

        getUserData();


    }

    static Intent makeNotificationIntent(Context geofenceService, String msg) {
        Log.d(TAG, msg);
        return new Intent(geofenceService, MainActivity.class);
    }


    @Override
    protected void onStop() {
        super.onStop();
        signOut();

    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    private void getUserData() {
        FirebaseFirestore.getInstance().collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                HashMap<String, Object> map = (HashMap<String, Object>) document.getData();
                                //Hours=3, userID=Y0OBftcNM5PlzOLwNINwIboTrGj1, Name=Jeff
                                if (map.containsKey("Name") && map.get("userID").equals(user.getUid())) {
                                    titleTextView.setText("Hello " + (String) map.get("Name") + "!");
                                }

                            }

                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }
}
