package com.grahm.livepost.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.FacebookSdk;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.grahm.livepost.R;
import com.grahm.livepost.adapters.ChatAdapter;
import com.grahm.livepost.asynctask.PostImageTask;
import com.grahm.livepost.asynctask.PostVideoTask;
import com.grahm.livepost.interfaces.OnFragmentInteractionListener;
import com.grahm.livepost.interfaces.OnPutImageListener;
import com.grahm.livepost.objects.FirebaseActivity;
import com.grahm.livepost.objects.Story;
import com.grahm.livepost.objects.Update;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.util.KeyboardUtil;
import com.grahm.livepost.util.Util;
import com.grahm.livepost.util.Utilities;
import com.objectlife.statelayout.StateLayout;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;
import com.twitter.sdk.android.tweetui.TweetUi;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import io.fabric.sdk.android.Fabric;

public class ChatActivity extends FirebaseActivity implements AbsListView.OnItemClickListener, OnFragmentInteractionListener {
    private static final String TAG_CLASS = "ChatActivity";
    public static final String TAG_ID = "key";
    public static final String TAG_USER = "user";
    public static final String TAG_STORY = "story";
    public static final String PREFS_TUTORIAL = "tutorial_chat";

    public static Activity mChat;

    @BindView(R.id.messageInput)
    public EditText mInputText;
    @BindView(R.id.msg_list)
    public RecyclerView mListView;
    @BindView(R.id.main_toolbar)
    public Toolbar mToolbar;
    @BindView(R.id.main_backdrop)
    public ImageView mBackdropImageView;
    @BindView(R.id.btnSend)
    public FloatingActionButton mBtnSend;
    @BindView(R.id.btnBottom)
    public Button mBtnBottom;
    @BindView(R.id.sl_layout_state)
    public StateLayout mStateLayout;
    @BindView(R.id.main_appbar)
    public AppBarLayout mAppBar;

    public static MainActivity.FragmentsEnum page = MainActivity.FragmentsEnum.CHAT;
    private DatabaseReference mFirebaseRef;
    private PostImageTask mPostImageTask;
    private PostVideoTask mPostVideoTask;
    private String mId;
    private Story mStory;
    private LinearLayoutManager mLinearLayoutManager;

    private ChatAdapter mMessagesListAdapter;
    private User mUser;
    private OnPutImageListener putImageListener = new OnPutImageListener() {
        @Override
        public void onSuccess(String url) {
            mFirebaseRef.getRoot().child("posts/" + mId + "/last_message").setValue(url);
            Update m = new Update(0, null, url, mUser.getProfile_picture(), mUser.getName(), mUser.getUserKey());
            // Create a new, auto-generated child of that chat location, and save our chat data there
            DatabaseReference r = mFirebaseRef.push();
            r.setValue(m);
            r.child(Update.TIMESTAMP_FIELD_STR).setValue(ServerValue.TIMESTAMP);
        }
    };

    private static final String TWITTER_KEY = "roDB8OWxSlYv3hiKXYnPusPUJ";
    private static final String TWITTER_SECRET = "hV1sxHw8CcQaFMnKU59F0ze5qFVEcxDrRtQCouf2sXWXoZ300w";

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(TAG_ID, mId);
        outState.putSerializable(TAG_STORY, mStory);
        outState.putSerializable(TAG_USER, mUser);
        getIntent().putExtra(TAG_USER, mUser);
        super.onSaveInstanceState(outState);
    }

    private void restoreState(Bundle savedInstanceState) {
        Bundle args = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
        if (args != null) {
            mId = args.getString(TAG_ID);
            mStory = (Story) args.getSerializable(TAG_STORY);
        }
        mUser = Utilities.getUser(mFirebaseRef, this, args);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreState(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mChat = this;
        ButterKnife.bind(this);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig), new TweetUi(), new Twitter(authConfig), new TweetUi(), new TweetComposer());

        FacebookSdk.sdkInitialize(getApplicationContext());

        setSupportActionBar(mToolbar);
        restoreState(savedInstanceState);
        mFirebaseRef = FirebaseDatabase.getInstance().getReference("updates").child(mId);
        setupMenu();
        mStateLayout.setContentViewResId(R.id.v_content)
                .setErrorViewResId(R.id.v_error)
                .setEmptyViewResId(R.id.v_empty)
                .setLoadingViewResId(R.id.v_loading)
                .initWithState(StateLayout.VIEW_LOADING);

        KeyboardUtil keyboardUtil = new KeyboardUtil(this, findViewById(R.id.chat_container));
        keyboardUtil.enable();
        SharedPreferences settings = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);
//        if (!settings.getBoolean(PREFS_TUTORIAL, false)) {
            loadTutorial();
