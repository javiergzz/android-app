package com.grahm.livepost.objects;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Map;

public class Update implements Serializable{
    //Strings for references
    public static final String MESSAGE_FIELD_STR = "message";
    public static final String TIMESTAMP_FIELD_STR = "timestamp";
    public static final String SENDER_KEY_FIELD_STR = "sender_key";
    public static final String SENDER_FIELD_STR = "sender";
    public static final String PROFILE_PICTURE_FIELD_STR = "profile_picture";
    public static final String LIKES_FIELD_STR = "likes";
    public static final String COUNT_LIKES_FIELD_STR = "count_likes";
    //Variables
    private String message;
    private Long timestamp;
    private String sender_key;
    private String sender;
    private String profile_picture;
    private Map<String,Integer> likes;
    private Integer count_likes;

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    private String thumb;
    //Empty constructor mandatory for Firebase
    public Update(){}
    public Update(int count_likes, Map<String, Integer> likes, String message, String profile_picture, String sender, String sender_key) {
        this.count_likes = count_likes;
        this.likes = likes;
        this.message = message;
        this.profile_picture = profile_picture;
        this.sender = sender;
        this.sender_key = sender_key;
    }

    public Update(int count_likes, Map<String, Integer> likes, String message, String thumb, String profile_picture, String sender, String sender_key) {
        this.count_likes = count_likes;
        this.likes = likes;
        this.message = message;
        this.profile_picture = profile_picture;
        this.sender = sender;
        this.sender_key = sender_key;
        this.thumb = thumb;
    }
    //Getters & setters
    public Integer getCount_likes() {
        return count_likes;
    }

    public void setCount_likes(Integer count_likes) {
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

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}