package com.grahm.livepost.activities;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.MediaController;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.grahm.livepost.R;
import com.grahm.livepost.util.Utilities;

import android.widget.Toast;
import android.widget.VideoView;

import butterknife.BindView;
import butterknife.ButterKnife;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

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
    public static final String VIDEO_URL_KEY ="update";

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private String mVideoUrl;
    @BindView(R.id.video_view)
    public VideoView mVideoView;
    @BindView(R.id.progress_bar)
    public ProgressBar mProgressBar;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(VIDEO_URL_KEY,mVideoUrl);
    }

    private void restoreState(Bundle savedInstanceState){
        Bundle args = savedInstanceState !=null?savedInstanceState:getIntent().getExtras();
        mVideoUrl = args.getString(VIDEO_URL_KEY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        ButterKnife.bind(this);
        restoreState(savedInstanceState);
        try {
            //mVideoView.setBackgroundColor(getResources().getColor(R.color.black));
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(mVideoView);
            mVideoView.setMediaController(mediaController);
            new fetchVideoTask().execute(mVideoUrl);
            //mVideoView.start();
        } catch (Exception e) {
            Log.e(TAG,"Exception:"+e);
            Toast.makeText(this, "Error connecting", Toast.LENGTH_LONG).show();
        }
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "Player", "onCreate");
    }

    class fetchVideoTask extends AsyncTask<String,Integer,Boolean>{
        String mVideoPath;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(!TextUtils.isEmpty(mVideoPath)) {
                Log.d(TAG,"Playing video:"+mVideoPath);
                mVideoView.setVideoPath(mVideoPath);
                mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                        mProgressBar.setVisibility(View.GONE);
                        mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                            @Override
                            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                                mp.start();
                                mProgressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                });
            }
        }


        @Override
        protected Boolean doInBackground(String... params){
            try {
                mVideoPath = getDataSource(Utilities.cleanUrl(mVideoUrl));
                return true;
            }catch (IOException ex) {
                Log.e(TAG, "error: " + ex.getMessage(), ex);
                return false;
            }
        }
        private String getDataSource(String path) throws IOException {
            if (!URLUtil.isNetworkUrl(path)) {
                return path;
            } else {
                URL url = new URL(path);
                URLConnection cn = url.openConnection();
                cn.connect();

                InputStream stream = cn.getInputStream();
                if (stream == null)
                    throw new RuntimeException("stream is null");
                File temp = File.createTempFile("mediaplayertmp", "dat");
                temp.deleteOnExit();
                String tempPath = temp.getAbsolutePath();
                FileOutputStream out = new FileOutputStream(temp);
                byte buf[] = new byte[128];
                do {
                    int numread = stream.read(buf);
                    if (numread <= 0)
                        break;
                    out.write(buf, 0, numread);
                } while (true);
                try {
                    stream.close();
                } catch (IOException ex) {
                    Log.e(TAG, "error: " + ex.getMessage(), ex);
                }
                return tempPath;
            }
        }
    }
}