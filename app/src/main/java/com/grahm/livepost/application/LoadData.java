package com.grahm.livepost.application;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.util.Log;

import com.alipay.euler.andfix.patch.PatchManager;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.flurry.android.FlurryAgent;
import com.jcmore2.appcrash.AppCrash;
import com.percolate.foam.FoamApiKeys;
import com.percolate.foam.FoamMultiDexApplication;


/**
 * Created by javiergonzalez on 6/21/16.
 */
@FoamApiKeys(
        papertrail = "logs4.papertrailapp.com:50563",
        flurry = "CM4JRYJJMR4XM24KTRHZ"
)
public class LoadData extends FoamMultiDexApplication {
    private static final String TAG = "LoadData";
    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
        //Foam Logger
        AppEventsLogger.activateApp(this);
        //App crash catcher
        AppCrash.init(this);
        //Quick patcher
        try {
            PatchManager patchManager = new PatchManager(this);
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String appversion = pInfo.versionName;
            patchManager.init(appversion);//current version
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }
    }

}
