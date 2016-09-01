package com.grahm.livepost.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.database.ServerValue;
import com.firebase.client.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.grahm.livepost.R;
import com.grahm.livepost.objects.User;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Utilities {
    public static User mUser;
    public static boolean isOnline(Context ctx){
        ConnectivityManager connManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mMobile = connManager .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return mWifi.isConnected() || mMobile.isConnected();
    }

    public static boolean isNullOrEmpty(String str) {
        return (str == null || str.trim().length() == 0);
    }

    public static User getUser(Firebase mFirebaseRef, Context ctx, Bundle savedInstanceState){
        FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mFirebaseUser==null){
            return null;
        }
        final Gson gson = new Gson();
        final SharedPreferences SP = ctx.getSharedPreferences(ctx.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        if(savedInstanceState==null){
            mUser = new Gson().fromJson(ctx.getSharedPreferences(ctx.getString(R.string.preference_file_key), Context.MODE_PRIVATE).getString("user", null), User.class);
            mFirebaseRef=mFirebaseRef==null?new Firebase(ctx.getString(R.string.firebase_url)):mFirebaseRef;

            mFirebaseRef.getRoot().child("users/"+mFirebaseUser.getEmail().replace(".","")).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mUser =dataSnapshot.getValue(User.class);
                    SP.edit().putString("user",gson.toJson(mUser,User.class)).commit();
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    mUser = gson.fromJson(SP.getString("user", null), User.class);
                }
            });

        }
        else{
            mUser = (User) savedInstanceState.getSerializable("user");
        }
        return mUser;
    }
    public static String getTimeMsg(Timestamp t){
        return new SimpleDateFormat("MM/dd/yyyy").format(t);
    }

    public static User readUser(Context ctx){
        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            return null;
        }
        final SharedPreferences sharedPreferences = ctx.getSharedPreferences(ctx.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        final Gson gson = new Gson();
        String s = sharedPreferences.getString("user", "");
        //User is authenticated but not in shared preferences
        if(s=="") {
            FirebaseDatabase.getInstance().getReference("users").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    User user = (User) dataSnapshot.getValue();
                    if (user != null) {
                        editor.putString("user", gson.toJson(user));
                        editor.commit();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("ReadUserError", databaseError.getMessage());
                }
            });
            sharedPreferences.getString("user", "");
            return gson.fromJson(s, User.class);
        }else {
            return s != "" ? gson.fromJson(s, User.class) : null;
        }
    }
    public static String cleanUrl(String url){
        String [] parts = url.split("\\?");
        return parts[0];
    }

}
