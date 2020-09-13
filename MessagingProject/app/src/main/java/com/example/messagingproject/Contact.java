package com.example.messagingproject;

import java.util.ArrayList;
import java.util.Objects;

// simple contact class
public class Contact extends Chat implements Comparable<Contact>{

    private String phoneNumber="";


    public void setPhoneNumber(String phoneNumber) {
        phoneNumber=phoneNumber.trim();
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public int compareTo(Contact contact) {
        return this.name.compareTo(contact.name);
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
