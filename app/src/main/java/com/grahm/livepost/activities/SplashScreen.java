package com.grahm.livepost.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.grahm.livepost.R;

import com.grahm.livepost.objects.User;
import com.grahm.livepost.util.Utilities;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

import static com.grahm.livepost.util.GV.TWITTER_KEY;
import static com.grahm.livepost.util.GV.TWITTER_SECRET;

public class SplashScreen extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 3000;
    public static final String PREFS_LOGIN = "isLogin";
    public static final String PREFS_AUTH = "auth";
    public static final String PREFS_LIVEPOST = "livepost";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(SplashScreen.this, new Twitter(authConfig));
        SharedPreferences settings = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);
        boolean isLogin = settings.getBoolean(PREFS_LOGIN, false);
        setContentView(R.layout.activity_splash_screen);
        checkIfUserExists(savedInstanceState, isLogin);
    }

    private  User getQuickUser(Bundle savedInstanceState){
        User user;
        Gson gson = new Gson();
        SharedPreferences SP = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        if (savedInstanceState == null || !savedInstanceState.containsKey("user")) {
            user = gson.fromJson(SP.getString("user", null), User.class);
        } else {
            user = (User) savedInstanceState.getSerializable("user");
        }
        return user;
    }

    private void checkIfUserExists(Bundle savedInstanceState, final boolean isLogin){
        if(isLogin){
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            User user = getQuickUser(savedInstanceState);
            if(user != null && !TextUtils.isEmpty(user.getUid())){
                ref.getRoot().child("users/" + user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            openActivity(MainActivity.class);
                        }else{
                            openActivity(Login.class);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        openActivity(Login.class);
                    }
                });
            }else{
                openActivity(Login.class);
            }
        }else{
            openActivity(Login.class);
        }
    }

    private void openActivity(final Class forwardActivity){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashScreen.this, forwardActivity);
                SplashScreen.this.startActivity(mainIntent);
                SplashScreen.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

}
