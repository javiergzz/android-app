package com.grahm.livepost.asynctask;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.grahm.livepost.objects.Update;
import com.grahm.livepost.util.AzureUtils;
import com.grahm.livepost.util.GV;
import com.grahm.livepost.util.Utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class UploadVideoThumbTask extends AsyncTask<Bitmap, String, Void> {
    public static final String TAG = "UploadVideoThumbTask";
    private ProgressDialog dialog;
    private Context mContext;
    private String mThumbName;
    private DatabaseReference mUpdateRef;
    private Update mUpdate;

    public UploadVideoThumbTask(DatabaseReference ref,Update update, Context context) {
        mContext = context;
        mUpdateRef = ref;
        mUpdate = update;
    }

    protected Void doInBackground(Bitmap... bmps) {
        if (bmps == null || bmps.length != 1) {
            return null;
        }
        // The bitmap of the image selected.
        Bitmap thumb = bmps[0];
        if (thumb != null) {
           postThumb(thumb);
        }
        return null;
    }


    protected String postThumb(Bitmap thumb) {
        String url = "";

        long l = System.currentTimeMillis()/1000L;
        mThumbName = mUpdate.getSender()+l;
        //compressVideo(srcUri);
        url = uploadThumb(mThumbName + ".png", thumb);
        return url;
    }

    private String uploadThumb(String thumbName, Bitmap thumb) {
        String url = "";
        try {
            //Upload thumbnail
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            //scaledBitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            thumb.compress(Bitmap.CompressFormat.PNG, 80, bos);
            byte[] bitmapdata = bos.toByteArray();
            ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
            if(bos.size()>0) {
                url = AzureUtils.uploadBlob(thumbName, bs, GV.PICTURE_BUCKET);
                if(url!=null)
                    updateFirebaseEntry("<thumb>"+Utilities.cleanUrl(url)+"</thumb>");

            }
        } catch (Exception exception) {
            Log.e("AsyncTask", "Error: " + exception);
        }
        return url;
    }
    private void updateFirebaseEntry(String thumbTag){
            mUpdateRef.child(Update.MESSAGE_FIELD_STR).setValue("<video>"+mUpdate.getMessage()+"</video>"+thumbTag);
    }

}