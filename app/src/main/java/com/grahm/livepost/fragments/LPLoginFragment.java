package com.grahm.livepost.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.grahm.livepost.R;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.ui.Controls;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LPLoginFragment extends Fragment {

    private static final String TAG_CLASS = "LPLoginFragment";
    private static final int PASS_MIN_LENGTH = 6;
    private EditText txtEmail;
    private EditText txtPassword;
    private User mUser = new User();
    private OnClickListener loginListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!attemptLogin()){
                mUser.setEmail(txtEmail.getText().toString());
                mUser.setPassword((txtPassword.getText().toString()));
                doLogin();
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

    private void doLogin(){
        Controls.createDialog(getActivity(), "Loading...", false);
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = "http://rest-livepost-dev.herokuapp.com/v1/login";
        Map<String,String> params = new HashMap<String, String>();
        params.put("email",mUser.getEmail().toString().toLowerCase());
        params.put("password", mUser.getPassword());
        JSONObject json = new JSONObject(params);
        JsonObjectRequest request = new JsonObjectRequest(url, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG_CLASS, response.toString());
                Controls.dismissDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Controls.dismissDialog();
            }
        });
        Controls.setDialogMessage("Loading...");
        queue.add(request);
    }
}
