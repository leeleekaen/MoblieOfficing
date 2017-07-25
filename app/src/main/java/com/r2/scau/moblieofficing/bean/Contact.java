package com.r2.scau.moblieofficing.bean;

import android.support.annotation.NonNull;

/**
 * Created by 嘉进 on 11:27.
 */

public class Contact implements Comparable<Contact> {
    private String name;
    private String phone;
    private String firstLetter;


    public Contact(){

    }
    public Contact(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public Contact(String name, String phone, String firstLetter) {
        this.name = name;
        this.phone = phone;
        this.firstLetter = firstLetter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFirstLetter() {
        return firstLetter;
    }

    public void setFirstLetter(String firstLetter) {
        this.firstLetter = firstLetter;
    }


    @Override
    public int compareTo(@NonNull Contact contact) {
        if(this.firstLetter.compareTo(contact.getFirstLetter()) > 0){
            return 1;
        }

        if(this.firstLetter.compareTo(contact.getFirstLetter()) < 0){
            return -1;
        }

        return 0;
    }
}