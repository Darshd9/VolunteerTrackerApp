package com.example.volunteertracker;


import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.health.TimerStat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class GeofenceTrasitionService extends IntentService {

    private static final String TAG = GeofenceTrasitionService.class.getSimpleName();
    private static final int GEOFENCE_NOTIFICATION_ID = 0;
    private static final int TIME_FROM = 600;
    private static final int TIME_TO = 700;
    private FirebaseFirestore db;

    public GeofenceTrasitionService() {
        super("GeofenceTrasitionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
// Retrieve the Geofencing intent
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        String msgNotification;

        // Handling errors
        if (geofencingEvent.hasError()) {
            String errorMsg = getErrorString(geofencingEvent.getErrorCode());
            Log.e(TAG, errorMsg);
            return;
        }

        // Retrieve GeofenceTrasition
        int geoFenceTransition = geofencingEvent.getGeofenceTransition();
        // Check if the transition type
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            // Get the geofence that were triggered
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            // Create a detail message with Geofences received
            String geofenceTransitionDetails = getGeofenceTrasitionDetails(geoFenceTransition, triggeringGeofences);
            // Send notification details as a String

            //Add function to check that current time is allowed for volunteering or not
            if(!checkDate())
            {
                msgNotification = "Not Adding entry since date did not match " + geofenceTransitionDetails;
                Log.e(TAG, msgNotification);
            }
            else
            {
                getUserActivity();
            }


            // if yes then add to databse for 1 hours
            // before adding if there is existing entry for the date, then do not add it.

            Log.e(TAG, "show notification here " + geofenceTransitionDetails);
            sendNotification(geofenceTransitionDetails);
        }
    }

    private Boolean checkDate() {
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            return false;
        }
        if (calendar.get(Calendar.HOUR_OF_DAY) < 17 || calendar.get(Calendar.HOUR_OF_DAY) > 19) {
            return false;
        }
        return true;
    }

    // Create a detail message with Geofences received
    private String getGeofenceTrasitionDetails(int geoFenceTransition, List<Geofence> triggeringGeofences) {
        // get the ID of each geofence triggered
        ArrayList<String> triggeringGeofencesList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesList.add(geofence.getRequestId());
        }
        String status = null;
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
            status = "Checking in Volunteer ";
        else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
            status = "Exiting ";
        return status + TextUtils.join(", ", triggeringGeofencesList);
    }

    // Send a notification
    private void sendNotification(String msg) {
        Log.i(TAG, "sendNotification: " + msg);

        // Intent to start the main Activity
        Intent notificationIntent = MainActivity.makeNotificationIntent(
                getApplicationContext(), msg
        );

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MapsActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Creating and sending Notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(
                GEOFENCE_NOTIFICATION_ID,
                createNotification(msg, resultPendingIntent));
    }

    // Create a notification
    private Notification createNotification(String msg, PendingIntent notificationPendingIntent) {
        String channelId = "default_channel";
        String title = "Geofence Notification!";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "my_channel";
            String Description = "channel_desc";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(channelId, name, importance);
            mChannel.setDescription(Description);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true)

                .setContentText(msg);
        return builder.build();
    }

    // Handle errors
    private static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GeoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error.";
        }
    }

    private void getUserActivity() {
        db = FirebaseFirestore.getInstance();
        db.collection("users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.d("", "Error : " + e.getMessage());
                }
                final ArrayList<TimeSheet> timeSheets = new ArrayList<>();
                for (final DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        final String userid = (String) doc.getDocument().get("userID");
                        if(userid.equalsIgnoreCase(MainActivity.user.getUid())){
                            Log.d("Brand Name: ", doc.getDocument().getId());
                            //mUserName.setText((String)doc.getDocument().getData().get("Name"));
                            doc.getDocument().getReference().collection("Meetings").addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                    if (e != null) {
                                        Log.d("", "Error : " + e.getMessage());
                                    }

                                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                                        if (doc.getType() == DocumentChange.Type.ADDED) {
                                            Log.d("SubBrands Name: ", doc.getDocument().getId());
                                            HashMap<String,Object> map = (HashMap<String, Object>) doc.getDocument().getData();

                                            if(map.containsKey("date") && map.containsKey("volunteerHours")){
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
                                    Calendar c1 = Calendar.getInstance();
                                    Date dt1 = c1.getTime();
                                    TimeSheet latestTimeSheet = timeSheets.get(0);
                                    Date dt2 = latestTimeSheet.getDate().toDate();
                                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                                    String dt1String = sdf.format(dt1);
                                    String dt2String = sdf.format(dt2);
                                    if (dt1String.compareTo(dt2String)== 0){
                                        Log.d("debug", "No need to add data");
                                        return;
                                    }
                                    else
                                    {
                                        Timestamp ts1 = new Timestamp(dt1);
                                        int hour = 1 ;
                                        Long totalHours = (Long) doc.getDocument().getData().get("Hours");
                                        Long newHours = totalHours + hour;
                                        TimeSheet newTimeSheet = new TimeSheet(hour, ts1);
                                        doc.getDocument().getReference().collection("Meetings").document().set(newTimeSheet);
                                        DocumentReference userRef = db.collection("users").document(doc.getDocument().getId());
                                        userRef.update("Hours", newHours)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "DocumentSnapshot successfully updated!");
                                                }
                                            });
                                        return;
                                    }
                                }
                            });
                        }
                    }
                }
            }});
    }

}

