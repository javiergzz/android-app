package com.grahm.livepost.application;

import android.content.pm.PackageInfo;
import android.util.Log;

import com.alipay.euler.andfix.patch.PatchManager;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.firebase.database.FirebaseDatabase;
import com.grahm.livepost.R;
import com.grahm.livepost.activities.InitActivity;
import com.grahm.livepost.file.FileUtils;
import com.jcmore2.appcrash.AppCrash;
import com.percolate.foam.FoamApiKeys;
import com.percolate.foam.FoamMultiDexApplication;

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
        AppCrash.get().withInitActivity(InitActivity.class)
                .withBackgroundColor(android.R.color.white)
                .withView(R.layout.custom_error_view);
        //Quick patcher
        try {
            PatchManager patchManager = new PatchManager(this);
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String appversion = pInfo.versionName;
            patchManager.init(appversion);//current version
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Fresco.initialize(this);
        FileUtils.createApplicationFolder();
    }

}
