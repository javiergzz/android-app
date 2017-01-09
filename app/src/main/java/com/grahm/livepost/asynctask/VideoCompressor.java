package com.grahm.livepost.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import com.grahm.livepost.interfaces.OnPutVideoListener;
import com.grahm.livepost.video.MediaController;

import java.io.File;

/**
 * Created by javiergonzalez on 1/9/17.
 */

public class VideoCompressor extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = "VideoCompressor";
    private File mTempFile;
    private String mVideoName;
    private OnPutVideoListener mListener;

    public VideoCompressor(File file, String videoName, OnPutVideoListener listener){
        mTempFile = file;
        mVideoName = videoName;
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d(TAG,"Start video compression");
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        return MediaController.getInstance().convertVideo(mTempFile.getPath(), mVideoName);
    }

    @Override
    protected void onPostExecute(Boolean compressed) {
        super.onPostExecute(compressed);
        if(compressed){
            if(mListener != null){
                mListener.onSuccess(mVideoName);
            }
            Log.d(TAG,"Compression successfull!");
        }
    }
}