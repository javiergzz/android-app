package com.grahm.livepost.objects;

import android.text.TextUtils;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * Created by Vyz on 2015-12-29.
 */
public class User implements Serializable {
    //Strings for references
    public static final String EMAIL_FIELD_STR = "email";
    public static final String NAME_FIELD_STR = "name";
    public static final String POSTS_CONTRIBUTED_FIELD_STR = "posts_contributed";
    public static final String POSTS_CREATED_FIELD_STR = "posts_created";
    public static final String PROFILE_PICTURE_FIELD_STR = "profile_picture";
    public static final String TIMESTAMP_FIELD_STR = "timestamp";
    public static final String UID_FIELD_STR = "uid";
    //Variables
    private String email;
    private String name;
    private Map<String, Object> posts_contributed;
    private Map<String, Object> posts_created;
    private String profile_picture;
    private Long timestamp;
    private String username;
    private String twitter;
    private String uid;
    //Getters & setters

    public String getAuthorString() {
        return TextUtils.isEmpty(uid) ? twitter : uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

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

    public Map<String, Object> getPosts_contributed() {
        return posts_contributed;
    }

    public void setPosts_contributed(Map<String, Object> posts_contributed) {
        this.posts_contributed = posts_contributed;
    }

    public Map<String, Object> getPosts_created() {
        return posts_created;
    }

    public void setPosts_created(Map<String, Object> posts_created) {
        this.posts_created = posts_created;
    }

    public String getProfile_picture() {
        return profile_picture;
    }

    public void setProfile_picture(String profile_picture) {
        this.profile_picture = profile_picture;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    //Dual setter with some logic
    public void merge(User user) {
        this.name = TextUtils.isEmpty(user.getName()) ? this.name : user.getName();
        this.email = TextUtils.isEmpty(user.getEmail()) ? this.email : user.getEmail();
    }

    //Empty constructor mandatory for Firebase
    public User() {
    }

    public User(String email, String name, Map<String, Object> posts_contributed, Map<String, Object> posts_created, String profile_picture, String uid) {
        this.email = email;
        this.name = name;
        this.posts_contributed = posts_contributed;
        this.posts_created = posts_created;
        this.profile_picture = profile_picture;
        this.uid = uid;
    }

    @Override
    public String toString() {
        return name == null ? username : name;
    }
}