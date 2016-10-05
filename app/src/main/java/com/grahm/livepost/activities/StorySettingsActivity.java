package com.grahm.livepost.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.grahm.livepost.R;
import com.grahm.livepost.adapters.ContributorsAdapter;
import com.grahm.livepost.adapters.UsersAdapter;
import com.grahm.livepost.objects.FirebaseActivity;
import com.grahm.livepost.objects.Story;
import com.grahm.livepost.objects.User;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StorySettingsActivity extends FirebaseActivity {
    private static final String TAG = "StorySettingsActivity";
    @BindView(R.id.list_contributors)
    public RecyclerView mListContributors;
//    @BindView(R.id.settings_toolbar)
//    public Toolbar mToolbar;

    @BindView(R.id.text_story_code)
    public TextView mTextStoryCode;

    @BindView(R.id.add_contributor_edit_text)
    public AutoCompleteTextView mTextContributor;

    @BindView(R.id.edit_story_layout)
    public LinearLayout mEditStoryLayout;

    private Story mStory;
    private String mId;
    private User mContributor;
    private DatabaseReference mFirebaseRef = FirebaseDatabase.getInstance().getReference();
    private ContributorsAdapter mContributorsAdapter;


    private void restoreState(Bundle savedInstanceState) {
        Bundle b = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
        mStory = (Story) b.getSerializable(ChatActivity.TAG_STORY);
        mId = b.getString(ChatActivity.TAG_ID);
        String url = "<iframe width=\"100%\" height=\"900\" src=\"" + getString(R.string.embed_url) + mId + "\" frameborder=\"0\" allowfullscreen></iframe>";


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
        outState.putSerializable(ChatActivity.TAG_STORY, mStory);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_settings);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        restoreState(savedInstanceState);
//        mToolbar.setTitle(getString(R.string.story_settings_title));
        //if(mStory.getAuthor()==mUser.getAuthorString())

        setupContributors();
        setupAddButton();

    }

    private void setupAddButton() {
        //String[] COUNTRIES = new String[] {"Belgium", "France", "Italy", "Germany", "Spain"};

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        final ArrayAdapter<User> adapter = new UsersAdapter(usersRef, StorySettingsActivity.this, mId);
        mTextContributor.setThreshold(1);

        mTextContributor.setAdapter(adapter);
        mTextContributor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mContributor = (User) view.getTag();
                Log.e(TAG, mContributor.toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mTextContributor.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mContributor = (User) view.getTag();
                Log.e(TAG, mContributor.toString());
            }
        });
        adapter.notifyDataSetChanged();

        //UsersAdapter adapter = new UsersAdapter(FirebaseDatabase.getInstance().getReference("users"),getApplicationContext(), mId);
    }


    private void setupContributors() {
        mFirebaseRef.child("members/" + mId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                if (map != null) {
                    mContributorsAdapter = new ContributorsAdapter(FirebaseDatabase.getInstance().getReference("users"), mId, map);
                    mListContributors.setLayoutManager(new LinearLayoutManager(StorySettingsActivity.this));
                    mListContributors.setAdapter(mContributorsAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @OnClick(R.id.btn_story_code)
    public void copyCodeButtonCallback(View v) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getString(R.string.story_code), mTextStoryCode.getText());
        clipboard.setPrimaryClip(clip);
    }

    @OnClick(R.id.btn_story_delete)
    public void deleteStoryButton(View v) {
        //Delete post
        mFirebaseRef.child("posts/" + mId).removeValue();

        //Delete from entry contributors
        mFirebaseRef.child("members/" + mId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Fetch contributors
                Map<String, User> m = (Map<String, User>) dataSnapshot.getValue();
                if (m != null) {
                    for (Map.Entry<String, User> entry : m.entrySet()) {
                        try {
                            //Remove from each user's contributed posts field
                            mFirebaseRef.child("users/" + entry.getKey() + "/posts_contributed_to/").child(mId).removeValue();
                            System.out.println(entry.getKey() + "/" + entry.getValue());
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                    dataSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Delete entry from creator
        mFirebaseRef.child("users/" + mStory.getAuthor() + "/posts_created/" + mId).removeValue();
        onBackPressed();
    }

    @OnClick(R.id.add_contributor_button)
    public void addContributor(final View v) {
        if (mContributor == null) {
            String q = mTextContributor.getText().toString();
            mFirebaseRef.child("users").orderByChild("name").equalTo(q).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        mContributor = dataSnapshot.getValue(User.class);
                        addContributorQuery();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), getString(R.string.story_settings_invalid_user_error), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            addContributorQuery();
        }

    }

    private void addContributorQuery() {
        if (mContributor == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.story_settings_invalid_user_error), Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> k = new HashMap<String, Object>();
        k.put("role", "contributor");
        k.put("uid", mContributor.getAuthorString());
        map.put(mContributor.getAuthorString(), k);
        mFirebaseRef.child("members/" + mId).updateChildren(map);


        Log.d(TAG, "users/" + mContributor.getAuthorString() + "/posts_contributed_to/" + map.toString());
        mFirebaseRef.child("users/" + mContributor.getAuthorString() + "/posts_contributed_to/" + mId).setValue(mStory);

        mTextContributor.setText("");
        mTextContributor.clearListSelection();
        mTextContributor.clearFocus();
    }
}