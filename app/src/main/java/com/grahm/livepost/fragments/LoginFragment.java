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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.grahm.livepost.R;
import com.grahm.livepost.activities.MainActivity;
import com.grahm.livepost.activities.SplashScreen;
import com.grahm.livepost.ui.Controls;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.grahm.livepost.util.Utilities.saveTwitterOnFirebase;

public class LoginFragment extends Fragment {

    private static final String TAG_CLASS = "LoginFragment";
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDB;
    private DatabaseReference mFirebaseRef;
    private FirebaseAuth.AuthStateListener mAuthListener;
    public static TwitterLoginButton loginButton;
    private Callback<TwitterSession> callbackTwitter = new Callback<TwitterSession>() {
        @Override
        public void success(Result<TwitterSession> result) {
            mLoading.setVisibility(View.VISIBLE);
            handleTwitterSession(result.data);
        }

        @Override
        public void failure(TwitterException exception) {
            Log.d("TwitterKit", "Login with Twitter failure", exception);
        }
    };

    SharedPreferences sharedPref;
    private ProgressBar mLoading;

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
        mLoading = (ProgressBar) view.findViewById(R.id.l_loaging);
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
        mFirebaseDB = FirebaseDatabase.getInstance();
        mFirebaseRef = mFirebaseDB.getReference();
        sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
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

    private void transformUser(final String uid, final TwitterSession session) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://rest-livepost-dev.herokuapp.com/v1.1/twitter/transform";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            Log.d("My App", json.toString());
                            Log.d("phonetype value ", json.getBoolean("success") + "");
                            if (json.getBoolean("success")) {
                                getProfilePicture(session);
                            } else {
                                mLoading.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), (json.getJSONObject("msg")).getString(Locale.getDefault().getDisplayLanguage()), Toast.LENGTH_LONG).show();
                            }
                        } catch (Throwable tx) {
                            Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                            mLoading.setVisibility(View.GONE);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("uid", uid);
                params.put("screen_name", "@" + session.getUserName().toLowerCase());
                return params;
            }
        };
        queue.add(stringRequest);
        queue.start();
    }

    private void twitterActions(final TwitterSession session) {
        final String mUid = mAuth.getCurrentUser().getUid();
        mFirebaseRef.child("users").child(mUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                com.grahm.livepost.objects.User u = dataSnapshot.getValue(com.grahm.livepost.objects.User.class);
                if (u != null && u.isActive()) {
                    saveOnProperties(u);
                    Intent mainIntent = new Intent(getActivity(), MainActivity.class);
                    mLoading.setVisibility(View.GONE);
                    startActivity(mainIntent);
                    getActivity().finish();
                } else {
                    transformUser(mUid, session);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG_CLASS, databaseError.getMessage());
            }
        });

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
                        if (!task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            twitterActions(session);
                        }
                    }
                });
    }

    private void getProfilePicture(final TwitterSession session) {
        Callback<User> callbackUser = new Callback<User>() {
            @Override
            public void success(Result<User> userResult) {
                Log.i("TWITTER", userResult.data.toString());
                String name = userResult.data.name;
                String email = userResult.data.email;
                String photoUrlNormalSize = userResult.data.profileImageUrl;
                com.grahm.livepost.objects.User _user = new com.grahm.livepost.objects.User();
                _user.setEmail(email);
                _user.setName(name);
                _user.setProfile_picture(photoUrlNormalSize);
                _user.setUid(mAuth.getCurrentUser().getUid());
                _user.setTwitter(session.getUserName());
                saveTwitterOnFirebase(_user);
                saveOnProperties(_user);
                mLoading.setVisibility(View.GONE);
                Intent mainIntent = new Intent(getActivity(), MainActivity.class);
                startActivity(mainIntent);
                getActivity().finish();
            }

            @Override
            public void failure(TwitterException exc) {
                mLoading.setVisibility(View.GONE);
                Log.d("TwitterKit", "Verify Credentials Failure", exc);
            }
        };
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient(session);
        twitterApiClient.getAccountService().verifyCredentials(true, false, callbackUser);
    }

    private void saveOnProperties(com.grahm.livepost.objects.User user) {
        SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(user);
        editor.putString("user", json);
        editor.putString("uid", user.getUid());
        editor.putString("username", user.getName());
        editor.putBoolean(SplashScreen.PREFS_LOGIN, true);
        editor.putString(SplashScreen.PREFS_AUTH, SplashScreen.PREFS_LIVEPOST);
        editor.commit();
    }
}
