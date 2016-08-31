package com.grahm.livepost.asynctask;

import android.content.Context;
import android.os.AsyncTask;

import com.firebase.client.utilities.Utilities;
import com.grahm.livepost.interfaces.OnHttpListener;
import com.grahm.livepost.ui.Controls;

import org.json.JSONObject;

/**
 * Created by javiergonzalez on 8/29/16.
 */

public class HttpClient extends AsyncTask<Void, Void, Void> {

    private Context mContext;
    private OnHttpListener mListener;
    private JSONObject response;

    public HttpClient(Context context, OnHttpListener listener){
        this.mContext = context;
        this.mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Controls.createDialog(mContext, "Loading", false);
    }

    @Override
    protected Void doInBackground(Void... params) {
        response = mListener.doInBackground();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        mListener.onPostExecute(response);
        Controls.dismissDialog();
    }
}
