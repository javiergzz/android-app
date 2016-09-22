package com.grahm.livepost.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.grahm.livepost.R;
import com.grahm.livepost.adapters.ProfilePagerAdapter;
import com.grahm.livepost.asynctask.RegisterUserTask;
import com.grahm.livepost.asynctask.S3PutObjectTask;
import com.grahm.livepost.fragments.ProfilePictureFragment;
import com.grahm.livepost.interfaces.OnFragmentInteractionListener;
import com.grahm.livepost.interfaces.OnPutImageListener;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.ui.Controls;
import com.grahm.livepost.util.Utilities;
import com.grahm.livepost.utils.Config;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.grahm.livepost.fragments.LoginFragment.loginButton;

public class Login extends AppCompatActivity implements OnFragmentInteractionListener {

    private static final String TAG_CLASS = "Login".toUpperCase();
    public  static final int LOGIN_FRAGMENT_IDX = 0;
    public  static final int NAME_FRAGMENT_IDX = 1;
    public  static final int PROFILE_FRAGMENT_IDX = 2;
    public  static final int SIGNUP_FRAGMENT_IDX = 3;
    private static final int NUM_PAGES = 4;
    @BindView(R.id.pager_signup) public  ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private OnFragmentInteractionListener mListener;
    private static final int TAKE_PICTURE = 1;
    private static final int PHOTO_SELECTED = 0;
    private static final int TWITTER_LOGIN = 140;
    private AmazonS3Client s3Client = new AmazonS3Client(new BasicAWSCredentials(Config.ACCESS_KEY_ID, Config.SECRET_KEY));
    private OnPutImageListener OnPutImageListener = new OnPutImageListener() {
        @Override
        public void onSuccess(String url) {
            mUser.setProfile_picture(url);
            signUpUser();
        }
    };
    private Uri mIimageUri;
    private User mUser = new User();
    private String mPassword;
    private DatabaseReference mFirebaseRef;
    private RegisterUserTask mAuthTask = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
        mFirebaseRef = FirebaseDatabase.getInstance().getReference();
        mUser = Utilities.getUser(mFirebaseRef,this,savedInstanceState);
        mUser = mUser==null?new User():mUser;
        mListener = this;
        mPagerAdapter = new ProfilePagerAdapter(getSupportFragmentManager(), NUM_PAGES, mListener);
        mPager.setAdapter(mPagerAdapter);
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    //Done button callback
    public void doSwipe(View v) {

            mPager.setCurrentItem(mPager.getCurrentItem() + 1);

    }
    public void doLogin(View v) {
            Intent mainIntent = new Intent(Login.this, LoginActivity.class);
            Login.this.startActivity(mainIntent);
    }

    @Override
    public void onFragmentInteraction(int id, Bundle args) {
        boolean signup = args.getBoolean("signup",false);
        if(args.containsKey("password"))mPassword = args.getString("password","");
        User user = (User)args.get("user");
        mUser.merge(user);
        if(signup){
            attemptRegistration(mIimageUri);
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
        String userId = mUser.getUid();
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        String pictureName = userId + "_" + ts;
        Controls.createDialog(Login.this, "Compressing Photo...", false);
        new S3PutObjectTask(Login.this, s3Client, OnPutImageListener, pictureName, false).execute(mIimageUri);
    }
    private void attemptRegistration(Uri pictureUri) {
        if (mAuthTask != null) {
            return;
        }
        Controls.createDialog(Login.this, getString(R.string.login_compressing_dialog), false);
        mAuthTask = new RegisterUserTask(mUser, mPassword,mFirebaseRef, FirebaseAuth.getInstance(),this,s3Client,OnPutImageListener,true);
        mAuthTask.execute(pictureUri);

    }

    private void signUpUser(){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = getString(R.string.heroku_url);
        Map<String,String> params = new HashMap<String, String>();
        params.put("email",mUser.getEmail().toString().toLowerCase());
        params.put("name",mUser.getName());
        params.put("password", mPassword);
        params.put("picture",mUser.getProfile_picture());
        JSONObject json = new JSONObject(params);
        JsonObjectRequest request = new JsonObjectRequest(url, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG_CLASS, response.toString());
                //If response failed, its string will contain an error code. #TODO this should be improved
                if(!response.toString().contains("code")){
                    Intent mainIntent = new Intent(Login.this, MainActivity.class);
                    Login.this.startActivity(mainIntent);
                    Login.this.finish();
                }
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
