package com.grahm.livepost.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.grahm.livepost.R;
import com.grahm.livepost.adapters.ProfilePagerAdapter;
import com.grahm.livepost.asynctask.RegisterUserTask;
import com.grahm.livepost.interfaces.OnFragmentInteractionListener;
import com.grahm.livepost.interfaces.OnPutImageListener;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.ui.Controls;
import com.grahm.livepost.util.Utilities;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static com.grahm.livepost.fragments.LoginFragment.loginButton;

public class Login extends AppCompatActivity implements OnFragmentInteractionListener {

    private static final String TAG_CLASS = "Login".toUpperCase();
    public static final int LOGIN_FRAGMENT_IDX = 0;
    public static final int NAME_FRAGMENT_IDX = 1;
    public static final int PROFILE_FRAGMENT_IDX = 2;
    public static final int SIGNUP_FRAGMENT_IDX = 3;
    public static final int LOGIN_LP_FRAGMENT_IDX = 4;
    private static final int NUM_PAGES = 5;
    @BindView(R.id.pager_signup) public  ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private OnFragmentInteractionListener mListener;
    private static final int TAKE_PICTURE = 1;
    private static final int PHOTO_SELECTED = 0;
    private static final int TWITTER_LOGIN = 140;
    private OnPutImageListener OnPutImageListener = new OnPutImageListener() {
        @Override
        public void onSuccess(String url) {
            mUser.setProfile_picture(url);
            Intent mainIntent = new Intent(Login.this, MainActivity.class);
            startActivity(mainIntent);
            Login.this.finish();
        }
    };
    public static Uri mIimageUri;
    private User mUser = new User();
    private String mPassword;
    private DatabaseReference mFirebaseRef;
    private RegisterUserTask mAuthTask = null;
    private boolean mLogin = false;
    FirebaseAuth mAuth;
    private String mUid;
    private AppCompatActivity mAvtivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mAvtivity = this;
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseRef = FirebaseDatabase.getInstance().getReference();
        mUser = Utilities.getUser(mFirebaseRef, this, savedInstanceState);
        mUser = mUser == null ? new User() : mUser;
        mListener = this;
        mPagerAdapter = new ProfilePagerAdapter(getSupportFragmentManager(), NUM_PAGES, mListener);
        mPager.setAdapter(mPagerAdapter);

    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            if (mLogin) {
                mLogin = false;
                mPager.setCurrentItem(LOGIN_FRAGMENT_IDX, false);
            } else {
                mPager.setCurrentItem(mPager.getCurrentItem() - 1);
            }
        }
    }

    private void saveUserOnProperties() {
        SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit();
        //Write user data to shared preferences
        Gson gson = new Gson();
        String json = gson.toJson(mUser);
        editor.putString("user", json);
        editor.putString("uid", mUid);
        editor.putString("username", mUser.getEmail());
        editor.putBoolean(SplashScreen.PREFS_LOGIN, true);
        editor.putString(SplashScreen.PREFS_AUTH, SplashScreen.PREFS_LIVEPOST);
        editor.commit();
        mUser.setUid(mUid);
        Utilities.saveUserOnFirebase(mUser);
        Intent mainIntent = new Intent(Login.this, MainActivity.class);
        startActivity(mainIntent);
        Login.this.finish();
    }

    //Done button callback
    public void doSwipe(View v) {
        mPager.setCurrentItem(mPager.getCurrentItem() + 1);
    }

    public void openLogin(View v) {
        mLogin = true;
        mPager.setCurrentItem(LOGIN_LP_FRAGMENT_IDX, false);
    }

    @Override
    public void onFragmentInteraction(int id, Bundle args) {
        boolean signup = args.getBoolean("signup", false);
        if (args.containsKey("password")) mPassword = args.getString("password", "");
        User user = (User) args.get("user");
        mUser.merge(user);
        if (signup) {
            signUpUser();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() + 1);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG_CLASS, "resultCode: " + requestCode);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case TWITTER_LOGIN:
                    loginButton.onActivityResult(requestCode, resultCode, data);
                    break;
                default:
                    break;

            }
        }

        EasyImage.handleActivityResult(requestCode, resultCode, data, Login.this, new DefaultCallback() {
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
    }

    private void signUpUser() {
        new RegisterUserTask(mUser, mPassword, mFirebaseRef, mAuth, mAvtivity, OnPutImageListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR ,mIimageUri);
    }

}