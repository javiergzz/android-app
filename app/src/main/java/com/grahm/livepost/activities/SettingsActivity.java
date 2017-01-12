package com.grahm.livepost.activities;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.grahm.livepost.R;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.ui.Controls;
import com.grahm.livepost.util.Utilities;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    private static final int DISPLAY_LENGTH = 3000;
    private static final int PASS_MIN_LENGTH = 6;
    private DatabaseReference mFirebaseRef;
    private EditText mOldPassword;
    private EditText mNewPassword;
    private AlertDialog mAlertDialog;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        mFirebaseRef = FirebaseDatabase.getInstance().getReference();
        mUser = Utilities.getUser(mFirebaseRef, SettingsActivity.this, savedInstanceState);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            TextView txtVersion = (TextView) findViewById(R.id.txt_version);
            txtVersion.setText("Version " + pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "Settings", "onCreate");
    }

    public void openContent(View v) {
        Uri uri = Uri.parse(v.getTag().toString());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @OnClick(R.id.btn_log_out)
    public void logOut() {

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
                Intent mainIntent = new Intent(SettingsActivity.this, Login.class);
                startActivity(mainIntent);
                SettingsActivity.this.finish();
                Controls.dismissDialog();
            }
        }, DISPLAY_LENGTH);
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= PASS_MIN_LENGTH;
    }

    private boolean attemptPassword(){

        String oldPassword = mOldPassword.getText().toString();
        String newPassword = mNewPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(oldPassword)) {
            mOldPassword.setError("This field is required");
            focusView = mOldPassword;
            cancel = true;
        } else if (!isPasswordValid(oldPassword)) {
            mOldPassword.setError("This password is too short");
            focusView = mOldPassword;
            cancel = true;
        }

        if (TextUtils.isEmpty(newPassword)) {
            mNewPassword.setError("This field is required");
            focusView = mNewPassword;
            cancel = true;
        } else if (!isPasswordValid(newPassword)) {
            mNewPassword.setError("This password is too short");
            focusView = mNewPassword;
            cancel = true;
        }

        if(cancel){
            focusView.requestFocus();
        }

        return cancel;
    }

    @OnClick(R.id.btn_change_password)
    public void showDialog() {
        LayoutInflater li = LayoutInflater.from(SettingsActivity.this);
        View promptsView = li.inflate(R.layout.prompt_change_password, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                SettingsActivity.this);
        alertDialogBuilder.setView(promptsView);
        mOldPassword = (EditText) promptsView
                .findViewById(R.id.txt_old_password_prompt);
        mNewPassword = (EditText) promptsView
                .findViewById(R.id.txt_password_prompt);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Change",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        mAlertDialog = alertDialogBuilder.create();
        mAlertDialog.show();
        mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!attemptPassword()){
                    Log.i(TAG, "before reauthenticate");
                    reauthenticateUser();
                }
            }
        });
    }

    private void changePassword() {
        Log.i(TAG, "changePassword");
        Controls.createDialog(SettingsActivity.this, "Changing password...", false);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.updatePassword(mNewPassword.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Controls.dismissDialog();
                        if (task.isSuccessful()) {
                            Log.i(TAG, "changePassword isSuccessful");
                            Toast.makeText(SettingsActivity.this, "Changed", Toast.LENGTH_LONG).show();
                            mAlertDialog.cancel();
                        } else {
                            Log.i(TAG, "changePassword error");
                            Toast.makeText(SettingsActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    private void reauthenticateUser() {
        Log.i(TAG, "reauthenticate");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider
                .getCredential(mUser.getEmail(), mOldPassword.getText().toString());
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.i(TAG, "reauthenticate isSuccessful");
                            changePassword();
                        }else{
                            Log.i(TAG, "reauthenticate error");
                            Toast.makeText(SettingsActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}

