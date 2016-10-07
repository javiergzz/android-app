package com.grahm.livepost.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.grahm.livepost.R;
import com.grahm.livepost.activities.SplashScreen;
import com.grahm.livepost.objects.User;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Utilities {
    public static final int MSG_TYPE_TEXT = 0;
    public static final int MSG_TYPE_IMAGE = 1;
    public static final int MSG_TYPE_VIDEO = 2;
    public static String[] userLettersPalette = {"300009", "e50019", "c5c4b6", "645f69", "2a3a59"};
    public static User mUser;

    public static boolean isOnline(Context ctx) {
        ConnectivityManager connManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return mWifi.isConnected() || mMobile.isConnected();
    }

    public static boolean isNullOrEmpty(String str) {
        return (str == null || str.trim().length() == 0);
    }

    public static String trimProfilePic(User user) {
        if (user == null) return "";
        String input = user.getProfile_picture();
        if (TextUtils.isEmpty(input)) {
            return null;
        }
        String[] parts = input.split("\\?");
        return parts[0];
    }

    public static String getProfilePic(User user) {
        String input = user.getProfile_picture();
        if (TextUtils.isEmpty(input)) {
            String username = user.getName();
            int index = username.charAt(0);
            String url = "http://dummyimage.com/100x100/" + userLettersPalette[index % 5] + "/ffffff.png&text=" + username.charAt(0);
            return url;
        }
        String[] parts = input.split("\\?");
        return parts[0];
    }


    public static void saveUserOnFirebase(User _user) {
        Long tsLong = System.currentTimeMillis() / 1000;
        String ts = tsLong.toString();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users");
        DatabaseReference usersRef = ref.child(_user.getUid());
        Map<String, Object> user = new HashMap<String, Object>();
        user.put("email", _user.getEmail());
        user.put("name", _user.getName());
        user.put("profile_picture", _user.getProfile_picture());
        user.put("timestamp", ts);
        user.put("uid", _user.getUid());
        usersRef.updateChildren(user);
    }

    public static void saveTwitterOnFirebase(User _user) {
        Long tsLong = System.currentTimeMillis() / 1000;
        String ts = tsLong.toString();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users");
        DatabaseReference usersRef = ref.child(_user.getUid());
        Map<String, Object> user = new HashMap<String, Object>();
        user.put("name", _user.getEmail());
        user.put("profile_picture", _user.getProfile_picture());
        user.put("timestamp", ts);
        user.put("uid", _user.getUid());
        user.put("twitter", _user.getTwitter());
        usersRef.updateChildren(user);
    }

    public static User getUser(DatabaseReference mFirebaseRef, Context ctx, Bundle savedInstanceState) {
        final Gson gson = new Gson();
        final SharedPreferences SP = ctx.getSharedPreferences(ctx.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        if (savedInstanceState == null || !savedInstanceState.containsKey("user")) {
            mUser = gson.fromJson(SP.getString("user", null), User.class);
        } else {
            mUser = (User) savedInstanceState.getSerializable("user");
        }
        mFirebaseRef = mFirebaseRef == null ? FirebaseDatabase.getInstance().getReference() : mFirebaseRef;
        FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser == null) {
            if (mUser == null) {
                return null;
            }//Nonexisting User
            mFirebaseRef.getRoot().child("users/" + mUser.getUserKey()).addValueEventListener(new ValueEventListener() {
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

        } else {
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

    public static String getTimeMsg(Long t) {
        // TODO validate date
        // return new SimpleDateFormat("hh:mma MM/dd/yyyy").format(t);
        //return new SimpleDateFormat("hh:mma").format(t);
        return (String) DateUtils.getRelativeTimeSpanString(t);
    }

    public static int deduceMessageType(String messageString) {
        if(messageString== null) return MSG_TYPE_TEXT;
        String mimeString = Util.getMimeTypeFromUrl(messageString);
        if (mimeString.contains("image")) return MSG_TYPE_IMAGE;
        if (mimeString.contains("video")) return MSG_TYPE_VIDEO;
        return MSG_TYPE_TEXT;
    }
    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static String replaceExtension(String srcUrl, String newExtension)
    {
        int extdotIndex = srcUrl.lastIndexOf(".");
        return srcUrl.substring(0,extdotIndex)+newExtension;
    }
    public static User readUser(Context ctx) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return null;
        }
        final SharedPreferences sharedPreferences = ctx.getSharedPreferences(ctx.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        final Gson gson = new Gson();
        String s = sharedPreferences.getString("user", "");
        //User is authenticated but not in shared preferences
        if (s == "") {
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
        } else {
            return s != "" ? gson.fromJson(s, User.class) : null;
        }
    }

    public static String cleanUrl(String url) {
        String[] parts = url.split("\\?");
        return parts[0];
    }

    public static String cleanVideoUrl(String url) {
        String[] parts = url.split("\\?");
        //https://s3.amazonaws.com/livepostrocks/videos/Art%20Swagger1475559024.mp4_800x399
        String noargs = parts[0];
        int dimindex = noargs.lastIndexOf("_");
        return dimindex > 0 ? noargs.substring(0, dimindex - 1) : noargs;
    }

    public static Bitmap retriveVideoFrameFromVideo(String videoPath)
            throws Throwable {
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            if (Build.VERSION.SDK_INT >= 14)
                mediaMetadataRetriever.setDataSource(videoPath, new HashMap<String, String>());
            else
                mediaMetadataRetriever.setDataSource(videoPath);
            //   mediaMetadataRetriever.setDataSource(videoPath);
            bitmap = mediaMetadataRetriever.getFrameAtTime();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Throwable(
                    "Exception in retriveVideoFrameFromVideo(String videoPath)"
                            + e.getMessage());

        } finally {
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release();
            }
        }
        return bitmap;
    }
}
