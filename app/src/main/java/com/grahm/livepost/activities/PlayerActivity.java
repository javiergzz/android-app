package com.grahm.livepost.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.grahm.livepost.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlayerActivity extends AppCompatActivity implements EasyVideoCallback {
    public static final String TAG = "PlayerActivity";
    public static final String VIDEO_URL_KEY = "update";

    private String mVideoUrl;

    @BindView(R.id.player_video)
    public EasyVideoPlayer mPlayer;

    @BindView(R.id.web_view_player)
    public WebView mWebView;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(VIDEO_URL_KEY, mVideoUrl);
    }

    private void restoreState(Bundle savedInstanceState) {
        Bundle args = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
        mVideoUrl = args.getString(VIDEO_URL_KEY);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        ButterKnife.bind(this);
        restoreState(savedInstanceState);
        mPlayer.setCallback(this);
        if (mVideoUrl.contains("https://livepostrocks.s3.amazonaws.com")) {
            mPlayer.setVisibility(View.GONE);
            mWebView.loadUrl(mVideoUrl);
        } else {
            mWebView.setVisibility(View.GONE);
            mPlayer.setSource(Uri.parse(mVideoUrl));
        }
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "Player", "onCreate");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPlayer.pause();
    }

    @Override
    public void onStarted(EasyVideoPlayer player) {

    }

    @Override
    public void onPaused(EasyVideoPlayer player) {

    }

    @Override
    public void onPreparing(EasyVideoPlayer player) {

    }

    @Override
    public void onPrepared(EasyVideoPlayer player) {
        player.start();
    }

    @Override
    public void onBuffering(int percent) {

    }

    @Override
    public void onError(EasyVideoPlayer player, Exception e) {
        Log.e("EasyVideoPlayer", e.getMessage());
    }

    @Override
    public void onCompletion(EasyVideoPlayer player) {

    }

    @Override
    public void onRetry(EasyVideoPlayer player, Uri source) {

    }

    @Override
    public void onSubmit(EasyVideoPlayer player, Uri source) {

    }

}