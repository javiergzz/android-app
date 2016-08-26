package com.grahm.livepost.objects;

import java.sql.Timestamp;

/**
 * Created by Vyz on 2015-12-28.


 posts_picture:
 "http://livepostrocks.s3.amazonaws.com/placehold..."
 timestamp:
 1471592622494
 title:
 "Protest in NY"
 */
public class Post {
    private String author;
    private String author_name;
    private String category;
    private boolean isLive;

    private String last_message;
    private long last_time;
    private String posts_picture;
    private Timestamp timestamp;
    private String title;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor_name() {
        return author_name;
    }

    public void setAuthor_name(String author_name) {
        this.author_name = author_name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isLive() {
        return isLive;
    }

    public void setIsLive(boolean isLive) {
        this.isLive = isLive;
    }

    public String getLast_message() {
        return last_message;
    }

    public void setLast_message(String last_message) {
        this.last_message = last_message;
    }

    public long getLast_time() {
        return last_time;
    }

    public void setLast_time(long last_time) {
        this.last_time = last_time;
    }

    public String getPosts_picture() {
        return posts_picture;
    }

    public void setPosts_picture(String posts_picture) {
        this.posts_picture = posts_picture;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Post(){}
    public Post(String author, String author_name, String category, boolean isLive, String last_message, long last_time, String posts_picture, Timestamp timestamp, String title) {
        this.author = author;
        this.author_name = author_name;
        this.category = category;
        this.isLive = isLive;
        this.last_message = last_message;
        this.last_time = last_time;
        this.posts_picture = posts_picture;
        this.timestamp = timestamp;
        this.title = title;
    }

}
