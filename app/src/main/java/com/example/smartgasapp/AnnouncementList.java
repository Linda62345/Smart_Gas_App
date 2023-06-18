package com.example.smartgasapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class AnnouncementList {
    String name, type, description;
    public AnnouncementList(String Name, String Type, String Description){
        this.name = Name;
        this.type = Type;
        this.description = Description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}