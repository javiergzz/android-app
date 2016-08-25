package com.grahm.livepost.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;

import com.grahm.livepost.R;

public class SplashScreen extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 3000;
    public static final String PREFS_NAME = "PrefsFile";
    public static final String PREFS_LOGIN = "isLogin";
    public static final String PREFS_ONBOARDING = "onboarding";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        final boolean isLogin = settings.getBoolean(PREFS_LOGIN, false);
        final boolean onboarding = settings.getBoolean(PREFS_ONBOARDING, false);
        final Class activity = (onboarding) ? (isLogin) ? MainActivity.class : Login.class : Onboarding.class;
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Class activity = Login.class;
                Intent mainIntent = new Intent(SplashScreen.this, activity);
                SplashScreen.this.startActivity(mainIntent);
                SplashScreen.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);

    }

}
