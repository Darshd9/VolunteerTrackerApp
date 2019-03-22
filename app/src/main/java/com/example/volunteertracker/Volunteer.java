package com.example.volunteertracker;

import android.location.Location;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Volunteer {
    private String name;
    private int volunteerHours;


    public Volunteer(String name, int volunteerHours){
        this.name = name;
        this.volunteerHours = volunteerHours;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getVolunteerHours(){
        return volunteerHours;
    }

    public void setVolunteerHours(int volunteerHours) {
        this.volunteerHours = volunteerHours;
    }

    public void addVolunteerHour(){
        this.volunteerHours++;
    }


    @Override
    public String toString() {
        return volunteerHours + " hours:     " + name;
    }
}
