package com.grahm.livepost.activities;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.grahm.livepost.R;
import com.grahm.livepost.asynctask.UploadImageTask;
import com.grahm.livepost.interfaces.OnPutImageListener;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.util.Utilities;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class EditProfile extends AppCompatActivity{

    private static final String TAG = "EditProfileActivity";
    private DatabaseReference mFirebaseRef;
    private User mUser;
    @BindView(R.id.img_edit_profile_pic)
    public ImageView mImageView;
    @BindView(R.id.txt_edit_user_name)
    public EditText mTxtName;
    @BindView(R.id.txt_edit_user_email)
    public EditText mTxtEmail;
    private Uri mIimageUri = null;
    private String mImageUrl = null;
    private OnPutImageListener mOnPutImageListener = new OnPutImageListener() {
        @Override
        public void onSuccess(String url) {
            mImageUrl = url;
            saveOnFirebase();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.bind(this);
        mFirebaseRef = FirebaseDatabase.getInstance().getReference();
        mUser = Utilities.getUser(mFirebaseRef, EditProfile.this, savedInstanceState);
        mTxtName.setText(mUser.getName());
        mTxtEmail.setText(mUser.getEmail());
        setProfilePicture();
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "Edit Profile", "onCreate");
    }

    private void setProfilePicture(){
        Glide.with(this).load(Utilities.trimProfilePic(mUser)).asBitmap().centerCrop().into(new BitmapImageViewTarget(mImageView) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                mImageView.setImageDrawable(circularBitmapDrawable);
            }
        });
    }

    @OnClick(R.id.btn_save_user_data)
    public void saveDataUser(){
        if(mIimageUri != null){
            new UploadImageTask(EditProfile.this, mUser.getUserKey() + "_" + System.currentTimeMillis() / 1000L, mOnPutImageListener, true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mIimageUri);
        }else{
            saveOnFirebase();
        }

    }

    public void saveOnFirebase(){
        boolean isValid = true;
        Map<String, Object> user = new HashMap<String, Object>();
        if(!TextUtils.isEmpty(mImageUrl)){
            user.put("profile_picture", mImageUrl);
        }
        if(!TextUtils.isEmpty(mTxtEmail.getText())){
            user.put("email", mTxtEmail.getText().toString());
        }
        if(!TextUtils.isEmpty(mTxtName.getText())){
            user.put("name", mTxtName.getText().toString());
        }
        isValid = !TextUtils.isEmpty(mTxtName.getText()) || !TextUtils.isEmpty(mImageUrl);
        if(isValid){
            if(TextUtils.isEmpty(mTxtEmail.getText())){
                FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
                fUser.updateEmail(mTxtEmail.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User email address updated.");
                                }
                            }
                        });
            }
            mFirebaseRef.child("users").child(mUser.getUserKey()).updateChildren(user, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    Toast.makeText(EditProfile.this, "Data saved!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @OnClick(R.id.img_edit_profile_pic)
    public void selectNewProfilePicture(){
        EasyImage.openChooserWithDocuments(EditProfile.this, "Choose a profile picture", 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, EditProfile.this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {}

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                //Handle the image
                onPhotoReturned(imageFile);
            }
        });
    }

    private void onPhotoReturned(File imageFile) {
        mIimageUri = Uri.fromFile(imageFile);
        Glide.with(this).load(mIimageUri).asBitmap().centerCrop().into(new BitmapImageViewTarget(mImageView) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                mImageView.setImageDrawable(circularBitmapDrawable);
            }
        });
    }
}

