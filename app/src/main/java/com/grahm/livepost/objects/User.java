package com.grahm.livepost.objects;

import android.text.TextUtils;

import java.util.Date;

/**
 * Created by javiergonzalez on 8/26/16.
 */

public class User {
    private String email;
    private String name;
    private String picture;
    private Date timestamp;
    private String uid;
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String profile_picture) {
        this.picture = profile_picture;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void merge(User user){
        this.name = TextUtils.isEmpty(user.getName()) ? this.name : user.getName();
        this.email = TextUtils.isEmpty(user.getEmail()) ? this.email : user.getEmail();
        this.password = TextUtils.isEmpty(user.getPassword()) ? this.password : user.getPassword();
    }
}
