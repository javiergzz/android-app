package com.grahm.livepost.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;
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
    public static Timestamp getTimestamp(){
        long rawtime = Long.valueOf(ServerValue.TIMESTAMP.get(".sv"));
        Log.d("Utilities","timestamp:"+rawtime);
        return new Timestamp(rawtime);
    }

    public static User getUser(Firebase mFirebaseRef,Activity activity,Bundle savedInstanceState){
        final Gson gson = new Gson();
        final SharedPreferences SP = activity.getSharedPreferences(activity.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        if(savedInstanceState==null){
            mUser = new Gson().fromJson(activity.getSharedPreferences(activity.getString(R.string.preference_file_key), Context.MODE_PRIVATE).getString("user", null), User.class);
            mFirebaseRef.getRoot().child("users/"+mFirebaseRef.getAuth().getAuth().get("email")).addValueEventListener(new ValueEventListener() {
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

}
