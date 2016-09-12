package com.grahm.livepost.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.grahm.livepost.R;
import com.grahm.livepost.objects.FirebaseActivity;
import com.grahm.livepost.util.Utilities;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

public class SplashScreen extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 3000;
    public static final String PREFS_NAME = "PrefsFile";
    public static final String PREFS_LOGIN = "isLogin";
    public static final String PREFS_AUTH = "auth";
    public static final String PREFS_LIVEPOST = "livepost";
    public static final String PREFS_TWITTER = "twitter";
    public static final String PREFS_ONBOARDING = "onboarding";
    private Class mForwardActivity;
    private static final String TWITTER_KEY = "roDB8OWxSlYv3hiKXYnPusPUJ";
    private static final String TWITTER_SECRET = "hV1sxHw8CcQaFMnKU59F0ze5qFVEcxDrRtQCouf2sXWXoZ300w";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(SplashScreen.this, new Twitter(authConfig));
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        final boolean onboarding = settings.getBoolean(PREFS_ONBOARDING, false);
        final boolean isLogin = settings.getBoolean(PREFS_LOGIN, false);
        setContentView(R.layout.activity_splash_screen);
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .memoryCache(new WeakMemoryCache())
                .denyCacheImageMultipleSizesInMemory()
                .threadPoolSize(5)
                .defaultDisplayImageOptions(options)
                .build();
        ImageLoader.getInstance().init(config);
//        FirebaseAuth auth = FirebaseAuth.getInstance();
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        //Choose between Onboarding, Main Activity or Login/Signup activities depending on local content and firebase token status
//        if(!onboarding){
//            mForwardActivity = Onboarding.class;
//        } else if (auth.getCurrentUser() != null) {
//            //Signed in
//            Utilities.getUser(ref,this,savedInstanceState);
//            mForwardActivity = MainActivity.class;
//        } else {
            // not signed in
            mForwardActivity = (isLogin) ? MainActivity.class :  Login.class;

//        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashScreen.this, mForwardActivity);
                SplashScreen.this.startActivity(mainIntent);
                SplashScreen.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);




    }

}
