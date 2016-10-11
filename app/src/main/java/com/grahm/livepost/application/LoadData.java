package com.grahm.livepost.application;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.database.FirebaseDatabase;
import com.percolate.foam.FoamMultiDexApplication;


/**
 * Created by javiergonzalez on 6/21/16.
 */

public class LoadData extends FoamMultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }

}