//        }

        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "Story Screen", "onCreate");
    }

    private void loadTutorial(){
        
    }

    @OnTextChanged(R.id.messageInput)
    void onTextChanged(CharSequence text) {
        mBtnSend.setImageResource(TextUtils.isEmpty(text) ? android.R.drawable.ic_menu_camera : android.R.drawable.ic_menu_send);
    }

    @OnEditorAction(R.id.messageInput)
    public boolean editorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            sendMessage();
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            mLinearLayoutManager = new LinearLayoutManager(this);
            mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mListView.setLayoutManager(mLinearLayoutManager);
            mMessagesListAdapter = new ChatAdapter(mFirebaseRef.limitToLast(50), this, mId, mUser);
            mListView.setAdapter(mMessagesListAdapter);
            mMessagesListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    mListView.scrollToPosition(mMessagesListAdapter.getItemCount() - 1);
                }
            });
            mListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if(mLinearLayoutManager.findLastCompletelyVisibleItemPosition()+3 < mMessagesListAdapter.getItemCount()){
                        if(!mBtnBottom.isShown())mBtnBottom.setVisibility(View.VISIBLE);
                    }else if(mBtnBottom.isShown()){
                        mBtnBottom.setVisibility(View.GONE);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG_CLASS, "Bad adapter:" + e.toString());
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        com.grahm.livepost.util.EasyImage.handleActivityResult(requestCode, resultCode, data, this, new com.grahm.livepost.util.EasyImage.Callbacks() {
            @Override
            public void onImagePickerError(Exception e, com.grahm.livepost.util.EasyImage.ImageSource source, int type) {
                Toast.makeText(ChatActivity.this, getString(R.string.error_picker), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onImagePicked(File file, com.grahm.livepost.util.EasyImage.ImageSource source, int type) {
                String mimeType = Util.getMimeTypeFromUri(getBaseContext(), Uri.fromFile(file));
                if (mimeType.contains("video")) {
                    onVideoReturned(file);
                } else {
                    onPhotoReturned(file);
                }
                Log.e(TAG_CLASS, "imagefile:" + file.getName() + " type:" + type + " requestCode:" + requestCode + " resultCode:" + resultCode);
            }

            @Override
            public void onCanceled(com.grahm.livepost.util.EasyImage.ImageSource source, int type) {

            }
        });
    }

    private void onVideoReturned(File file) {
        Uri uri = Uri.fromFile(file);

        mPostVideoTask = new PostVideoTask(this, putImageListener, true);
        if (uri != null) mPostVideoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,uri);
    }

    private void onPhotoReturned(File imageFile) {
        Uri uri = Uri.fromFile(imageFile);

        mPostImageTask = new PostImageTask(this,  putImageListener, true);
        if (uri != null) mPostImageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,uri);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @OnFocusChange(R.id.messageInput)
    public void collapse(){
        mAppBar.setExpanded(false);
        gotoBottom(null);
    }

    @OnClick(R.id.btnSend)
    public void sendMessage() {
        String input = mInputText.getText().toString();
        mUser = Utilities.getUser(mFirebaseRef, getBaseContext(), getIntent().getExtras());
        if (TextUtils.isEmpty(input)) {
            if(Utilities.isOnline(ChatActivity.this)){
                Log.d(TAG_CLASS, "Choosing Image");
                Long l = System.currentTimeMillis() / 1000L;
                com.grahm.livepost.util.EasyImage.openChooserWithDocuments(this, mStory.getTitle(), 1);
            }else{
                // TODO replace string
                Toast.makeText(ChatActivity.this, "I can’t seem to connect to the internet. Try again later. Sorry!", Toast.LENGTH_LONG).show();
            }
        } else {
            mFirebaseRef.getRoot().child("posts/" + mId + "/last_message").setValue(input);
            Update m = new Update(0, null, input, Utilities.trimProfilePic(mUser), mUser.getName(), mUser.getUserKey());
            // Create a new, auto-generated child of that chat location, and save our chat data there
            DatabaseReference r = mFirebaseRef.push();
            r.setValue(m);
            r.child(Update.TIMESTAMP_FIELD_STR).setValue(ServerValue.TIMESTAMP);
            //Update Shared Preferences
            Map<String, Object> map = mUser.getPosts_contributed_to() == null ? new HashMap<String, Object>() : mUser.getPosts_contributed_to();
            map.put(mId, true);
            mUser.setPosts_contributed_to(map);
            mFirebaseRef.getRoot().child("users/" + mUser.getUid() + "/posts_contributed").updateChildren(mUser.getPosts_contributed_to());
            mInputText.setText("");
        }

    }

    @OnClick(R.id.btnBottom)
    public void gotoBottom(View view) {
        mListView.scrollToPosition(mMessagesListAdapter.getItemCount() - 1);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings_chat:
                Intent intent = new Intent(ChatActivity.this, StorySettingsActivity.class);
                intent.putExtra(TAG_STORY, mStory);
                intent.putExtra(TAG_ID, mId);
                startActivityForResult(intent, 1);
//                ChatActivity.this.finish();
                return true;
        }
        getSupportActionBar().setTitle(mStory.getTitle());
        setTitle(mStory.getTitle());
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(mUser.getUid().equals(mStory.getAuthor())){
            getMenuInflater().inflate(R.menu.menu_chat, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void setupMenu() {
        getSupportActionBar().setTitle(mStory.getTitle());
        setTitle(mStory.getTitle());
        Glide.with(this).load(mStory.getPosts_picture()).into(mBackdropImageView);
    }

    @Override
    public void onFragmentInteraction(int state, Bundle args) {
        if (mStateLayout.getState() != state)
            mStateLayout.setState(state);
            mBtnBottom.setVisibility(state != StateLayout.VIEW_ERROR ? View.VISIBLE : View.GONE);
    }
}