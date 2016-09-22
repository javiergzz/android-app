package com.grahm.livepost.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.grahm.livepost.R;
import com.grahm.livepost.adapters.UsersAdapter;
import com.grahm.livepost.objects.FirebaseActivity;
import com.grahm.livepost.objects.Story;
import com.grahm.livepost.objects.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StorySettingsActivity extends FirebaseActivity {
    private static final String TAG = "StorySettingsActivity";
    @BindView(R.id.list_contributors)
    public RecyclerView mListContributors;
    @BindView(R.id.settings_toolbar)
    public Toolbar mToolbar;

    @BindView(R.id.text_story_code)
    public AutoCompleteTextView mTextStoryCode;

    @BindView(R.id.edit_story_layout)
    public LinearLayout mEditStoryLayout;

    private Story mStory;
    private String mId;
    private DatabaseReference mFirebaseRef  = FirebaseDatabase.getInstance().getReference();


    private void restoreState(Bundle savedInstanceState){
        Bundle b = savedInstanceState==null?getIntent().getExtras():savedInstanceState;
        mStory = (Story)b.getSerializable(ChatActivity.TAG_STORY);
        mId = b.getString(ChatActivity.TAG_ID);
        String url = "<iframe width=\"100%\" height=\"900\" src=\""+getString(R.string.embed_url)+mId+"\" frameborder=\"0\" allowfullscreen></iframe>";



        mTextStoryCode.setText(url);
        mTextStoryCode.setEnabled(false);
        //TODO Uncomment this
        //if(mStory.getAuthor()==mUser.getAuthorString())
        mEditStoryLayout.setVisibility(View.VISIBLE);



    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(ChatActivity.TAG_STORY,mStory);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_settings);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        restoreState(savedInstanceState);
        mToolbar.setTitle(getString(R.string.story_settings_title));
        //if(mStory.getAuthor()==mUser.getAuthorString())
        setupAddButton()
        setupContributors();


    }
    private void setupAddButton(){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, COUNTRIES);
        AutoCompleteTextView textView = (AutoCompleteTextView)
                findViewById(R.id.countries_list);
        textView.setAdapter(adapter);
    }
    private void  setupContributors(){
        mFirebaseRef.child("members/"+mId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String,Object> map = (Map<String,Object>)dataSnapshot.getValue();
                if(map!=null ) {
                    UsersAdapter adapter = new UsersAdapter(FirebaseDatabase.getInstance().getReference("users"), mId, map);
                    mListContributors.setLayoutManager(new LinearLayoutManager(StorySettingsActivity.this));
                    mListContributors.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    @OnClick(R.id.btn_story_code)
    public void copyCodeButtonCallback(View v){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getString(R.string.story_code), mTextStoryCode.getText());
        clipboard.setPrimaryClip(clip);
    }
    @OnClick(R.id.btn_story_delete)
    public void deleteStoryButton(View v){
        //Delete post
        mFirebaseRef.child("posts/"+mId).removeValue();

        //Delete from entry contributors
        mFirebaseRef.child("members/"+mId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Fetch contributors
                Map<String,User> m = (Map<String,User>)dataSnapshot.getValue();
                for (Map.Entry<String, User> entry : m.entrySet())
                {
                    try {
                        //Remove from each user's contributed posts field
                        mFirebaseRef.child("users/" + entry.getKey() + "/posts_contributed_to/").child(mId).removeValue();
                        System.out.println(entry.getKey() + "/" + entry.getValue());
                    }catch (Exception e){
                        Log.e(TAG, e.getMessage());
                    }
                }
                dataSnapshot.getRef().removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Delete entry from creator
        mFirebaseRef.child("users/"+mStory.getAuthor()+"/posts_created/"+mId).removeValue();
    }
    @OnClick(R.id.add_contributor_button)
    public void addContributor(View v){
        mFirebaseRef
    }
}
