package com.grahm.livepost.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.grahm.livepost.R;
import com.grahm.livepost.activities.Login;
import com.grahm.livepost.asynctask.RegisterUserTask;
import com.grahm.livepost.interfaces.OnFragmentInteractionListener;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.util.GV;
import com.grahm.livepost.util.Utilities;


public class SignUpFragment extends Fragment {

    private static final int PASS_MIN_LENGTH = 6;
    private RegisterUserTask mAuthTask = null;
    private OnFragmentInteractionListener mListener;
    private EditText txtEmail;
    private EditText txtPassword;
    private String mPassword;
    private DatabaseReference mFirebaseRef;

    private User mUser = new User();
    private static final boolean signup = true;
    private OnClickListener doneListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mPassword = txtPassword.getText().toString();
            if(!attemptLogin()){
                mUser.setEmail(txtEmail.getText().toString());
                Bundle args = new Bundle();
                args.putSerializable("user",mUser);
                args.putBoolean("signup",signup);
                args.putString("password",mPassword);
                mListener.onFragmentInteraction(Login.SIGNUP_FRAGMENT_IDX, args);
            }
        }
    };

    public static SignUpFragment newInstance(OnFragmentInteractionListener listener) {
        SignUpFragment fragment = new SignUpFragment();
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(OnFragmentInteractionListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mFirebaseRef = FirebaseDatabase.getInstance().getReference();
        mUser = Utilities.getUser(mFirebaseRef,getActivity(),savedInstanceState);
        mUser = mUser==null?new User():mUser;
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        Button btnDone = (Button) view.findViewById(R.id.btn_done);
        txtEmail = (EditText) view.findViewById(R.id.txt_email);
        txtPassword = (EditText) view.findViewById(R.id.txt_password);
        btnDone.setOnClickListener(doneListener);
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

    private boolean attemptLogin(){
        txtEmail.setError(null);
        txtPassword.setError(null);

        String email = txtEmail.getText().toString();
        mPassword = txtPassword.getText().toString();
        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            txtEmail.setError("This field is required");
        } else if (!isEmailValid(email)) {
            txtEmail.setError("This email address is invalid");
            focusView = txtEmail;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(mPassword) && !isPasswordValid(mPassword)) {
            txtPassword.setError("This password is too short");
            focusView = txtPassword;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        }
        return cancel;
    }

    private boolean isEmailValid(String email) {
        if (email == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() > PASS_MIN_LENGTH;
    }
}
