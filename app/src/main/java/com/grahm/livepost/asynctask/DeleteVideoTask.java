package com.grahm.livepost.asynctask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.grahm.livepost.R;
import com.grahm.livepost.util.GV;
import com.grahm.livepost.util.Utilities;

public class DeleteVideoTask extends AsyncTask<Void, Void, Void> {
    public static final String TAG = "DeleteVideoTask";
    private ProgressDialog dialog;
    private Context mContext;
    private AmazonS3Client mS3Client;
    private String mUrl;

    public DeleteVideoTask(Context context, AmazonS3Client client, String url) {
        mContext = context;
        mS3Client = client;
        mUrl = url;
    }


    protected void onPreExecute() {
        dialog = new ProgressDialog(mContext);
        dialog.setMessage(mContext.getString(R.string.at_deleting));
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        String deleteUrl = Utilities.cleanVideoUrl(mUrl);
        //delete video
        DeleteObjectRequest doq = new DeleteObjectRequest(GV.VIDEO_BUCKET, deleteUrl);
        mS3Client.deleteObject(doq);
        //delete thumbnail
        doq = new DeleteObjectRequest(GV.PICTURE_BUCKET, deleteUrl.replace(".mp4", ".png"));
        mS3Client.deleteObject(doq);
        return null;
    }

    protected void onPostExecute(Void result) {
        dialog.dismiss();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        dialog.dismiss();
    }

}