package com.grahm.livepost.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.bumptech.glide.Glide;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.widget.ShareDialog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.grahm.livepost.R;
import com.grahm.livepost.adapters.ChatAdapter;
import com.grahm.livepost.asynctask.PostImageTask;
import com.grahm.livepost.asynctask.PostVideoTask;
import com.grahm.livepost.interfaces.OnPutImageListener;
import com.grahm.livepost.objects.FirebaseActivity;
import com.grahm.livepost.objects.Story;
import com.grahm.livepost.objects.Update;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.util.GV;
import com.grahm.livepost.util.Util;
import com.grahm.livepost.util.Utilities;
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
import butterknife.OnTextChanged;
import io.fabric.sdk.android.Fabric;
import pl.aprilapps.easyphotopicker.EasyImage;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.grahm.livepost.util.GV.TWITTER_KEY;
import static com.grahm.livepost.util.GV.TWITTER_SECRET;

public class ChatActivity extends FirebaseActivity implements AbsListView.OnItemClickListener {
    private static final String TAG_CLASS = "ChatActivity";
    public static final String TAG_ID = "key";
    public static final String TAG_USER = "user";
    public static final String TAG_STORY = "story";
    private static final int TAKE_PICTURE = 0;
    private static final int PHOTO_SELECTED = 1;

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
    
    public static MainActivity.FragmentsEnum page = MainActivity.FragmentsEnum.CHAT;
    private DatabaseReference mFirebaseRef;
    private PostImageTask mPostImageTask;
    private PostVideoTask mPostVideoTask;
    private String mId;
    private Story mStory;
    private Uri mIimageUri;
    private AmazonS3Client s3Client = new AmazonS3Client(new BasicAWSCredentials(GV.ACCESS_KEY_ID, GV.SECRET_KEY));

    private ChatAdapter mMessagesListAdapter;
    private User mUser;
    private OnPutImageListener putImageListener = new OnPutImageListener() {
        @Override
        public void onSuccess(String url) {
            mFirebaseRef.getRoot().child("posts/" + mId + "/last_message").setValue(url);
            Update m = new Update(0,null,url,mUser.getProfile_picture(), mUser.getName(), mUser.getUserKey());
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
        ButterKnife.bind(this);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig), new TweetUi(), new Twitter(authConfig), new TweetUi(), new TweetComposer());

        FacebookSdk.sdkInitialize(getApplicationContext());

        setSupportActionBar(mToolbar);
        restoreState(savedInstanceState);
        mFirebaseRef = FirebaseDatabase.getInstance().getReference("updates").child(mId);
        setupMenu();
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

        ButterKnife.bind(this);
        try {
            LinearLayoutManager llm = new LinearLayoutManager(this);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            mListView.setLayoutManager(llm);
            mMessagesListAdapter = new ChatAdapter(mFirebaseRef.limitToLast(50), this, mId, mUser);
            mListView.setAdapter(mMessagesListAdapter);
            mMessagesListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    mListView.scrollToPosition(mMessagesListAdapter.getItemCount() - 1);
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
                String mimeType = Util.getMimeTypeFromUri(getBaseContext(),Uri.fromFile(file));
                if(mimeType.contains("video") ){
                    onVideoReturned(file);
                }else {
                    onPhotoReturned(file);
                }
                Log.e(TAG_CLASS,"imagefile:"+file.getName()+"type:"+type+" requestCode:"+requestCode+ " resultCode:"+resultCode);
            }

            @Override
            public void onCanceled(com.grahm.livepost.util.EasyImage.ImageSource source, int type) {

            }
        });
    }

    private void onVideoReturned(File file){
        Uri uri = Uri.fromFile(file);

        mPostVideoTask = new PostVideoTask(this,s3Client,putImageListener,true);
        if(uri!= null) mPostVideoTask.execute(uri);
    }

    private void onPhotoReturned(File imageFile) {
        Uri uri = Uri.fromFile(imageFile);

        mPostImageTask = new PostImageTask(this,s3Client,putImageListener,true);
        if(uri!= null) mPostImageTask.execute(uri);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @OnClick(R.id.btnSend)
    public void sendMessage() {
        String input = mInputText.getText().toString();
        mUser = Utilities.getUser(mFirebaseRef, getBaseContext(), getIntent().getExtras());
        if (TextUtils.isEmpty(input)) {
            Log.d(TAG_CLASS, "Choosing Image");
            Long l = System.currentTimeMillis() / 1000L;
            com.grahm.livepost.util.EasyImage.openChooserWithDocuments(this, mId + "_" + l, 1);
        } else {
            mFirebaseRef.getRoot().child("posts/" + mId + "/last_message").setValue(input);
            Update m = new Update(0,null,input,Utilities.trimProfilePic(mUser), mUser.getName(), mUser.getUserKey());
            // Create a new, auto-generated child of that chat location, and save our chat data there
            DatabaseReference r = mFirebaseRef.push();
            r.setValue(m);
            r.child(Update.TIMESTAMP_FIELD_STR).setValue(ServerValue.TIMESTAMP);
            //Update Shared Preferences
            Map<String, Object> map = mUser.getPosts_contributed() == null ? new HashMap<String, Object>() : mUser.getPosts_contributed();
            map.put(mId, true);
            mUser.setPosts_contributed(map);
            mFirebaseRef.getRoot().child("users/" + mUser.getUid() + "/posts_contributed").updateChildren(mUser.getPosts_contributed());
            mInputText.setText("");
        }

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
                ChatActivity.this.finish();
                return true;
        }
        getSupportActionBar().setTitle(mStory.getTitle());
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void setupMenu() {
        getSupportActionBar().setTitle(mStory.getTitle());
        Glide.with(this).load(mStory.getPosts_picture()).into(mBackdropImageView);
    }

}