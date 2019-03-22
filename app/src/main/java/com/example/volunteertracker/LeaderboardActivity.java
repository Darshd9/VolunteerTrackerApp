package com.example.volunteertracker;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class LeaderboardActivity extends AppCompatActivity {

    private static final String TAG = "LeaderBoard";
    private ListView leaderBoard;
    private FirebaseAuth auth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        leaderBoard = (ListView)findViewById(R.id.leaderBoardListView);


db = FirebaseFirestore.getInstance();



        populateListView();
    }

    private void populateListView(){

       final  ArrayList<Volunteer> volunteerList = new ArrayList<>();
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                HashMap<String,Object> map = (HashMap<String, Object>) document.getData();
                                //Hours=3, userID=Y0OBftcNM5PlzOLwNINwIboTrGj1, Name=Jeff
                                if(map.containsKey("Name") && map.containsKey("Hours")){
                                    long hours = (long) map.get("Hours");
                                    Volunteer volunteer = new Volunteer((String) map.get("Name"), (int) hours);
                                    volunteerList.add(volunteer);
                                }

                            }

                            volunteerList.trimToSize();
                            Collections.sort(volunteerList, new Comparator<Volunteer>() {

                                @Override

                                public int compare(Volunteer v1, Volunteer v2) {

                                    return Integer.valueOf(v1.getVolunteerHours()).compareTo(v2.getVolunteerHours());

                                }

                            });

                            Collections.reverse(volunteerList);




                            final ListAdapter adapter = new ArrayAdapter<Volunteer>(LeaderboardActivity.this, android.R.layout.simple_list_item_1, volunteerList);

                            leaderBoard.setAdapter(adapter);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

    }
}
