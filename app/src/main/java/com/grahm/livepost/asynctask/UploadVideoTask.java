package com.grahm.livepost.asynctask;

/*
* By Jorge E. Hernandez (@lalongooo) 2015
* */

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.amazonaws.mobileconnectors.s3.transfermanager.Upload;

import com.grahm.livepost.objects.MessageInfo;
import com.grahm.livepost.objects.MessageInfo.DownloadState;

public class UploadVideoTask extends AsyncTask<Void, Integer, Void> {
    private static final String TAG = UploadVideoTask.class.getSimpleName();
    final MessageInfo messageInfo;
    private Upload mUpload;

    public UploadVideoTask(MessageInfo info, Upload upload) {
        messageInfo = info;
        mUpload = upload;
    }

    @Override
    protected void onPreExecute() {
        messageInfo.setDownloadState(DownloadState.DOWNLOADING);
    }

    @Override
    protected Void doInBackground(Void... params) {
        messageInfo.setDownloadState(DownloadState.DOWNLOADING);
        while (!mUpload.isDone()) {
            publishProgress((int) mUpload.getProgress().getPercentTransferred());
        }
        publishProgress(100);
        messageInfo.setDownloadState(DownloadState.COMPLETE);
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
        messageInfo.setDownloadState(DownloadState.COMPLETE);
        ProgressBar bar = messageInfo.getProgressBar();
        if (bar != null) {
            bar.setVisibility(View.INVISIBLE);
            bar.invalidate();
        }
        Log.i("Uploaded:", messageInfo.getFilename());
    }
}
