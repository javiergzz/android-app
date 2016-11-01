package com.grahm.livepost.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.grahm.livepost.R;
import com.grahm.livepost.activities.MainActivity;
import com.grahm.livepost.activities.SplashScreen;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.ui.Controls;

public class LPLoginFragment extends Fragment {

    private UserLoginTask mAuthTask = null;
    private FirebaseDatabase mFirebaseDB;
    private DatabaseReference mFirebaseRef;
    private FirebaseAuth mAuth = null;
    private static final String TAG_CLASS = "LPLoginFragment";
    private static final int PASS_MIN_LENGTH = 6;
    private EditText txtEmail;
    private EditText txtPassword;
    private String mPassword = "";
    private User mUser = new User();
    private OnClickListener loginListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!attemptLogin()){
                Log.i(TAG_CLASS, txtEmail.getText().toString() + " ::: " +  txtPassword.getText().toString());
                mAuthTask = new UserLoginTask(txtEmail.getText().toString(), txtPassword.getText().toString());
                mAuthTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR ,(Void) null);
            }
        }
    };

    public LPLoginFragment() {

    }

    public static LPLoginFragment newInstance() {
        return new LPLoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lplogin, container, false);
        txtEmail = (EditText) view.findViewById(R.id.txt_email);
        txtPassword = (EditText) view.findViewById(R.id.txt_password);
        Button btnLogin = (Button) view.findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(loginListener);
        mFirebaseDB = FirebaseDatabase.getInstance();
        mFirebaseRef = mFirebaseDB.getReference();
        mAuth = FirebaseAuth.getInstance();
        return view;
    }

    private boolean attemptLogin(){
        txtEmail.setError(null);
        txtPassword.setError(null);

        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();
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
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
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

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private static final String TAG = "UserLoginTask";

        private String mUid;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        protected void loginAction() {
            mUid = mAuth.getCurrentUser().getUid();
            mFirebaseRef.child("users").child(mUid).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User u = dataSnapshot.getValue(User.class);
                    Gson gson = new Gson();
                    String json = gson.toJson(u);
                    SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("user", json);
                    editor.putString("uid", mUid);
                    editor.putString("username", mEmail);
                    editor.putBoolean(SplashScreen.PREFS_LOGIN, true);
                    editor.putString(SplashScreen.PREFS_AUTH, SplashScreen.PREFS_LIVEPOST);
                    editor.commit();
                    Log.i(TAG, TAG + ": Login successful!");
                    Intent mainIntent = new Intent(getActivity(), MainActivity.class);
                    startActivity(mainIntent);
                    getActivity().finish();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Controls.dismissDialog();
                    Log.e(TAG,databaseError.getMessage());
                }
            });

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Controls.createDialog(getActivity(), "Loading", false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                mAuth.signInWithEmailAndPassword(mEmail, mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Log.i(TAG, "== !task::isSuccessful :) ==");
                            return;
                        }
                        Log.i(TAG, "== task::isSuccessful :) ==");
                        loginAction();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, e.getMessage().toString());
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            Controls.dismissDialog();
            mAuthTask = null;
        }

        @Override
        protected void onCancelled() {
            Controls.dismissDialog();
            mAuthTask = null;
        }
    }

}