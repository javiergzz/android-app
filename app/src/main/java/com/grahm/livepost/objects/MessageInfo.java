package com.grahm.livepost.objects;

/*
* By Jorge E. Hernandez (@lalongooo) 2015
* */

import android.widget.ProgressBar;

import com.grahm.livepost.util.Util;


public class MessageInfo {
    private final static String TAG = MessageInfo.class.getSimpleName();

    public enum DownloadState {
        NOT_STARTED,
        QUEUED,
        DOWNLOADING,
        COMPLETE
    }

    public enum MessageType {
        IMAGE,
        TEXT,
        VIDEO
    }

    private volatile DownloadState mDownloadState = DownloadState.NOT_STARTED;
    private final String mFilename;
    private final MessageType mMsgType;
    private final String mDateSent;

    private volatile Integer mProgress;
    private volatile ProgressBar mProgressBar;

    public MessageInfo(String filename) {
        this.mFilename = filename;
        this.mProgress = 0;
        this.mProgressBar = null;
        this.mMsgType = MessageType.TEXT;
        this.mDateSent = Util.getDateTime();
    }

    public MessageInfo(String filename, MessageType msgType) {
        this.mFilename = filename;
        this.mProgress = 0;
        this.mProgressBar = null;
        this.mMsgType = msgType;
        this.mDateSent = Util.getDateTime();
    }

    public MessageInfo(String filename, MessageType msgType, String dateSent) {
        this.mFilename = filename;
        this.mProgress = 0;
        this.mProgressBar = null;
        this.mMsgType = msgType;
        this.mDateSent = dateSent;
    }

    public MessageType getType() {
        return mMsgType;
    }

    public String getDateSent() {
        return mDateSent;
    }

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

    public void setProgressBar(ProgressBar progressBar) {
        mProgressBar = progressBar;
    }

    public void setDownloadState(DownloadState state) {
        mDownloadState = state;
    }

    public DownloadState getDownloadState() {
        return mDownloadState;
    }

    public Integer getProgress() {
        return mProgress;
    }

    public void setProgress(Integer progress) {
        this.mProgress = progress;
    }

    public String getFilename() {
        return mFilename;
    }
}
