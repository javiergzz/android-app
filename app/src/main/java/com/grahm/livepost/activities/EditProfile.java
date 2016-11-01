package com.grahm.livepost.activities;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.grahm.livepost.R;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.util.Utilities;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
        if(!TextUtils.isEmpty(mTxtEmail.getText())){
            mFirebaseRef.child("users").child(mUser.getUserKey()).child("email").setValue(mTxtEmail.getText().toString());
        }
        if(!TextUtils.isEmpty(mTxtName.getText())){
            mFirebaseRef.child("users").child(mUser.getUserKey()).child("name").setValue(mTxtName.getText().toString());
        }
    }
}

