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
    private String type; // text message or image
    private String image;

    public Message(Date date, Date time, String text, String senderUsername,String senderUserID) {
        DateFormat dateFormat=SimpleDateFormat.getDateTimeInstance();
        this.date=dateFormat.format(date);
        DateFormat timeFormat=SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
        this.time = timeFormat.format(time);
        this.text = text;
        this.senderUsername = senderUsername;
        this.senderUserID = senderUserID;
        this.type="Text";

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
        this.type="Text"; // default type
    }

    public Message(String date, String time, String text, String senderUsername, String senderUserID, String type, String image) {
        this.date = date;
        this.time = time;
        this.text = text;
        this.senderUsername = senderUsername;
        this.senderUserID = senderUserID;
        this.type = type;
        this.image = image;
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

    public String getType() {
        return type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setText(String text) {
        this.text = text;
    }
}
