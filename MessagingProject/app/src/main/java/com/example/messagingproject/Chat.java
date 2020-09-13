package com.example.messagingproject;

import java.util.ArrayList;
import java.util.HashMap;

public class Chat {
    public String name="";
  //  public HashMap<String,Message> chatHistory; // <sender ID, message data>
    public String id;
    public String image;
    public boolean isGroup;

    public Chat() {
      //  chatHistory=new ArrayList<>();
    }
    public Chat(String name,String id,boolean isGroup){
        this.name=name;
        this.id=id;
        this.isGroup=isGroup;
      //  this.chatHistory=chatHistory;
    }

    public void setChatHistory(HashMap<String, Message> chatHistory) {
      //  this.chatHistory = chatHistory;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    //public HashMap<String, Message> getChatHistory() {
      //  return chatHistory;
   // }
}
