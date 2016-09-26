package com.grahm.livepost.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.AppBarLayout;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.grahm.livepost.R;
import com.grahm.livepost.adapters.ChatAdapter;
import com.grahm.livepost.asynctask.PostImageTask;
import com.grahm.livepost.interfaces.OnPutImageListener;
import com.grahm.livepost.objects.FirebaseActivity;
import com.grahm.livepost.objects.Story;
import com.grahm.livepost.objects.Update;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.util.GV;
import com.grahm.livepost.util.Utilities;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

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
    @BindView(R.id.toolbar_title)
    public TextView mTitle;


    public static MainActivity.FragmentsEnum page = MainActivity.FragmentsEnum.CHAT;
    private DatabaseReference mFirebaseRef;
    private PostImageTask mPostTask;
    private String mId;
    private Story mStory;
    private Uri mIimageUri;
    private AmazonS3Client s3Client = new AmazonS3Client(new BasicAWSCredentials(GV.ACCESS_KEY_ID, GV.SECRET_KEY));
    private ImageLoader mImageLoader= ImageLoader.getInstance();

    private ChatAdapter mMessagesListAdapter;
    private User mUser;
    private OnPutImageListener putImageListener = new OnPutImageListener() {
        @Override
        public void onSuccess(String url) {
            mFirebaseRef.getRoot().child("posts/" + mId + "/last_message").setValue(url);
            Update m = new Update(0,null,url,mUser.getProfile_picture(), mUser.getName(), mUser.getEmail());
            // Create a new, auto-generated child of that chat location, and save our chat data there
            DatabaseReference r = mFirebaseRef.push();
            r.setValue(m);
            r.child(Update.TIMESTAMP_FIELD_STR).setValue(ServerValue.TIMESTAMP);
        }
    };


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(TAG_ID, mId);
        outState.putSerializable(TAG_STORY,mStory);
        outState.putSerializable(TAG_USER, mUser);
        getIntent().putExtras(outState);
        super.onSaveInstanceState(outState);
    }


    private void restoreState(Bundle savedInstanceState){
        Bundle args = savedInstanceState==null?getIntent().getExtras():savedInstanceState;
        if(args!=null){
            mId = args.getString(TAG_ID);
            mStory = (Story) args.getSerializable(TAG_STORY);
        }
        mUser = Utilities.getUser(mFirebaseRef,this,args);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        restoreState(savedInstanceState);
        EasyImage.configuration(this)
                .setImagesFolderName("images")
                .saveInAppExternalFilesDir()
                .setCopyExistingPicturesToPublicLocation(true);
        mFirebaseRef = FirebaseDatabase.getInstance().getReference("updates/"+mId);

        setupViews();
    }

    private void setupViews(){
        setupMenu();
        mImageLoader.displayImage(mStory.getPosts_picture(),mBackdropImageView);
    }


    @OnEditorAction(R.id.messageInput)
    public boolean editorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            sendMessage();
        }
        return true;
    }

    @OnClick(R.id.btnAddPicture)
    public void addPictureCallback() {
        Log.d(TAG_CLASS,"Choosing Image");
        Long l = System.currentTimeMillis()/1000L;
        EasyImage.openChooserWithDocuments(this, mId+"_"+l, 1);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                Toast toast = Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                //Handle the image
                onPhotoReturned(imageFile);
            }
        });
    }

    private void onPhotoReturned(File imageFile){
        Uri uri = Uri.fromFile(imageFile);

        mPostTask = new PostImageTask(this,s3Client,putImageListener,true);
        if(uri!= null) mPostTask.execute(uri);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @OnClick(R.id.btnSend)
    public void sendMessage() {
        String input = mInputText.getText().toString();
        mUser = Utilities.getUser(mFirebaseRef,getBaseContext(),getIntent().getExtras());
        if (!TextUtils.isEmpty(input)) {
            mFirebaseRef.getRoot().child("posts/" + mId + "/last_message").setValue(input);
            Update m = new Update(0,null,input,Utilities.trimProfilePic(mUser), mUser.getName(), mUser.getEmail());
            // Create a new, auto-generated child of that chat location, and save our chat data there
            DatabaseReference r = mFirebaseRef.push();
            r.setValue(m);
            r.child(Update.TIMESTAMP_FIELD_STR).setValue(ServerValue.TIMESTAMP);
            //Update Shared Preferences
            Map<String,Object> map = mUser.getPosts_contributed()==null?new HashMap<String,Object>():mUser.getPosts_contributed();
            map.put(mId, true);
            mUser.setPosts_contributed(map);
            mFirebaseRef.getRoot().child("users/"+mUser.getUid()+"/posts_contributed").updateChildren(mUser.getPosts_contributed());
            mInputText.setText("");
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings_chat:
                startActivityForResult(new Intent(this, StorySettingsActivity.class), 1);
                return true;
        };
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        if(mStory.getTitle()!=null) {
            setTitle(mStory.getTitle());
            mTitle.setText(mStory.getTitle());
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void setupMenu(){
        if(mStory.getTitle()!=null) {
            mToolbar.setTitle(mStory.getTitle());
            mTitle.setText(mStory.getTitle());
        }
        mToolbar.inflateMenu(R.menu.menu_chat);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.action_settings_chat:
                        Intent intent = new Intent(ChatActivity.this, StorySettingsActivity.class);
                        intent.putExtra(TAG_STORY,mStory);
                        intent.putExtra(TAG_ID,mId);
                        startActivity(intent);
                        return true;
                };
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
