package com.grahm.livepost.asynctask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import com.grahm.livepost.R;
import com.grahm.livepost.util.GV;
import com.grahm.livepost.util.Utilities;

import static com.grahm.livepost.util.AzureUtils.deleteBlob;

public class DeleteImageTask extends AsyncTask<Void, Void, Void> {
    public static final String TAG = "DeleteImageTask";
    private ProgressDialog dialog;
    private Context mContext;
    private String mUrl;

    public DeleteImageTask(Context context,String url) {
        mContext = context;
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
        String deleteUrl = Utilities.cleanUrl(mUrl);
        deleteBlob(deleteUrl,GV.PICTURE_BUCKET);
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