package com.grahm.livepost.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.grahm.livepost.R;
import com.grahm.livepost.adapters.ProfilePagerAdapter;
import com.grahm.livepost.asynctask.S3PutObjectTask;
import com.grahm.livepost.fragments.ProfilePictureFragment;
import com.grahm.livepost.interfaces.OnFragmentInteractionListener;
import com.grahm.livepost.interfaces.OnPutImageListener;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.ui.Controls;
import com.grahm.livepost.utils.Config;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.grahm.livepost.fragments.LoginFragment.loginButton;

public class Login extends AppCompatActivity implements OnFragmentInteractionListener {

    private static final String TAG_CLASS = "Login".toUpperCase();
    private static final int NUM_PAGES = 5;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private OnFragmentInteractionListener mListener;
    private static final int TAKE_PICTURE = 1;
    private static final int PHOTO_SELECTED = 0;
    private static final int TWITTER_LOGIN = 140;
    private AmazonS3Client s3Client = new AmazonS3Client(new BasicAWSCredentials(Config.ACCESS_KEY_ID, Config.SECRET_KEY));
    private OnPutImageListener OnPutImageListener = new OnPutImageListener() {
        @Override
        public void onSuccess(String url) {
            mUser.setPicture(url);
            signUpUser();
        }
    };
    private Uri mIimageUri;
    private User mUser = new User();
    private static final int POS_LOGIN_FRAGMENT = 0;
    private static final int POS_LOGIN = 4;
    private boolean mLogin = false;
    private boolean isOnboarding = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mListener = this;
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ProfilePagerAdapter(getSupportFragmentManager(), NUM_PAGES, mListener);
        mPager.setAdapter(mPagerAdapter);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isOnboarding = extras.getBoolean("isOnboarding", false);
        }
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            if(!isOnboarding){
                super.onBackPressed();
            }
        } else {
            if(mLogin){
                mPager.setCurrentItem(POS_LOGIN_FRAGMENT);
            }else{
                mPager.setCurrentItem(mPager.getCurrentItem() - 1);
            }
        }
    }

    public void doSwipe(View v) {
        mPager.setCurrentItem(mPager.getCurrentItem() + 1);
    }

    public void openLogin(View v){
        mLogin = true;
        mPager.setCurrentItem(POS_LOGIN);
    }
    @Override
    public void onFragmentInteraction(User user, boolean signup) {
        mUser.merge(user);
        if(signup){
            uploadPhoto();
        }else{
            mPager.setCurrentItem(mPager.getCurrentItem() + 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG_CLASS, "resultCode: " + requestCode);
        if (resultCode == RESULT_OK) {
            Bitmap media;
            switch (requestCode) {
                case PHOTO_SELECTED:
                    try{
                        mIimageUri = data.getData();
                        media = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                        ProfilePictureFragment.setImage(media);
                    }catch(IOException e){
                        Log.e(TAG_CLASS, "Error: " + e);
                    }
                    break;
                case TAKE_PICTURE:
                    mIimageUri = data.getData();
                    Bundle extras = data.getExtras();
                    media = (Bitmap) extras.get("data");
                    ProfilePictureFragment.setImage(media);
                    break;
                case TWITTER_LOGIN:
                    loginButton.onActivityResult(requestCode, resultCode, data);
                    break;
                default:
                    break;

            }
        }
    }

    public void uploadPhoto(){
        String userId = mUser.getEmail().replace("." , "<dot>");
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        String pictureName = userId + "_" + ts;
        Controls.createDialog(Login.this, "Compressing Photo...", false);
        new S3PutObjectTask(Login.this, s3Client, OnPutImageListener, pictureName, false).execute(mIimageUri);
    }

    private void signUpUser(){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://rest-livepost-dev.herokuapp.com/v1/signup";
        Map<String,String> params = new HashMap<String, String>();
        params.put("email",mUser.getEmail().toString().toLowerCase());
        params.put("name",mUser.getName());
        params.put("password", mUser.getPassword());
        params.put("picture",mUser.getPicture());
        JSONObject json = new JSONObject(params);
        JsonObjectRequest request = new JsonObjectRequest(url, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG_CLASS, response.toString());
                Controls.dismissDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Controls.dismissDialog();
            }
        });
        Controls.setDialogMessage("Loading...");
        queue.add(request);
    }
}
