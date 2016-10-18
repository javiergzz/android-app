package com.grahm.livepost.asynctask;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.gson.Gson;
import com.grahm.livepost.interfaces.OnPutImageListener;
import com.grahm.livepost.R;
import com.grahm.livepost.objects.ImageSize;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.util.AzureUtils;
import com.grahm.livepost.util.GV;
import com.grahm.livepost.util.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Date;
import java.util.Map;

public class RegisterUserTask extends AsyncTask<Uri, String, String> {
    public static final String TAG = "RegisterUserTask";
    public static final int ASPECT_HEIGHT = -1;
    private ProgressDialog dialog;
    private Context mContext;
    private OnPutImageListener mListener;
    private String mPictureName;
    private Boolean mShowDialog;
    private User mUser;
    private String mUid;
    private String mPassword;
    DatabaseReference mFirebaseRef;
    FirebaseException mFirebaseError;
    FirebaseAuth mFirebaseAuth;
    SharedPreferences mSharedPref;

    public RegisterUserTask(User user, String password, DatabaseReference ref, FirebaseAuth auth, Context context, OnPutImageListener listener, Boolean showDialog){
        mUid = null;
        mContext = context;
        mListener = listener;
        mShowDialog = showDialog;
        mUser=user;
        mPassword = password;
        mFirebaseRef = ref;
        mFirebaseAuth = auth;
        mSharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        dialog = new ProgressDialog(mContext);

    }
    protected void onPreExecute() {

        dialog.setMessage(mContext.getString(R.string.reg_creating_text));
        dialog.setCancelable(false);
        if(mShowDialog && !dialog.isShowing() && !dialog.isIndeterminate()){
            dialog.show();
        }
    }

    protected String doInBackground(Uri... uris) {
        String url = "";
        if (uris == null || uris.length != 1) {
            return null;
        }
        // The file location of the image selected.
        Uri selectedImage = uris[0];
        if(selectedImage!= null)
            mUser.setProfile_picture(uploadImages(selectedImage));
        Log.d(TAG,"Adding Entry");
        addFirebaseEntry();
        return url;
    }
    protected void onPostExecute(String result) {
        if(!mShowDialog && dialog.isShowing())dialog.dismiss();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if(mShowDialog && dialog.isShowing())dialog.dismiss();
    }
    protected String uploadImages(Uri srcUri){
        String url = "";
        Resources r = mContext.getResources();
        DisplayMetrics d =r.getDisplayMetrics();
        int picSide = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, r.getDimension(R.dimen.profile_pic_w), d));
        mPictureName = "avatar_"+mUser.getName()+ System.currentTimeMillis()/1000L+".jpg";
        url = uploadImage(mPictureName,srcUri,picSide,picSide);
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

    private synchronized void addFirebaseEntry(){
        try {
            mFirebaseAuth.createUserWithEmailAndPassword(mUser.getEmail(), mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful()) {
                        Log.e(TAG,"User creation failed");
                        dialog.setMessage(mContext.getString(R.string.user_reg_failed) +task.getException().getMessage());
                        dialog.cancel();
                        return;
                    }
                    //If user creation was successful, store extra data object
                    mUid = mFirebaseAuth.getCurrentUser().getUid();
                    mUser.setUid(mUid);
                    mFirebaseRef.child("users/"+mUid).setValue(mUser);
                    mFirebaseRef.child("users/"+mUid+"/timestamp").setValue(ServerValue.TIMESTAMP);
                    SharedPreferences.Editor editor = mSharedPref.edit();
                    //Write user data to shared preferences
                    Gson gson = new Gson();
                    String json = gson.toJson(mUser);
                    editor.putString("user", json);
                    editor.putString("uid", mUid);
                    editor.putString("username", mUser.getEmail());
                    editor.commit();
                    Log.i(TAG, "User " + mUser.getEmail() + " was registerd successfully!");
                    if(mShowDialog && dialog.isShowing())dialog.dismiss();
                    if(mListener != null){
                        mListener.onSuccess(mPictureName);
                    }
                }
            }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    private String uploadImage(String pictureName,Uri srcUri,int mDstWidth, int mDstHeight){
        String url = "";
        Bitmap scaledBitmap = getScaledBitmap(srcUri,mDstWidth, mDstHeight);
        ContentResolver resolver = mContext.getContentResolver();
        /* Get byte stream */
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        //scaledBitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        byte[] bitmapdata = bos.toByteArray();
        ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
    try {
        if (bs != null) {
            url = AzureUtils.uploadBlob(pictureName, bs, GV.PICTURE_BUCKET);
        }
    } catch (Exception exception) {
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