package com.example.volunteertracker;

public class VolunteerEvent {

    private String title;
    private String date;
    private int hours;

    public VolunteerEvent(String title, String date, int hours){
        this.title = title;
        this.date = date;
        this.hours = hours;
    }

    public int getHours() {
        return hours;
    }

    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
