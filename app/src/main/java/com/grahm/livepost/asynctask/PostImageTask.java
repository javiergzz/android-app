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
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.grahm.livepost.interfaces.OnPutImageListener;
import com.grahm.livepost.R;
import com.grahm.livepost.objects.Update;
import com.grahm.livepost.objects.Story;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.util.GV;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Date;

public class PostImageTask extends AsyncTask<Uri, String, String> {
    public static final String TAG = "CreateSessionTask";
    public static final int ASPECT_HEIGHT = -1;
    private ProgressDialog dialog;
    private Context mContext;
    private AmazonS3Client mS3Client;
    private OnPutImageListener mListener;
    private String mPictureName;
    private ImageLoader mImageLoader;
    private Boolean mShowDialog;
    private String mUid;
    private User mUser;
    SharedPreferences mSharedPref;

    public PostImageTask(Context context, AmazonS3Client client, OnPutImageListener listener, Boolean showDialog) {
        mUid = null;
        mContext = context;
        mS3Client = client;
        mListener = listener;
        mShowDialog = showDialog;
        mImageLoader = ImageLoader.getInstance();
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
        String url = "";
        Resources r = mContext.getResources();
        DisplayMetrics d = r.getDisplayMetrics();


        int maxMediumWidth = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, r.getDimension(R.dimen.max_medium_width), d));
        int maxMediumHeight = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, r.getDimension(R.dimen.max_medium_height), d));
        long l = System.currentTimeMillis()/1000L;
        mPictureName = mUser.getName()+l;
        url = uploadImage(mPictureName + ".jpg", srcUri, maxMediumWidth, maxMediumHeight);
        return url;
    }

    private Bitmap getScaledBitmap(Uri srcUri, int mDstWidth, int mDstHeight) {
        Bitmap unscaledBitmap = mImageLoader.loadImageSync(srcUri.toString());
        Bitmap scaledBitmap;
        ImageSize srcSize = new ImageSize(unscaledBitmap.getWidth(), unscaledBitmap.getHeight());
        ImageSize boundarySize = new ImageSize(mDstWidth, mDstHeight);

        //Use Height -1 for width-dependent images to be used on staggered list
        if (unscaledBitmap.getWidth() <= mDstWidth && unscaledBitmap.getHeight() <= mDstHeight)
            return unscaledBitmap;
        else {
            unscaledBitmap.recycle();
            return mImageLoader.loadImageSync(srcUri.toString(), getScaledDimension(srcSize, boundarySize));

        }
    }


    private String uploadImage(String pictureName, Uri srcUri, int mDstWidth, int mDstHeight) {
        String url = "";
        Bitmap scaledBitmap = getScaledBitmap(srcUri, mDstWidth, mDstHeight);
        ContentResolver resolver = mContext.getContentResolver();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(resolver.getType(srcUri));
        /* Get byte stream */
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        //scaledBitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        byte[] bitmapdata = bos.toByteArray();
        ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);

        metadata.setContentLength(bos.size());
        try {
            PutObjectRequest por = new PutObjectRequest(GV.PICTURE_BUCKET, pictureName, bs, metadata).withCannedAcl(CannedAccessControlList.PublicRead);
            mS3Client.putObject(por);
            ResponseHeaderOverrides override = new ResponseHeaderOverrides();
            override.setContentType("image/jpeg");
            GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(GV.PICTURE_BUCKET, pictureName);
            urlRequest.setExpiration(new Date(System.currentTimeMillis() + 3600000));
            urlRequest.setResponseHeaders(override);
            URL urlUri = mS3Client.generatePresignedUrl(urlRequest);
            Uri.parse(urlUri.toURI().toString());
            url = urlUri.toString();
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