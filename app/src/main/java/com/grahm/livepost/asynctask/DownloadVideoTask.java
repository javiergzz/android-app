package com.grahm.livepost.asynctask;

/*
* By Jorge E. Hernandez (@lalongooo) 2015
* */

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import com.amazonaws.mobileconnectors.s3.transfermanager.Download;

import com.grahm.livepost.file.FileUtils;
import com.grahm.livepost.network.TransferController;

import com.grahm.livepost.objects.MessageInfo;


public class DownloadVideoTask extends AsyncTask<Void, Integer, Void> {

    private static final String TAG = DownloadVideoTask.class.getSimpleName();
    final MessageInfo messageInfo;
    private TransferController transferController;
    private DownloadComplete downloadComplete;
    public interface DownloadComplete {
        void downloadComplete(Bitmap bitmap);
    }

    public DownloadVideoTask(MessageInfo info, TransferController transferController, DownloadComplete downloadComplete) {
        this.transferController = transferController;
        this.messageInfo = info;
        this.downloadComplete = downloadComplete;
    }

    public DownloadVideoTask(MessageInfo info, TransferController transferController) {
        this.transferController = transferController;
        this.messageInfo = info;
        this.downloadComplete = null;
    }

    @Override
    protected void onPreExecute() {
        messageInfo.setDownloadState(MessageInfo.DownloadState.DOWNLOADING);
    }

    @Override
    protected Void doInBackground(Void... params) {

        Download download;
        if(downloadComplete!=null){
            download = transferController.downloadThumbnail(messageInfo.getFilename());
        }else{
            download = transferController.downloadVideo(messageInfo.getFilename());
        }

        messageInfo.setDownloadState(MessageInfo.DownloadState.DOWNLOADING);
        while (!download.isDone()) {
            publishProgress((int) download.getProgress().getPercentTransferred());
        }
        publishProgress(100);
        messageInfo.setDownloadState(MessageInfo.DownloadState.COMPLETE);
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        messageInfo.setProgress(values[0]);
        ProgressBar bar = messageInfo.getProgressBar();
        if (bar != null) {
            bar.setVisibility(View.VISIBLE);
            bar.setProgress(messageInfo.getProgress());
            bar.invalidate();
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        if(downloadComplete != null){
            downloadComplete.downloadComplete(FileUtils.getBitmap(messageInfo.getFilename()));
        }
        messageInfo.setDownloadState(MessageInfo.DownloadState.COMPLETE);
        ProgressBar bar = messageInfo.getProgressBar();
        if (bar != null) {
            bar.setVisibility(View.INVISIBLE);
        }
    }
}
