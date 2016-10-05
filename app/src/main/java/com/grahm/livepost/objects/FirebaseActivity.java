package com.grahm.livepost.objects;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.grahm.livepost.R;
import com.grahm.livepost.activities.SplashScreen;
import com.grahm.livepost.util.Utilities;

/**
 * Created by Vyz on 2016-08-29.
 */
public class FirebaseActivity extends AppCompatActivity {
    private static final String TAG = "FirebaseActivity";
    protected FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDB;
    private DatabaseReference mFirebaseRef;
    protected FirebaseAuth mAuth;
    protected FirebaseUser mFirebaseUser;
    protected User mUser;
// ...

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseDB = FirebaseDatabase.getInstance();
        mFirebaseRef = mFirebaseDB.getReference();
        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        checkAuth(savedInstanceState);
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                checkAuth(savedInstanceState);
            }
        };
    }


    protected void checkAuth(Bundle savedInstanceState){
        mFirebaseUser = mAuth.getCurrentUser();
        if (mFirebaseUser != null) {
            // User is signed in
            Log.d(TAG, "onAuthStateChanged:signed_in:" + mFirebaseUser.getUid());
            mUser = Utilities.getUser(mFirebaseRef,FirebaseActivity.this,savedInstanceState);
        } else {
            // User is signed out
            Log.d(TAG, "onAuthStateChanged:signed_out");
            Intent mainIntent = new Intent(FirebaseActivity.this, SplashScreen.class);
            FirebaseActivity.this.startActivity(mainIntent);
            FirebaseActivity.this.finish();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
}
