package com.grahm.livepost.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.grahm.livepost.R;
import com.grahm.livepost.interfaces.OnFragmentInteractionListener;
import com.grahm.livepost.objects.User;


public class NameFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private EditText txtName;
    private User mUser = new User();
    private static final boolean signup = false;
    private OnClickListener continueListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String name = txtName.getText().toString();
            if(TextUtils.isEmpty(name)){
                showAlertContinue();
            }else{
                mUser.setName(name);
                mListener.onFragmentInteraction(mUser, signup);
            }
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
        View view = inflater.inflate(R.layout.fragment_name, container, false);
        Button btnContinue = (Button) view.findViewById(R.id.btn_continue);
        txtName = (EditText) view.findViewById(R.id.name);
        btnContinue.setOnClickListener(continueListener);
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

    private void showAlertContinue(){
        new AlertDialog.Builder(getActivity())
                .setTitle("Alert")
                .setMessage("Please write your name \uD83D\uDE4F")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

}
