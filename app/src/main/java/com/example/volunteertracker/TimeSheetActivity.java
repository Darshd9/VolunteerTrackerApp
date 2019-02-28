package com.example.volunteertracker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class TimeSheetActivity extends AppCompatActivity {

    private static final String TAG = "TImeSheet";
    private FirebaseFirestore db;
    private ListView leaderBoard;
    private TextView mUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_sheet);

        leaderBoard = (ListView) findViewById(R.id.timeSheetListView);
        mUserName = findViewById(R.id.nameTextView);

        db = FirebaseFirestore.getInstance();
        getTimeSheets();
    }

    private void getTimeSheets() {

        db.collection("users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.d("", "Error : " + e.getMessage());
                }
                final ArrayList<TimeSheet> timeSheets = new ArrayList<>();
                for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        String userid = (String) doc.getDocument().get("userID");
                        if (userid.equalsIgnoreCase(MainActivity.user.getUid())) {
                            Log.d("Brand Name: ", doc.getDocument().getId());
                            mUserName.setText((String) doc.getDocument().getData().get("Name"));
                            doc.getDocument().getReference().collection("Meetings").addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                    if (e != null) {
                                        Log.d("", "Error : " + e.getMessage());
                                    }

                                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                                        if (doc.getType() == DocumentChange.Type.ADDED) {
                                            Log.d("SubBrands Name: ", doc.getDocument().getId());
                                            HashMap<String, Object> map = (HashMap<String, Object>) doc.getDocument().getData();

                                            if (map.containsKey("date") && map.containsKey("volunteerHours")) {
                                                long hours = (long) map.get("volunteerHours");
                                                TimeSheet timeSheet = new TimeSheet((int) hours, (Timestamp) map.get("date"));
                                                timeSheets.add(timeSheet);

                                            }
                                        }
                                    }

                                    timeSheets.trimToSize();
                                    Collections.sort(timeSheets, new Comparator<TimeSheet>() {

                                        @Override

                                        public int compare(TimeSheet v1, TimeSheet v2) {

                                            return v1.getDate().toDate().compareTo(v2.getDate().toDate());

                                        }

                                    });

                                    Collections.reverse(timeSheets);

                                    //Collections.sort(volunteerList, Collections.<Volunteer>reverseOrder());


                                    final ListAdapter adapter = new ArrayAdapter<TimeSheet>(TimeSheetActivity.this, android.R.layout.simple_list_item_1, timeSheets);

                                    leaderBoard.setAdapter(adapter);

                                }
                            });
                        }
                    }

                }
            }
        });


    }
}
