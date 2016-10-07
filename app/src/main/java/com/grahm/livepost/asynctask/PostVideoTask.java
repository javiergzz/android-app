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
import com.google.gson.Gson;
import com.grahm.livepost.R;
import com.grahm.livepost.interfaces.OnPutImageListener;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.util.GV;
import com.grahm.livepost.util.Util;
import com.grahm.livepost.util.Utilities;

import java.io.File;
import java.io.InputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Date;

public class PostVideoTask extends AsyncTask<Uri, String, String> {
    public static final String TAG = "PostVideoTask";
    private ProgressDialog dialog;
    private Context mContext;
    private AmazonS3Client mS3Client;
    private OnPutImageListener mListener;
    private String mVideoName;
    private Boolean mShowDialog;
    private User mUser;
    SharedPreferences mSharedPref;

    public PostVideoTask(Context context, AmazonS3Client client, OnPutImageListener listener, Boolean showDialog) {
        mContext = context;
        mS3Client = client;
        mListener = listener;
        mShowDialog = showDialog;
        mSharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        String s = mSharedPref.getString("user", null);
        if (!TextUtils.isEmpty(s)) {
            mUser = new Gson().fromJson(s, User.class);
        }
    }

    protected void onPreExecute() {
        dialog = new ProgressDialog(mContext);
        dialog.setMessage(mContext.getString(R.string.ns_creating_text));
        dialog.setCancelable(false);
        if (mShowDialog) {
            dialog.show();
        }
    }

    protected String doInBackground(Uri... uris) {
        String url = "";
        if (uris == null || uris.length != 1) {
            return null;
        }
        // The file location of the image selected.
        Uri selectedVideo = uris[0];
        if (selectedVideo != null) {
            url = postVideo(selectedVideo);
        }
        return url;
    }

    protected void onPostExecute(String result) {
        if (mShowDialog) {
            dialog.dismiss();
            mListener.onSuccess(result);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (mShowDialog) {
            dialog.dismiss();
        }
    }

    protected String postVideo(Uri srcUri) {
        String url = "";

        long l = System.currentTimeMillis()/1000L;
        mVideoName = mUser.getName()+l;
        //compressVideo(srcUri);

        url = uploadVideo(mVideoName + ".mp4", srcUri);
        return url;
    }

    private void compressVideo(Uri srcUri)
    {
        Util.setupFfmpeg(mContext);
        FFmpeg ffmpeg = FFmpeg.getInstance(mContext);
        if(ffmpeg.isFFmpegCommandRunning()){
            Toast.makeText(mContext, mContext.getString(R.string.error_video_uploader_busy), Toast.LENGTH_LONG).show();
            return;
        }
        ;
        String [] cmd = {"-y","-i "+ srcUri.getPath()+" "+mContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES).toString()};
        try {
            // to execute "ffmpeg -version" command you just need to pass "-version"
            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {}

                @Override
                public void onProgress(String message) {
                    Log.d(TAG,message);
                }

                @Override
                public void onFailure(String message) {}

                @Override
                public void onSuccess(String message) {
                    Log.d(TAG,message);
                }

                @Override
                public void onFinish() {}
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Handle if FFmpeg is already running
            Toast.makeText(mContext, mContext.getString(R.string.error_video_uploader_busy), Toast.LENGTH_LONG).show();
        }
    }
    private String uploadVideo(String videoName, Uri srcUri) {
        String url = "";
        ContentResolver resolver = mContext.getContentResolver();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(resolver.getType(srcUri));

        try {
            InputStream in = resolver.openInputStream(srcUri);
            metadata.setContentLength(in.available());
            PutObjectRequest por = new PutObjectRequest(GV.VIDEO_BUCKET, videoName, in, metadata).withCannedAcl(CannedAccessControlList.PublicRead);
            mS3Client.putObject(por);
            ResponseHeaderOverrides override = new ResponseHeaderOverrides();
            override.setContentType("video/mp4");
            GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(GV.VIDEO_BUCKET, videoName);
            urlRequest.setExpiration(new Date(System.currentTimeMillis() + 3600000));
            urlRequest.setResponseHeaders(override);
            URL urlUri = mS3Client.generatePresignedUrl(urlRequest);
            Uri.parse(urlUri.toURI().toString());
            url = "<video>"+urlUri.toString()+"</video>";

            //Upload thumbnail
            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(new File(srcUri.getPath()).getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            //scaledBitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            thumb.compress(Bitmap.CompressFormat.PNG, 80, bos);
            byte[] bitmapdata = bos.toByteArray();
            ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
            metadata.setContentLength(bos.size());
            metadata.setContentType("image/png");
            if(bos.size()>0) {
                Log.e(TAG,Utilities.replaceExtension(videoName,".png"));
                por = new PutObjectRequest(GV.VIDEO_BUCKET, Utilities.replaceExtension(videoName,".png"), bs, metadata).withCannedAcl(CannedAccessControlList.PublicRead);
                mS3Client.putObject(por);
                override = new ResponseHeaderOverrides();
                override.setContentType(Util.getMimeTypeFromUri(mContext, srcUri));
                urlRequest = new GeneratePresignedUrlRequest(GV.VIDEO_BUCKET, Utilities.replaceExtension(videoName,".png"));
                urlRequest.setExpiration(new Date(System.currentTimeMillis() + 3600000));
                urlRequest.setResponseHeaders(override);
                mS3Client.generatePresignedUrl(urlRequest);
                URL thumbUrlUri = mS3Client.generatePresignedUrl(urlRequest);
                url += "<thumb>"+thumbUrlUri.toString()+"</thumb>";

            }
        } catch (Exception exception) {
            Log.e("AsyncTask", "Error: " + exception);
        }
        return url;

    }


}