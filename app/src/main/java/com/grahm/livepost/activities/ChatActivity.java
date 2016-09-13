package com.grahm.livepost.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.grahm.livepost.R;
import com.grahm.livepost.adapters.ChatAdapter;
import com.grahm.livepost.asynctask.PostImageTask;
import com.grahm.livepost.interfaces.OnFragmentInteractionListener;
import com.grahm.livepost.interfaces.OnPutImageListener;
import com.grahm.livepost.objects.FirebaseActivity;
import com.grahm.livepost.objects.Update;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.util.GV;
import com.grahm.livepost.util.Utilities;

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
    private static final int TAKE_PICTURE = 0;
    private static final int PHOTO_SELECTED = 1;

    @BindView(R.id.messageInput)
    EditText mInputText;
    @BindView(R.id.msg_list)
    RecyclerView mListView;
    public static MainActivity.FragmentsEnum page = MainActivity.FragmentsEnum.CHAT;
    private DatabaseReference mFirebaseRef;
    private PostImageTask mPostTask;
    private String mId;
    private Uri mIimageUri;
    private AmazonS3Client s3Client = new AmazonS3Client(new BasicAWSCredentials(GV.ACCESS_KEY_ID, GV.SECRET_KEY));

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
        outState.putSerializable(TAG_USER, mUser);
        getIntent().putExtra(TAG_USER,mUser);
        super.onSaveInstanceState(outState);
    }

    private void restoreState(Bundle savedInstanceState){
        Bundle args = savedInstanceState==null?getIntent().getExtras():savedInstanceState;
        if(args!=null){
            mId = args.getString(TAG_ID);
            mUser = Utilities.getUser(mFirebaseRef,this,args);
        }
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
        restoreState(savedInstanceState);
        EasyImage.configuration(this)
                .setImagesFolderName("images")
                .saveInAppExternalFilesDir()
                .setCopyExistingPicturesToPublicLocation(true);
        mFirebaseRef = FirebaseDatabase.getInstance().getReference("updates").child(mId);
        ButterKnife.bind(this);
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
        Log.d("FragmentChatClass","Choosing Image");
        Long l = System.currentTimeMillis()/1000L;
        EasyImage.openChooserWithDocuments(this, mId+"_"+l, 1);
    }


    @Override
    public void onStart() {
        super.onStart();

        try {
            LinearLayoutManager llm = new LinearLayoutManager(this);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            mListView.setLayoutManager(llm);
            mMessagesListAdapter = new ChatAdapter(mFirebaseRef.limitToLast(50), this, mId);
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
}
