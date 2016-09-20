package com.grahm.livepost.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
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
import com.grahm.livepost.objects.FirebaseActivity;
import com.grahm.livepost.objects.Story;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StorySettingsActivity extends FirebaseActivity {
    @BindView(R.id.list_contributors)
    public RecyclerView mListContributors;
    @BindView(R.id.settings_toolbar)
    public Toolbar mToolbar;
    @BindView(R.id.text_story_code)
    public EditText mTextStoryCode;
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
        //TODO Uncomment this
        //if(mStory.getAuthorString()==mUser.getEmail())
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        mToolbar.setTitle(getString(R.string.story_settings_title));

    }
    @OnClick(R.id.btn_story_code)
    public void copyCodeButtonCallback(View v){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getString(R.string.story_code), mTextStoryCode.getText());
        clipboard.setPrimaryClip(clip);
    }
    @OnClick(R.id.btn_story_delete)
    public void deleteStoryButton(View v){

    }
}
