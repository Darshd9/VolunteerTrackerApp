package com.example.volunteertracker;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@IgnoreExtraProperties
public class TimeSheet {

    private int volunteerHours;
    private Timestamp date;

    public TimeSheet(int volunteerHours, Timestamp date) {
        this.volunteerHours = volunteerHours;
        this.date = date;
    }

    public int getVolunteerHours() {
        return volunteerHours;
    }

    public void setVolunteerHours(int volunteerHours) {
        this.volunteerHours = volunteerHours;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    @Override
    public String toString() {
        DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US);
        String dateStr = dateFormat.format(date.toDate());
        return volunteerHours + " hours:     " + dateStr;
    }
}
