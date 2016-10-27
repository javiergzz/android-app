package com.grahm.livepost.objects;

import java.io.Serializable;

/**
 * Created by Vyz on 2016-10-21.
 */

public class Invite implements Serializable {
    private String storyId;
    private String storyTitle;
    private String senderKey;
    private String senderProfilePicture;
    private Long timestamp;
    private Boolean seen = false;

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getStoryTitle() {
        return storyTitle;
    }

    public void setStoryTitle(String storyTitle) {
        this.storyTitle = storyTitle;
    }

    public String getSenderKey() {
        return senderKey;
    }

    public void setSenderKey(String senderKey) {
        this.senderKey = senderKey;
    }

    public String getSenderProfilePicture() {
        return senderProfilePicture;
    }

    public void setSenderProfilePicture(String senderProfilePicture) {
        this.senderProfilePicture = senderProfilePicture;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }
    public Invite(){}
    public Invite(String storyId, String storyTitle, String senderKey, String senderProfilePicture) {
        this.storyId = storyId;
        this.senderKey = senderKey;
        this.senderProfilePicture = senderProfilePicture;
        this.storyTitle = storyTitle;
        this.seen = false;
    }
}
