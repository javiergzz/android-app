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
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class SplashScreen extends FirebaseActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 3000;
    public static final String PREFS_NAME = "PrefsFile";
    public static final String PREFS_LOGIN = "isLogin";
    public static final String PREFS_ONBOARDING = "onboarding";
    private Class mForwardActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        final boolean isLogin = settings.getBoolean(PREFS_LOGIN, false);
        final boolean onboarding = settings.getBoolean(PREFS_ONBOARDING, false);
//        final Class activity = (onboarding) ? (isLogin) ? MainActivity.class : SignUpPage.class : Onboarding.class;
        setContentView(R.layout.activity_splash_screen);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        if (auth.getCurrentUser() != null) {
            //Signed in
            Utilities.getUser(ref,this,savedInstanceState);
            mForwardActivity = MainActivity.class;
        } else {
            // not signed in
            mForwardActivity = SignUpPage.class;

        }

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
