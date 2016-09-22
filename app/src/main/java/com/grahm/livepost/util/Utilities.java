package com.grahm.livepost.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.grahm.livepost.R;
import com.grahm.livepost.objects.User;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Utilities {
    public static final int MSG_TYPE_TEXT = 0;
    public static final int MSG_TYPE_IMAGE = 1;
    public static final int MSG_TYPE_VIDEO = 2;
    public static String [] userLettersPalette = {"300009","e50019","c5c4b6","645f69","2a3a59"};
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
    public static String trimProfilePic(User user){
        if(user == null) return "";
        String input = user.getProfile_picture();
        if(TextUtils.isEmpty(input)){
            return null;
        }
        String[] parts = input.split("\\?");
        return parts[0];
    }
    public static String getProfilePic(User user){
        String input = user.getProfile_picture();
        if(TextUtils.isEmpty(input)){
            String username = user.getName();
            int index = username.charAt(0);
            String url = "http://dummyimage.com/100x100/"+userLettersPalette[index%5]+"/ffffff.png&text="+username.charAt(0);
            return url;
        }
        String[] parts = input.split("\\?");
        return parts[0];
    }

    public static User getUser(DatabaseReference mFirebaseRef, Context ctx, Bundle savedInstanceState){
        final Gson gson = new Gson();
        final SharedPreferences SP = ctx.getSharedPreferences(ctx.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        if(savedInstanceState==null || !savedInstanceState.containsKey("user")) {
            mUser = gson.fromJson(SP.getString("user", null), User.class);
        }else{
            mUser = (User) savedInstanceState.getSerializable("user");
        }
        mFirebaseRef = mFirebaseRef == null ? FirebaseDatabase.getInstance().getReference() : mFirebaseRef;
        FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mFirebaseUser==null){
            if(mUser==null){return null;}//Nonexisting User
            mFirebaseRef.getRoot().child("users/" + mUser.getAuthorString()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mUser = dataSnapshot.getValue(User.class);
                    SP.edit().putString("user", gson.toJson(mUser, User.class)).commit();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    mUser = gson.fromJson(SP.getString("user", null), User.class);
                }
            });
            //Twitter auth

        }else {
            //Firebase auth
            mFirebaseRef.getRoot().child("users/" + mFirebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mUser = dataSnapshot.getValue(User.class);
                    SP.edit().putString("user", gson.toJson(mUser, User.class)).commit();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    mUser = gson.fromJson(SP.getString("user", null), User.class);
                }
            });


        }
        return mUser;
    }
    public static String getTimeMsg(Timestamp t){
        return new SimpleDateFormat("hh:mma MM/dd/yyyy").format(t);
    }

    public static int deduceMessageType(String messageString){
        if(messageString.contains(".png")||messageString.contains(".jpg"))return MSG_TYPE_IMAGE;
        if(messageString.contains(".mp4")) return MSG_TYPE_VIDEO;
        return MSG_TYPE_TEXT;
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
