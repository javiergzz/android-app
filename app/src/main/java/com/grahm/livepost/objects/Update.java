package com.grahm.livepost.objects;

import java.sql.Timestamp;
import java.util.Map;

public class Update {

    private String message;
    private Timestamp timestamp;
    private String sender_key;
    private String sender;
    private String profile_picture;
    private Map<String,Integer> likes;
    private int count_likes;
    public Update(){}
    public Update(int count_likes, Map<String, Integer> likes, String message, String profile_picture, String sender, String sender_key, Timestamp timestamp) {
        this.count_likes = count_likes;
        this.likes = likes;
        this.message = message;
        this.profile_picture = profile_picture;
        this.sender = sender;
        this.sender_key = sender_key;
        this.timestamp = timestamp;
    }

    public int getCount_likes() {
        return count_likes;
    }

    public void setCount_likes(int count_likes) {
        this.count_likes = count_likes;
    }

    public Map<String, Integer> getLikes() {
        return likes;
    }

    public void setLikes(Map<String, Integer> likes) {
        this.likes = likes;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getProfile_picture() {
        return profile_picture;
    }

    public void setProfile_picture(String profile_picture) {
        this.profile_picture = profile_picture;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSender_key() {
        return sender_key;
    }

    public void setSender_key(String sender_key) {
        this.sender_key = sender_key;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
