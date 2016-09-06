package com.grahm.livepost.objects;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by javiergonzalez on 6/21/16.
 */

public class Story implements Serializable {

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

    public String getLast_message() {
        return last_message;
    }

    public void setLast_message(String last_message) {
        this.last_message = last_message;
    }

    public Long getLast_time() {
        return last_time;
    }

    public Long getTimestamp(){
        return timestamp;
    }

    public void setLast_time(Long last_time) {
        this.last_time = last_time;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPosts_picture() {
        return posts_picture;
    }

    public void setPosts_picture(String posts_picture) {
        this.posts_picture = posts_picture;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }


    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean getIsLive() {
        return Boolean.parseBoolean(isLive);
    }

    public void setIsLive(boolean live) {
       this.isLive = String.valueOf(live);
    }

    private String author;
    private String author_name;
    private String category;
    private String last_message;
    private Long last_time;
    private Double lat;
    private Double lng;
    private String location;
    private String posts_picture;
    private String subcategory;
    private Long timestamp;
    private String title;
    private String isLive;

    public Story (){}

    public Story(String author, String author_name, String category, String last_message, double lat, double lng, String location, String posts_picture, String subcategory, String title,boolean isLive) {
        this.author = author;
        this.author_name = author_name;
        this.category = category;
        this.last_message = last_message;
        this.lat = lat;
        this.lng = lng;
        this.location = location;
        this.posts_picture = posts_picture;
        this.subcategory = subcategory;
        this.title = title;
        this.isLive = String.valueOf(isLive);
    }
}
