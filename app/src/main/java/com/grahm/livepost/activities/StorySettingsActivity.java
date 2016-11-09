package com.grahm.livepost.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.grahm.livepost.R;
import com.grahm.livepost.adapters.ContributorsAdapter;
import com.grahm.livepost.objects.FirebaseActivity;
import com.grahm.livepost.objects.Invite;
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

    @BindView(R.id.txt_story_settings_title)
    public TextView mTxtTitle;

    private Story mStory;
    private String mId;
    private User mContributor;
    private DatabaseReference mFirebaseRef = FirebaseDatabase.getInstance().getReference();
    private ContributorsAdapter mContributorsAdapter;


    private void restoreState(Bundle savedInstanceState) {
        Bundle b = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
        mStory = (Story) b.getSerializable(ChatActivity.TAG_STORY);
        mId = b.getString(ChatActivity.TAG_ID);
        String url = getString(R.string.embed_url) + mId;
        String embed = "<iframe width=\"100%\" height=\"900\" src=\"" + url + "\" frameborder=\"0\" allowfullscreen></iframe>";


        mTxtTitle.setText(mStory.getIsLive() ? getString(R.string.story_settings_url_title) : getString(R.string.story_settings_code_title));
        mTextStoryCode.setText(mStory.getIsLive() ? url : embed);
        mTextStoryCode.setEnabled(mStory.getIsLive());

        if(!mStory.getIsLive()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mTextStoryCode.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            }
        }

        //TODO Uncomment this
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

        setupContributors();
        setupAddButton();

    }

    private void setupAddButton() {
        //String[] COUNTRIES = new String[] {"Belgium", "France", "Italy", "Germany", "Spain"};

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        mTextContributor.setThreshold(1);


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


        //UsersAdapter adapter = new UsersAdapter(FirebaseDatabase.getInstance().getReference("users"),getApplicationContext(), mId);
    }


    private void setupContributors() {
        mFirebaseRef.child("members/" + mId).orderByChild("role").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                if (map != null) {
                    mContributorsAdapter = new ContributorsAdapter(getApplicationContext(), FirebaseDatabase.getInstance().getReference("users"), mId, map);
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
        Toast.makeText(StorySettingsActivity.this, "Copied to Clipboard", Toast.LENGTH_SHORT).show();
    }

    private void deleteStory(){
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
                            mFirebaseRef.child("users/" + entry.getValue().getUserKey() + "/posts_contributed_to/").child(mId).removeValue();
                            System.out.println(entry.getKey() + "/" + entry.getValue());
                            Log.d(TAG, entry.getKey() + "/" + entry.getValue());
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
        mFirebaseRef.child("updates/" + mId).removeValue();
        onBackPressed();
    }

    @OnClick(R.id.btn_story_delete)
    public void deleteStoryButton(View v) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                StorySettingsActivity.this);
        alertDialogBuilder
                .setTitle(R.string.story_settings_delete)
                .setCancelable(false)
                .setPositiveButton("Delete",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                deleteStory();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @OnClick(R.id.invite_contributor_button)
    public void addContributor(final View v) {
        //Search based on user email
        String q = mTextContributor.getText().toString();
        if(!TextUtils.isEmpty(q)){
            mFirebaseRef.child("users").orderByChild("email").equalTo(q).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                        User u = dataSnapshot.getChildren().iterator().next().getValue(User.class);
                        String userKey =  u.getUserKey();

                        Log.e(TAG,userKey);
                        if (userKey != null)
                            addContributorQuery(u);
                        else
                            Log.e(TAG,userKey);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), getString(R.string.story_settings_invalid_user_error), Toast.LENGTH_LONG).show();
                }
            });
            //Search based on Twitter handle
            mFirebaseRef.child("users").orderByKey().equalTo(q).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                        User u = dataSnapshot.getChildren().iterator().next().getValue(User.class);
                        String userKey =  u.getUserKey();

                        Log.e(TAG,userKey);
                        if (userKey != null)
                            addContributorQuery(u);
                        else
                            Log.e(TAG,userKey);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), getString(R.string.story_settings_invalid_user_error), Toast.LENGTH_LONG).show();
                }
            });
        }


    }

    private void adddContributorQuery() {
        if (mContributor == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.story_settings_invalid_user_error), Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> k = new HashMap<String, Object>();
        k.put("role", "contributor");
        k.put("uid", mContributor.getUserKey());
        map.put(mContributor.getUserKey(), k);
        mFirebaseRef.child("members/" + mId).updateChildren(map);


        Log.d(TAG, "users/" + mContributor.getUserKey() + "/posts_contributed_to/" + map.toString());
        mFirebaseRef.child("users/" + mContributor.getUserKey() + "/posts_contributed_to/" + mId).setValue(mStory);

        mTextContributor.setText("");
        mTextContributor.clearListSelection();
        mTextContributor.clearFocus();
    }

    private void addContributorQuery(final User user) {
        if(user==null) {
            Toast.makeText(this, getString(R.string.toast_declined), Toast.LENGTH_SHORT).show();
            return;
        }
        final String userKey = user.getUserKey();
        //Members Query
        final Map<String, Object> map = new HashMap<String, Object>();
        final Map<String, Object> k = new HashMap<String, Object>();
        Log.e(TAG, "User key:" + userKey);
        k.put("role", "pending");
        k.put("uid", userKey);
        k.put("name",TextUtils.isEmpty(user.getEmail())?user.getEmail():user.getTwitter());
        map.put(userKey, k);

        mFirebaseRef.child("members/" + mId).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Map<String, Object> oldMembers = (HashMap<String, Object>) mutableData.getValue();
                if (oldMembers == null || mutableData.getValue() == null || mutableData.getChildrenCount() < 1) {
                    oldMembers = map;
                } else {
                    oldMembers.put(userKey, k);

                }
                mutableData.setValue(oldMembers);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });

        //Invites Query
        final Invite i = new Invite(mId, mStory.getTitle(), mUser.getUserKey(), mUser.getProfile_picture());
        final Map<String, Object> invitesMap = new HashMap<String, Object>();
        invitesMap.put(mId, i);

        mFirebaseRef.child("users/" + userKey + "/invites").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Map<String, Object> oldInvites = (HashMap<String, Object>) mutableData.getValue();

                if (oldInvites == null || mutableData.getValue() == null || mutableData.getChildrenCount() < 1) {
                    mutableData.setValue(invitesMap);
                } else {
                    oldInvites.put(mId, i);
                    mutableData.setValue(oldInvites);
                    mFirebaseRef.child("users/" + userKey + "/invites/" + mId + "/timestamp").setValue(ServerValue.TIMESTAMP);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });

        mTextContributor.setText("");
        mTextContributor.clearListSelection();
        mTextContributor.clearFocus();
    }
    /*private void addContributorQuery() {
        if (mContributor == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.story_settings_invalid_user_error), Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> k = new HashMap<String, Object>();
        k.put("role", "contributor");
        k.put("uid", mContributor.getUserKey());
        map.put(mContributor.getUserKey(), k);
        mFirebaseRef.child("members/" + mId).updateChildren(map);


        Log.d(TAG, "users/" + mContributor.getUserKey() + "/posts_contributed_to/" + map.toString());
        mFirebaseRef.child("users/" + mContributor.getUserKey() + "/posts_contributed_to/" + mId).setValue(mStory);

        mTextContributor.setText("");
        mTextContributor.clearListSelection();
        mTextContributor.clearFocus();
    }*/
}