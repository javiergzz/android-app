package com.grahm.livepost.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.grahm.livepost.R;
import com.grahm.livepost.activities.Login;
import com.grahm.livepost.interfaces.OnFragmentInteractionListener;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.ui.Controls;
import com.grahm.livepost.util.Utilities;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static com.grahm.livepost.fragments.LoginFragment.loginButton;


public class ProfilePictureFragment extends Fragment {

    private static final String TAG_CLASS = "ProfilePictureFragment";
    private static boolean mContinue = false;
    private OnFragmentInteractionListener mListener;
    @BindView(R.id.img_profile)
    public ImageView imgProfile;
    private static final boolean signup = false;
    private User mUser = new User();

    public static ProfilePictureFragment newInstance(OnFragmentInteractionListener listener) {
        ProfilePictureFragment fragment = new ProfilePictureFragment();
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(OnFragmentInteractionListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_picture, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.btn_continue)
    public void continueListener(){
        if(mContinue){
            Bundle args = new Bundle();
            args.putSerializable("user",mUser);
            args.putBoolean("signup",signup);
            mListener.onFragmentInteraction(Login.PROFILE_FRAGMENT_IDX, args);
        }else{
            showAlertContinue();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setImage(Uri image){
        Log.i(TAG_CLASS, "setImage");
        mContinue = true;
        Glide.with(this).load(image).asBitmap().centerCrop().into(new BitmapImageViewTarget(imgProfile) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                imgProfile.setImageDrawable(circularBitmapDrawable);
            }
        });
    }

    private void showAlertContinue(){
        new AlertDialog.Builder(getActivity())
            .setTitle("Alert")
            .setMessage("Where's your profile picture? \uD83D\uDE31")
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            })
            .show();
    }

    @OnClick(R.id.img_profile)
    public void showAlertMedia(){
        EasyImage.openChooserWithDocuments(ProfilePictureFragment.this, "Choose a profile picture", 1);
    }

    private void onPhotoReturned(File imageFile) {
        Log.i(TAG_CLASS, "onPhotoReturned");
        Login.mIimageUri = Uri.fromFile(imageFile);
        setImage(Uri.fromFile(imageFile));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.i(TAG_CLASS, "resultCode: " + requestCode);
        EasyImage.handleActivityResult(requestCode, resultCode, data, getActivity(), new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {}
            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                onPhotoReturned(imageFile);
            }
        });
    }

}
