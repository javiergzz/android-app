package com.grahm.livepost.asynctask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;


import com.grahm.livepost.R;
import com.grahm.livepost.util.GV;
import com.grahm.livepost.util.Utilities;

import static com.grahm.livepost.util.AzureUtils.deleteBlob;

public class DeleteVideoTask extends AsyncTask<Void, Void, Void> {
    public static final String TAG = "DeleteVideoTask";
    private ProgressDialog dialog;
    private Context mContext;
    private String mUrl;
    private String mThumb;

    public DeleteVideoTask(Context context, String url, String thumb) {
        mContext = context;
        mUrl = url;
        mThumb = thumb;
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
        deleteBlob(deleteUrl,GV.VIDEO_BUCKET);
        if(mThumb!=null)
            deleteBlob(mThumb,GV.PICTURE_BUCKET);
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