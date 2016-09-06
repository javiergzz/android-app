package com.grahm.livepost.interfaces;

import org.json.JSONObject;

public interface OnHttpListener {
    JSONObject doInBackground();
    void onPostExecute(JSONObject response);
}
