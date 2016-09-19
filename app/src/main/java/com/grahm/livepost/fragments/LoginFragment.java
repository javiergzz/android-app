package com.grahm.livepost.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.TwitterAuthProvider;
import com.grahm.livepost.R;
import com.grahm.livepost.activities.Login;
import com.grahm.livepost.activities.LoginActivity;
import com.grahm.livepost.activities.MainActivity;
import com.grahm.livepost.activities.SplashScreen;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;

import butterknife.OnClick;

public class LoginFragment extends Fragment {

    private static final String TAG_CLASS = "LoginFragment";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    public static TwitterLoginButton loginButton;
    private Callback<TwitterSession> callbackTwitter = new Callback<TwitterSession>() {
        @Override
        public void success(Result<TwitterSession> result) {
            TwitterSession session = result.data;
            String msg = "@" + session.getUserName() + " logged in! (#" + session.getUserId() + ")";
            Toast.makeText(getActivity().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("username", "@" + session.getUserName().toLowerCase());
            editor.putString(SplashScreen.PREFS_AUTH, SplashScreen.PREFS_TWITTER);
            editor.putBoolean(SplashScreen.PREFS_LOGIN, true);
            editor.commit();
            handleTwitterSession(session);
        }
        @Override
        public void failure(TwitterException exception) {
            Log.d("TwitterKit", "Login with Twitter failure", exception);
        }
    };

    SharedPreferences sharedPref;

    public LoginFragment() {

    }

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        loginButton = (TwitterLoginButton) view.findViewById(R.id.btn_login_twitter);
        loginButton.setCallback(callbackTwitter);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG_CLASS, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG_CLASS, "onAuthStateChanged:signed_out");
                }
            }
        };
        sharedPref = getActivity().getSharedPreferences(SplashScreen.PREFS_NAME, Context.MODE_PRIVATE);
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

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void handleTwitterSession(final TwitterSession session) {
        Log.d(TAG_CLASS, "handleTwitterSession:" + session);

        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG_CLASS, "signInWithCredential:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.w(TAG_CLASS, "signInWithCredential", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            getProfilePicture(session);
                        }
                    }
                });
    }

    private void getProfilePicture(TwitterSession session){
        Callback<User> callbackUser = new Callback<User>() {
            @Override
            public void success(Result<User> userResult) {
                String name = userResult.data.name;
                String email = userResult.data.email;
                String photoUrlNormalSize   = userResult.data.profileImageUrl;
                String photoUrlBiggerSize   = userResult.data.profileImageUrl.replace("_normal", "_bigger");
                String photoUrlMiniSize     = userResult.data.profileImageUrl.replace("_normal", "_mini");
                String photoUrlOriginalSize = userResult.data.profileImageUrl.replace("_normal", "");
                Log.i("name", name);
                Log.i("photoUrlNormalSize", photoUrlNormalSize);
                Log.i("photoUrlBiggerSize", photoUrlBiggerSize);
                Log.i("photoUrlMiniSize", photoUrlMiniSize);
                Log.i("photoUrlOriginalSize", photoUrlOriginalSize);

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("tw_picture", photoUrlNormalSize);
                editor.putString("tw_name", name);
                editor.commit();

                Intent mainIntent = new Intent(getActivity(), MainActivity.class);
                startActivity(mainIntent);
                getActivity().finish();
            }

            @Override
            public void failure(TwitterException exc) {
                Log.d("TwitterKit", "Verify Credentials Failure", exc);
            }
        };
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient(session);
        twitterApiClient.getAccountService().verifyCredentials(true, false, callbackUser);
    }

}
