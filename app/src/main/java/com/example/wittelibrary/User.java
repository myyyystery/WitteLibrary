package com.example.wittelibrary;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private int id;

    @SerializedName("username")
    private String username;

    @SerializedName("permission")
    private int permission;

    @SerializedName("name")
    private String name;

    @SerializedName("course")
    private int course;

    @SerializedName("groupname")
    private String groupname;

    // Getters and setters
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public int getPermission() {
        return permission;
    }

    public String getName() {
        return name;
    }

    public int getCourse() {
        return course;
    }

    public String getGroupname() {
        return groupname;
    }
}
