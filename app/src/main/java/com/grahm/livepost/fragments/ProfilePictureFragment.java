package com.grahm.livepost.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.grahm.livepost.R;
import com.grahm.livepost.activities.Login;
import com.grahm.livepost.interfaces.OnFragmentInteractionListener;
import com.grahm.livepost.objects.User;


public class ProfilePictureFragment extends Fragment {

    private static final int PHOTO_SELECTED = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static boolean mContinue = false;
    private OnFragmentInteractionListener mListener;
    private static ImageView imgProfile;
    private static final boolean signup = false;
    private User mUser = new User();
    private OnClickListener mContinueListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mContinue){
                Bundle args = new Bundle();
                args.putSerializable("user",mUser);
                args.putBoolean("signup",signup);
                mListener.onFragmentInteraction(Login.PROFILE_FRAGMENT_IDX, args);
            }else{
                showAlertContinue();
            }
        }
    };

    private OnClickListener mImageListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            showAlertMedia();
        }
    };

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
        Button btnContinue = (Button) view.findViewById(R.id.btn_continue);
        imgProfile = (ImageView) view.findViewById(R.id.img_profile);
        btnContinue.setOnClickListener(mContinueListener);
        imgProfile.setOnClickListener(mImageListener);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public static void setImage(Bitmap image){
        imgProfile.setImageBitmap(image);
        mContinue = true;
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

    private void showAlertMedia(){
        new AlertDialog.Builder(getActivity())
            .setTitle("Alert")
            .setMessage("Select an option")
            .setPositiveButton("Take a picture", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dispatchTakePictureIntent();
                }
            })
            .setNegativeButton("Select a Media", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    getActivity().startActivityForResult(intent, PHOTO_SELECTED);
                }
            })
            .show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            getActivity().startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

}
