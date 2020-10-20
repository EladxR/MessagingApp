package com.example.messagingproject;


public class Chat {
    public String name="";
    public String id;
    public String image;
    public boolean isGroup;
    public boolean isOtherDelete=false; //only relevant to private chat

    public Chat() { }
    public Chat(String name,String id,boolean isGroup){
        this(name,id,isGroup,null,false);
    }
    public Chat(String name,String id,boolean isGroup,String image){
        this(name,id,isGroup,image,false);
    }

    public Chat(String name,String id,boolean isGroup,String image,boolean isOtherDelete) {
        this.name = name;
        this.id = id;
        this.isGroup = isGroup;
        this.image = image;
        this.isOtherDelete=isOtherDelete;
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

    public boolean isGroup() {
        return isGroup;
    }

    public boolean isOtherDelete() {
        return isOtherDelete;
    }
}
