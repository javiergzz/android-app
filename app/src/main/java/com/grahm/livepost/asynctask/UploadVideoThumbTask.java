package com.grahm.livepost.asynctask;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;
import com.grahm.livepost.R;
import com.grahm.livepost.interfaces.OnPutImageListener;
import com.grahm.livepost.objects.Story;
import com.grahm.livepost.objects.Update;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.util.GV;
import com.grahm.livepost.util.Util;
import com.grahm.livepost.util.Utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

public class UploadVideoThumbTask extends AsyncTask<Bitmap, String, Void> {
    public static final String TAG = "UploadVideoThumbTask";
    private ProgressDialog dialog;
    private Context mContext;
    private AmazonS3Client mS3Client;
    private String mThumbName;
    private DatabaseReference mUpdateRef;
    private Update mUpdate;

    public UploadVideoThumbTask(DatabaseReference ref,Update update, Context context, AmazonS3Client client) {
        mContext = context;
        mS3Client = client;
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
        ObjectMetadata metadata = new ObjectMetadata();
        try {
            //Upload thumbnail
            ResponseHeaderOverrides override = new ResponseHeaderOverrides();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            //scaledBitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            thumb.compress(Bitmap.CompressFormat.PNG, 80, bos);
            byte[] bitmapdata = bos.toByteArray();
            ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
            metadata.setContentLength(bos.size());
            metadata.setContentType("image/png");
            if(bos.size()>0) {
                PutObjectRequest por = new PutObjectRequest(GV.VIDEO_BUCKET, thumbName, bs, metadata).withCannedAcl(CannedAccessControlList.PublicRead);
                mS3Client.putObject(por);
                override = new ResponseHeaderOverrides();
                override.setContentType("image/png");
                GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(GV.VIDEO_BUCKET, thumbName);
                urlRequest.setExpiration(new Date(System.currentTimeMillis() + 3600000));
                urlRequest.setResponseHeaders(override);
                URL thumbUrlUri = mS3Client.generatePresignedUrl(urlRequest);
                if(thumbUrlUri!=null)
                    updateFirebaseEntry("<thumb>"+thumbUrlUri.toString()+"</thumb>");

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