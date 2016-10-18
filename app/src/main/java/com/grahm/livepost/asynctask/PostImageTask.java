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
import java.io.InputStream;

public class PostImageTask extends AsyncTask<Uri, String, String> {
    public static final String TAG = "CreateSessionTask";
    public static final int ASPECT_HEIGHT = -1;
    private ProgressDialog dialog;
    private Context mContext;
    private OnPutImageListener mListener;
    private String mPictureName;
    private Boolean mShowDialog;
    private String mUid;
    private User mUser;
    private String mExtension;
    SharedPreferences mSharedPref;

    public PostImageTask(Context context, OnPutImageListener listener, Boolean showDialog) {
        mUid = null;
        mContext = context;
        mListener = listener;
        mShowDialog = showDialog;
        mSharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        String s = mSharedPref.getString("user", null);
        if (!TextUtils.isEmpty(s)) {
            mUser = new Gson().fromJson(s, User.class);
        }
    }

    protected void onPreExecute() {
        dialog = new ProgressDialog(mContext);
        dialog.setMessage(mContext.getString(R.string.ns_creating_text));
        dialog.setCancelable(false);
        if (mShowDialog) {
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
        if (selectedImage != null) {
            url = uploadImages(selectedImage);
        }
        return url;
    }

    protected void onPostExecute(String result) {
        if (mShowDialog) {
            dialog.dismiss();
            mListener.onSuccess(result);
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
        mExtension = Util.getMimeType(mContext, srcUri);
        String url = "";
        Resources r = mContext.getResources();
        DisplayMetrics d = r.getDisplayMetrics();


        int maxMediumWidth = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, r.getDimension(R.dimen.max_medium_width), d));
        int maxMediumHeight = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, r.getDimension(R.dimen.max_medium_height), d));
        long l = System.currentTimeMillis() / 1000L;
        mPictureName = mUser.getName() + l;

        url = uploadImage(mPictureName + "." + mExtension, srcUri, maxMediumWidth, maxMediumHeight);
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


    private String uploadImage(String pictureName, Uri srcUri, int mDstWidth, int mDstHeight) {
        String url = "";
        ByteArrayInputStream bs;
        ByteArrayOutputStream bos;
        InputStream is = null;
        ContentResolver resolver = mContext.getContentResolver();

        if (!mExtension.contains("gif")) {

            Bitmap scaledBitmap = getScaledBitmap(srcUri, mDstWidth, mDstHeight);
        /* Get byte stream */
            bos = new ByteArrayOutputStream();
            //scaledBitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            byte[] bitmapdata = bos.toByteArray();
            bs = new ByteArrayInputStream(bitmapdata);
            is = bs;
        } else {
            try {
                is = resolver.openInputStream(srcUri);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
        try {
            if (is != null) {
                    url = AzureUtils.uploadBlob(pictureName, is, GV.PICTURE_BUCKET);
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