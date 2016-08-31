package com.grahm.livepost.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.grahm.livepost.R;
import com.grahm.livepost.interfaces.OnFragmentInteractionListener;


public class NameFragment extends Fragment {

    private static final int PHOTO_SELECTED = 1;
    private static boolean mContinue = false;
    private OnFragmentInteractionListener mListener;
    private static ImageView imgProfile;
    private OnClickListener mContinueListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mContinue){
                mListener.onFragmentInteraction();
            }else{
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
        }
    };

    private OnClickListener mImageListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            getActivity().startActivityForResult(intent, PHOTO_SELECTED);
        }
    };

    public static NameFragment newInstance(OnFragmentInteractionListener listener) {
        NameFragment fragment = new NameFragment();
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

}
