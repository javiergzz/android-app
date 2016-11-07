package com.grahm.livepost.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LPLoginFragment extends Fragment {

    private UserLoginTask mAuthTask = null;
    private FirebaseDatabase mFirebaseDB;
    private DatabaseReference mFirebaseRef;
    private FirebaseAuth mAuth = null;
    private static final String TAG_CLASS = "LPLoginFragment";
    private static final int PASS_MIN_LENGTH = 6;
    @BindView(R.id.txt_email)
    public EditText txtEmail;
    @BindView(R.id.txt_password)
    public EditText txtPassword;
    @BindView(R.id.btn_login)
    public Button mBtnLogin;
    @BindView(R.id.txt_header)
    public TextView mTxtHeader;
    @BindView(R.id.view_password)
    public TextInputLayout mViewPassword;
    private String mPassword = "";
    private User mUser = new User();

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
        ButterKnife.bind(this, view);
        mFirebaseDB = FirebaseDatabase.getInstance();
        mFirebaseRef = mFirebaseDB.getReference();
        mAuth = FirebaseAuth.getInstance();
        resetLayout();
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
            focusView = txtEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            txtEmail.setError("This email address is invalid");
            focusView = txtEmail;
            cancel = true;
        }

        // Check for a valid password
        if (TextUtils.isEmpty(password)) {
            txtPassword.setError("This field is required");
            focusView = txtPassword;
            cancel = true;
        } else if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
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

    private boolean hasEmail(){
        txtEmail.setError(null);

        String email = txtEmail.getText().toString();
        boolean next = true;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            txtEmail.setError("This field is required");
            next = false;
        } else if (!isEmailValid(email)) {
            txtEmail.setError("This email address is invalid");
            next = false;
        }

        if (next) {
            txtEmail.requestFocus();
        }
        return next;
    }

    @OnClick(R.id.btn_forgot_password)
    public void forgotPassword(View v){
        v.setVisibility(View.GONE);
        mViewPassword.setVisibility(View.GONE);
        txtPassword.setVisibility(View.GONE);
        mBtnLogin.setTag("1");
        mBtnLogin.setText("Send Email");
        mTxtHeader.setText("Type down your email so we can send you a temporary password");
    }

    public void resetLayout(){
        mViewPassword.setVisibility(View.VISIBLE);
        txtPassword.setVisibility(View.VISIBLE);
        mBtnLogin.setTag("0");
        mBtnLogin.setText("Login");
        mTxtHeader.setText("Login");
    }

    @OnClick(R.id.btn_login)
    public void login(View v) {
        if(v.getTag() == "0"){
            if(!attemptLogin()){
                Log.i(TAG_CLASS, txtEmail.getText().toString() + " ::: " +  txtPassword.getText().toString());
                mAuthTask = new UserLoginTask(txtEmail.getText().toString(), txtPassword.getText().toString(), getActivity());
                mAuthTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR ,(Void) null);
            }
        }else{
            sendEmail();
        }
    }

    public void sendEmail(){
        if(hasEmail()){
            Controls.createDialog(getActivity(), "Sending email.", false);
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.sendPasswordResetEmail(txtEmail.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Controls.dismissDialog();
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(),"Email sent", Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() > PASS_MIN_LENGTH;
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private static final String TAG = "UserLoginTask";
        private Context mContext;

        private String mUid;

        UserLoginTask(String email, String password, Context context) {
            mEmail = email;
            mPassword = password;
            mContext = context;
        }

        protected void loginAction() {
            mUid = mAuth.getCurrentUser().getUid();
            mFirebaseRef.child("users").child(mUid).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User u = dataSnapshot.getValue(User.class);
                    Gson gson = new Gson();
                    String json = gson.toJson(u);
                    SharedPreferences sharedPref = mContext.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("user", json);
                    editor.putString("uid", mUid);
                    editor.putString("username", mEmail);
                    editor.putBoolean(SplashScreen.PREFS_LOGIN, true);
                    editor.putString(SplashScreen.PREFS_AUTH, SplashScreen.PREFS_LIVEPOST);
                    editor.commit();
                    Log.i(TAG, TAG + ": Login successful!");
                    Intent mainIntent = new Intent(getActivity(), MainActivity.class);
                    Controls.dismissDialog();
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
            Controls.createDialog(mContext, "Loading", false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                mAuth.signInWithEmailAndPassword(mEmail, mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Log.i(TAG, "== !task::isSuccessful :) ==");
                            Controls.dismissDialog();
                            Toast.makeText(mContext, task.getException().getMessage(), Toast.LENGTH_LONG).show();
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
            mAuthTask = null;
        }

        @Override
        protected void onCancelled() {
            Controls.dismissDialog();
            mAuthTask = null;
        }
    }

}