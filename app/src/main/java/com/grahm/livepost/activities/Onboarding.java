package com.grahm.livepost.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.grahm.livepost.R;
import com.grahm.livepost.asynctask.DownloadImageTask;
import com.grahm.livepost.interfaces.OnCallbackImageListener;

public class Onboarding extends AppCompatActivity implements OnCallbackImageListener {
    private static final int PROGRESS = 3000;
    private String[] imgCategories = {"", "breaking_news.png", "business.png", "crime.png", "crisis.png", "entertainment.png", "events.png", "fashion.png", "food.png", "funny.png", "miscellaneous.png", "music.png", "news.png", "politics.png", "religion.png", "sports.png", "technology.png", "travel.png"};
    private View bodyMessages;
    private ProgressBar progressBar;
    private EditText txtStoryTitle;
    private ImageView imgStory;
    private Spinner spinner;
    private AppCompatActivity activity;
    private RelativeLayout viewHeader;
    private LinearLayout viewUser;
    private int countMessages = 0;
    private String[] nameMessages;
    private String storyTitle = "";
    private String storyCategory = "";
    private OnCallbackImageListener imageCallback;
    private Spinner.OnItemSelectedListener selectCategory = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            storyCategory = ((TextView) selectedItemView).getText().toString();
            if(!TextUtils.isEmpty(imgCategories[position])){
                String _url = "http://livepostrocks.s3.amazonaws.com/placeholders/categories/" + imgCategories[position];
                new DownloadImageTask(imgStory, imageCallback).execute(_url);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parentView) {
            // your code here
        }

    };

    private EditText.OnEditorActionListener saveTitle = new EditText.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (!TextUtils.isEmpty((txtStoryTitle.getText().toString()))) {
                    storyTitle = txtStoryTitle.getText().toString();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    txtStoryTitle.setEnabled(false);
                    showLoading();
                } else {
                    new AlertDialog.Builder(activity)
                        .setTitle("Alert")
                        .setMessage("Hey your story needs a title! \uD83D\uDE1F")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();
                }

                return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        activity = this;
        imageCallback = this;
        bodyMessages = (View) findViewById(R.id.bodyMessages);
        progressBar = new ProgressBar(activity);
        txtStoryTitle = (EditText) findViewById(R.id.txtStoryTitle);
        imgStory = (ImageView) findViewById(R.id.img_story);
        viewHeader = (RelativeLayout) findViewById(R.id.viewHeader);
        viewUser = (LinearLayout) findViewById(R.id.viewUser);
        Resources res = getResources();
        nameMessages = res.getStringArray(R.array.on_board_messages);
        txtStoryTitle.setOnEditorActionListener(saveTitle);
        loadSpinnerCategories();
        showLoading();
    }

    private void evalCountMessage() {
        switch (countMessages) {
            case 0:
                showLoading();
                break;
            case 1:
                txtStoryTitle.setEnabled(true);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                break;
            case 2:
                showSpinnerCategories();
                break;
            case 3:
                showLoading();
                break;
            case 4:
                viewUser.setVisibility(View.VISIBLE);
                break;
            case 5:
                break;
            case 6:
                break;
            case 7:
                break;
        }
    }

    private void showLoading() {
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(30, 30));
        progressBar.setTop(5);
        progressBar.setVisibility(View.VISIBLE);
        ((LinearLayout) bodyMessages).addView(progressBar);
        hideLoading();
    }

    private void hideLoading() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                createRow();
                ((LinearLayout) bodyMessages).removeView(progressBar);
                evalCountMessage();
                countMessages++;
            }
        }, PROGRESS);
    }

    private void createRow() {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 0, 0, 15);
        TextView message = new TextView(activity);
        message.setTextColor(Color.rgb(54, 68, 87));
        message.setText(nameMessages[countMessages]);
        row.addView(message);
//        TextView author = new TextView(activity);
//        author.setTextColor(Color.rgb(218, 218, 218));
//        author.setText("Frank the bot");
//        row.addView(author);
        ((LinearLayout) bodyMessages).addView(row);
        row = null;
        message = null;
//        author = null;
    }

    private void loadSpinnerCategories(){
        String[] categories = {"Pick a Category", "Breaking News", "Business", "Crime", "Crisis", "Entertainment", "Events", "Fashion", "Food", "Funny", "Miscellaneous", "Music", "News", "Politics", "Religion", "Sports", "Technology", "Travel"};
        spinner = (Spinner) activity.findViewById(R.id.spinnerCategories);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, categories);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setOnItemSelectedListener(selectCategory);
    }

    private void showSpinnerCategories(){
        spinner.setVisibility(View.VISIBLE);
    }

    @Override
    public void callback(Bitmap result) {
        txtStoryTitle.setTextColor(Color.WHITE);
        viewHeader.setBackgroundColor(Color.rgb(54, 68, 87));
        spinner.setEnabled(false);
        showLoading();
    }
}
