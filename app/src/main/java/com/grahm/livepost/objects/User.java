package com.grahm.livepost.objects;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * Created by Vyz on 2015-12-29.
 */
public class User implements Serializable {
    private String email;
    private String name;
    private  Map<String, Object> posts_contributed;
    private  Map<String, Object> posts_created;
    private String profile_picture;
    private long timestamp;
    private String uid;

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

    public User() {
    }

    public User(String email, String name, Map<String, Object> posts_contributed, Map<String, Object> posts_created, String profile_picture, long timestamp, String uid) {
        this.email = email;
        this.name = name;
        this.posts_contributed = posts_contributed;
        this.posts_created = posts_created;
        this.profile_picture = profile_picture;
        this.timestamp = timestamp;
        this.uid = uid;
    }
}
