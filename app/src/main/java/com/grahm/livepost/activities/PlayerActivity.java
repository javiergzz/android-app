package com.grahm.livepost.activities;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.util.Log;
import android.widget.MediaController;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.github.rtoshiro.view.video.FullscreenVideoLayout;
import com.grahm.livepost.R;
import com.grahm.livepost.objects.Update;
import com.grahm.livepost.util.Utilities;

import butterknife.BindView;
import butterknife.ButterKnife;
import 	android.widget.VideoView;

import java.io.IOException;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class PlayerActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;
    public static final String TAG="PlayerActivity";
    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    public static final String UPDATE_KEY="update";

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private EasyVideoPlayer player;
    private Update mUpdate;
    @BindView(R.id.videoview)
    public FullscreenVideoLayout videoLayout;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(UPDATE_KEY,mUpdate);
    }

    private void restoreState(Bundle savedInstanceState){
        Bundle args = savedInstanceState !=null?savedInstanceState:getIntent().getExtras();
        mUpdate = (Update)args.getSerializable(UPDATE_KEY);
        videoLayout.setActivity(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        ButterKnife.bind(this);
        restoreState(savedInstanceState);

        Uri videoUri = Uri.parse( Utilities.cleanVideoUrl(mUpdate.getMessage()));
        try {
            videoLayout.setVideoURI(videoUri);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
