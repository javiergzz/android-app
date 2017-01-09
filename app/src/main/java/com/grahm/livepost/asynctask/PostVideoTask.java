package com.grahm.livepost.asynctask;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import com.grahm.livepost.R;
import com.grahm.livepost.interfaces.OnPutImageListener;
import com.grahm.livepost.util.AzureUtils;
import com.grahm.livepost.util.GV;
import com.grahm.livepost.util.Utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

public class PostVideoTask extends AsyncTask<Uri, String, String> {
    public static final String TAG = "PostVideoTask";
    private ProgressDialog dialog;
    private Context mContext;
    private OnPutImageListener mListener;
    private String mVideoName;
    private Boolean mShowDialog;
    private String mStoryId;

    public PostVideoTask(Context context, OnPutImageListener listener, String storyId, Boolean showDialog) {
        mContext = context;
        mListener = listener;
        mShowDialog = showDialog;
        mStoryId = storyId;
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
        long l = System.currentTimeMillis() / 1000L;
        mVideoName = mStoryId + "_" + l;
        url = uploadVideo(mVideoName + ".mp4", srcUri);
        return url;
    }

    private String uploadVideo(String videoName, Uri srcUri) {
        String url = "";
        String videoUrl = "";
        String thumbUrl = "";
        ContentResolver resolver = mContext.getContentResolver();

        try {
            InputStream in = resolver.openInputStream(srcUri);
            videoUrl = AzureUtils.uploadBlob(videoName, in, GV.VIDEO_BUCKET);
            url = "<video>" + Utilities.cleanUrl(videoUrl) + "</video>";
            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(new File(srcUri.getPath()).getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            thumb.compress(Bitmap.CompressFormat.PNG, 80, bos);
            byte[] bitmapdata = bos.toByteArray();
            ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
            if (bos.size() > 0) {
                Log.e(TAG, Utilities.replaceExtension(videoName, ".png"));
                thumbUrl = AzureUtils.uploadBlob(Utilities.replaceExtension(videoName, ".png"), bs, GV.PICTURE_BUCKET);
                url += "<thumb>" + Utilities.cleanUrl(thumbUrl) + "</thumb>";
            }
        } catch (Exception exception) {
            Log.e("AsyncTask", "Error: " + exception);
        }
        return url;

    }


}