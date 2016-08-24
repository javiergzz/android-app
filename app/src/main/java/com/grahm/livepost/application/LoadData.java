package com.grahm.livepost.application;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by javiergonzalez on 6/21/16.
 */

public class LoadData extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }

}
