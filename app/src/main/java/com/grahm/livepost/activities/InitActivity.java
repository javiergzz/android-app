package com.grahm.livepost.activities;

import android.app.Activity;
import android.os.Bundle;

import com.grahm.livepost.R;

import butterknife.ButterKnife;

public class InitActivity extends Activity {

    public static final String TAG = "InitActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

}
