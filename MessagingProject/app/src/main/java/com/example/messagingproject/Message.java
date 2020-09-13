package com.example.messagingproject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Message {
    private String date;
    private String time;
    private String text;
    private String senderUsername;
    private String senderUserID;

    public Message(Date date, Date time, String text, String senderUsername,String senderUserID) {
        DateFormat dateFormat=SimpleDateFormat.getDateTimeInstance();
        this.date=dateFormat.format(date);
        DateFormat timeFormat=SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
        this.time = timeFormat.format(time);
        this.text = text;
        this.senderUsername = senderUsername;
        this.senderUserID = senderUserID;

    }
    public Message(String text, String senderUsername,String senderUserID) {
        this(Calendar.getInstance().getTime(),Calendar.getInstance().getTime(),text,senderUsername,senderUserID);
    }

    //normal constructor for dataBase
    public Message(String date, String time, String text, String senderUsername) {
        this.date = date;
        this.time = time;
        this.text = text;
        this.senderUsername = senderUsername;
    }
    public Message(){

    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getText() {
        return text;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public String getSenderUserID() {
        return senderUserID;
    }
}
