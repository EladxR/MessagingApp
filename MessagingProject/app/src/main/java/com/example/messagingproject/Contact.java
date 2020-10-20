package com.example.messagingproject;

import java.util.Objects;

// simple contact class
public class Contact  implements Comparable<Contact>{

    private String phoneNumber="";
    public String username;
    public String profileImage;
    private boolean isChecked=false; // when need to check select contact

    public void setPhoneNumber() {
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setPhoneNumber(String phoneNumber, String username, String profileImage) {
        phoneNumber=phoneNumber.trim();
        this.phoneNumber = phoneNumber;
        this.username=username;
        this.profileImage=profileImage;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    public String getUsername() {
        return username;
    }
    public String getProfileImage() {
        return profileImage;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    @Override
    public int compareTo(Contact contact) {
        return this.phoneNumber.compareTo(contact.phoneNumber);
    }

    // equal by phone number (name can be changed- want to enter the same chat even if the name edited somehow)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contact)) return false;
        Contact contact = (Contact) o;
        return Objects.equals(phoneNumber, contact.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phoneNumber);
    }
}
