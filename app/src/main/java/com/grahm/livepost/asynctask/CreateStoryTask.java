package com.grahm.livepost.asynctask;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import com.grahm.livepost.util.AzureUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.gson.Gson;
import com.grahm.livepost.R;
import com.grahm.livepost.interfaces.OnPutImageListener;
import com.grahm.livepost.objects.ImageSize;
import com.grahm.livepost.objects.Story;
import com.grahm.livepost.objects.Update;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.util.GV;
import com.grahm.livepost.util.Util;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class CreateStoryTask extends AsyncTask<Uri, String, String> {
    public static final String TAG = "CreateStoryTask";
    public static final int ASPECT_HEIGHT = -1;
    private ProgressDialog dialog;
    private Context mContext;
    private OnPutImageListener mListener;
    private String mPictureName;
    private Boolean mShowDialog;
    private Story mStory;
    private String mUid;
    private User mUser;
    private String mKey;
    DatabaseReference mFirebaseRef;
    SharedPreferences mSharedPref;

    public CreateStoryTask(Story story, DatabaseReference ref, Context context, OnPutImageListener listener, Boolean showDialog) {
        mUid = null;
        mContext = context;
        mListener = listener;
        mShowDialog = showDialog;
        mStory = story;
        mFirebaseRef = ref;
        mSharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        String s = mSharedPref.getString("user", null);
        if (!TextUtils.isEmpty(s)) {
            mUser = new Gson().fromJson(s, User.class);
            mStory.setAuthor(mUser.getUid());
        }
        Log.e(TAG,"constructor: "+s);
    }

    protected void onPreExecute() {
        dialog = new ProgressDialog(mContext);
        dialog.setMessage(mContext.getString(R.string.ns_creating_text));
        dialog.setCancelable(false);
        if (mShowDialog) {
            dialog.show();
        }
        Log.e(TAG,"onPreExecute: "+mUid);
    }

    protected String doInBackground(Uri... uris) {
        String url = "";
        Log.e(TAG,"doInBackground: "+uris[0]);
        if (uris == null || uris.length < 1) {
            return null;
        }
        // The file location of the image selected.
        Uri selectedImage = uris[0];
        if (selectedImage != null) {
            url = uploadImages(selectedImage);
            mStory.setPosts_picture(url);
            mStory.setLast_message(url);
            addFirebaseEntry();
        }
        return url;
    }

    protected void onPostExecute(String result) {
        if (mShowDialog) {
            dialog.dismiss();
        }
        if (mListener != null) {
            mListener.onSuccess(mKey);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (mShowDialog) {
            dialog.dismiss();
        }
    }

    protected String uploadImages(Uri srcUri) {
        String url = "";
        Resources r = mContext.getResources();
        DisplayMetrics d = r.getDisplayMetrics();
        int thumbSize = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, r.getDimension(R.dimen.thumb_side), d));

        int largeThumbWidth = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, r.getDimension(R.dimen.large_thumb_side), d));
        int largeThumbMaxHeight = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, r.getDimension(R.dimen.max_large_thumb_side_height), d));

        int maxMediumWidth = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, r.getDimension(R.dimen.max_medium_width), d));
        int maxMediumHeight = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, r.getDimension(R.dimen.max_medium_height), d));

        int maxLargeWidth = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, r.getDimension(R.dimen.max_large_width), d));
        int maxLargeHeight = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, r.getDimension(R.dimen.max_large_height), d));
        mPictureName = mUser.getName().trim().toLowerCase() + "_" + mStory.getTitle().toLowerCase() + "_" + System.currentTimeMillis() / 1000L;
        //uploadImage(mPictureName + "_thumb.jpg", srcUri, thumbSize, thumbSize);
        //uploadImage(mPictureName + "_l_thumb.jpg", srcUri, largeThumbWidth, largeThumbMaxHeight);
        uploadImage(mPictureName + "_md.jpg", srcUri, maxMediumWidth, maxMediumHeight);
        url = uploadImage(mPictureName + ".jpg", srcUri, maxLargeWidth, maxLargeHeight);
        return url;
    }

    private Bitmap getScaledBitmap(Uri srcUri, int mDstWidth, int mDstHeight) {
        Bitmap unscaledBitmap = Util.loadBitmapFromUri(mContext,srcUri);

        Bitmap scaledBitmap;
        ImageSize srcSize = new ImageSize(unscaledBitmap.getWidth(), unscaledBitmap.getHeight());
        ImageSize boundarySize = new ImageSize(mDstWidth, mDstHeight);

        //Use Height -1 for width-dependent images to be used on staggered list
        if (unscaledBitmap.getWidth() <= mDstWidth && unscaledBitmap.getHeight() <= mDstHeight)
            return unscaledBitmap;
        else {
            unscaledBitmap.recycle();
            ImageSize s = getScaledDimension(srcSize, boundarySize);
            return Bitmap.createScaledBitmap(Util.loadBitmapFromUri(mContext,srcUri), s.getWidth(), s.getHeight(), false);
        }
    }

    private synchronized void addFirebaseEntry() {

        try {
            DatabaseReference ref = mFirebaseRef.push();
            ref.setValue(mStory);
            mKey = ref.getKey();
            //Set Timestamps
            ref.child("timestamp").setValue(ServerValue.TIMESTAMP);
            //Set Timestamps
            ref.child("last_time").setValue(ServerValue.TIMESTAMP);
//            Log.e(TAG,"FirebaseEntrying: "+ref.toString());
//            DatabaseReference r = mFirebaseRef.getRoot().child("updates/" + mKey).push();
//            r.setValue(new Update(0, null, mStory.getPosts_picture(), mUser.getProfile_picture(), mStory.getAuthor_name(), mStory.getAuthor()));
//            r.child(Story.TIMESTAMP_FIELD_STR).setValue(ServerValue.TIMESTAMP);

            //Add post to "posts created"
            Map<String, Object> posts = new HashMap<String, Object>();
            posts.put(mKey, mStory);
            mFirebaseRef.getRoot().child("users/"+mUser.getUid()).child("/posts_created").updateChildren(posts);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage().toString());
            e.printStackTrace();
        }
    }


    private String uploadImage(String pictureName, Uri srcUri, int mDstWidth, int mDstHeight) {
        String url = "";
        Bitmap scaledBitmap = getScaledBitmap(srcUri, mDstWidth, mDstHeight);
        ContentResolver resolver = mContext.getContentResolver();
        /* Get byte stream */
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        byte[] bitmapdata = bos.toByteArray();
        ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
        try {
            url = AzureUtils.uploadBlob(pictureName, bs, GV.PICTURE_BUCKET);
        }catch (Exception exception) {
            Log.e("AsyncTask", "Error: " + exception);
        }
        return url;

    }

    public static ImageSize getScaledDimension(ImageSize imgSize, ImageSize boundary) {

        int original_width = imgSize.getWidth();
        int original_height = imgSize.getHeight();
        int bound_width = boundary.getWidth();
        int bound_height = boundary.getHeight();
        int new_width = original_width;
        int new_height = original_height;

        // first check if we need to scale width
        if (original_width > bound_width) {
            //scale width to fit
            new_width = bound_width;
            //scale height to maintain aspect ratio
            new_height = (new_width * original_height) / original_width;
        }

        // then check if we need to scale even with the new height
        if (new_height > bound_height) {
            //scale height to fit instead
            new_height = bound_height;
            //scale width to maintain aspect ratio
            new_width = (new_height * original_width) / original_height;
        }

        return new ImageSize(new_width, new_height);
    }
}