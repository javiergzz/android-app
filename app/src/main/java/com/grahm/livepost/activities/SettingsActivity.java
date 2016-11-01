package com.grahm.livepost.activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.grahm.livepost.R;
import com.grahm.livepost.ui.Controls;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;

import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.grahm.livepost.activities.SplashScreen.PREFS_LOGIN;

public class SettingsActivity extends AppCompatActivity{

    private static final String TAG = "SettingsActivity";
    private static final int DISPLAY_LENGTH = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
    }

    public void openContent(View v){
        Uri uri = Uri.parse(v.getTag().toString());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @OnClick(R.id.btn_log_out)
    public void logOut(){

        Controls.createDialog(SettingsActivity.this, "Log Out", false);

        SharedPreferences settings = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        TwitterSession twitterSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
        if (twitterSession != null) {
            Twitter.getSessionManager().clearActiveSession();
            Twitter.logOut();
        }

        editor.putString("user", "");
        editor.putString("uid", "");
        editor.putString("username", "");
        editor.putString(SplashScreen.PREFS_AUTH, "");
        editor.putBoolean(SplashScreen.PREFS_LOGIN, false);
        editor.commit();

        FirebaseAuth.getInstance().signOut();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Controls.dismissDialog();
                Intent mainIntent = new Intent(SettingsActivity.this, Login.class);
                SettingsActivity.this.startActivity(mainIntent);
                SettingsActivity.this.finish();
            }
        }, DISPLAY_LENGTH);
    }

    public void changePassword(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String newPassword = "SOME-SECURE-PASSWORD";
        user.updatePassword(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                        }
                    }
                });

    }
}

