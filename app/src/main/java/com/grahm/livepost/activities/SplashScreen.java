package com.grahm.livepost.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.database.FirebaseDatabase;
import com.grahm.livepost.R;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

import static com.grahm.livepost.util.GV.TWITTER_KEY;
import static com.grahm.livepost.util.GV.TWITTER_SECRET;

public class SplashScreen extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 3000;
    public static final String PREFS_NAME = "PrefsFile";
    public static final String PREFS_LOGIN = "isLogin";
    public static final String PREFS_AUTH = "auth";
    public static final String PREFS_LIVEPOST = "livepost";
    public static final String PREFS_TWITTER = "twitter";
    public static final String PREFS_ONBOARDING = "onboarding";
    private Class mForwardActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(SplashScreen.this, new Twitter(authConfig));
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        final boolean onboarding = settings.getBoolean(PREFS_ONBOARDING, false);
        final boolean isLogin = settings.getBoolean(PREFS_LOGIN, false);
        setContentView(R.layout.activity_splash_screen);

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
