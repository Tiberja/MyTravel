package com.example.mytravel.calendar;

public class CalendarNote {
    public String date;   // "yyyy-MM-dd"
    public String note;

    public CalendarNote() {} // Firestore braucht das

    public CalendarNote(String date, String note) {
        this.date = date;
        this.note = note;
    }
}