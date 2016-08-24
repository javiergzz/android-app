package com.grahm.livepost.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.grahm.livepost.R;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Onboarding extends AppCompatActivity {
    private static final int PROGRESS = 3000;
    private View bodyMessages;
    private ProgressBar spinner;
    private EditText txtStoryTitle;
    private int countMessages = 0;
    private AppCompatActivity activity;
    private String[] nameMessages = {"on_board_1", "on_board_2", "on_board_3", "on_board_4", "on_board_5", "on_board_6", "on_board_7", "on_board_8"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        activity = this;
        bodyMessages  = (View) findViewById(R.id.bodyMessages);
        spinner = new ProgressBar(activity);
        txtStoryTitle = (EditText) findViewById(R.id.txtStoryTitle);
        showLoading();
    }

    private void showLoading(){
        spinner.setLayoutParams(new LinearLayout.LayoutParams(30, 30));
        spinner.setVisibility(View.VISIBLE);
        ((LinearLayout)bodyMessages).addView(spinner);
        hideLoading();
    }

    private void hideLoading(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                createRow();
                ((LinearLayout)bodyMessages).removeView(spinner);
            }
        }, PROGRESS);
    }

    private void createRow(){
        TextView row = new TextView(activity);
        row.setTextColor(Color.rgb(54, 68, 87));
        row.setText(nameMessages[countMessages]);
        ((LinearLayout)bodyMessages).addView(row);
    }
}
