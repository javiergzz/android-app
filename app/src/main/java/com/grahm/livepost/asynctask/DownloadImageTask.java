package com.grahm.livepost.asynctask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.grahm.livepost.interfaces.OnCallbackImageListener;

import java.io.InputStream;

/**
 * Created by javiergonzalez on 6/21/16.
 */

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    private ImageView bmImage;
    private OnCallbackImageListener image;

    public DownloadImageTask(ImageView bmImage, OnCallbackImageListener callback) {
        this.bmImage = bmImage;
        this.image = callback;
    }

    public DownloadImageTask(OnCallbackImageListener callback) {
        this.bmImage = bmImage;
        this.image = callback;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon;
    }

    protected void onPostExecute(Bitmap result) {
        if(result != null) {
            if(bmImage != null){
                bmImage.setImageBitmap(result);
            }
            image.callback(result);
        }
    }

}