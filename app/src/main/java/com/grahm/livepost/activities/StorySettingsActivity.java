package com.grahm.livepost.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class StorySettingsActivity extends FirebaseActivity {
    private static final String TAG = "StorySettingsActivity";
    @BindView(R.id.list_contributors)
    public RecyclerView mListContributors;

    @BindView(R.id.text_story_code)
    public TextView mTextStoryCode;

    @BindView(R.id.add_contributor_edit_text)
    public AutoCompleteTextView mTextContributor;

    @BindView(R.id.edit_story_layout)
    public LinearLayout mEditStoryLayout;

    @BindView(R.id.txt_story_settings_title)
    public TextView mTxtTitle;

    @BindView(R.id.progressLoading)
    public ProgressBar mLoading;

    private Story mStory;
    private String mId;
    private User mContributor;
    private DatabaseReference mFirebaseRef = FirebaseDatabase.getInstance().getReference();
    private ContributorsAdapter mContributorsAdapter;
    private long mChildrenCount = 0;
    private int mTutorialCount = 0;
    public static final String PREFS_TUTORIAL = "tutorial_story_settings";


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
            mTextStoryCode.setBackgroundResource(R.drawable.border_color);
        }

        if (!mStory.getIsLive()) {
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
        SharedPreferences settings = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);
        if (!settings.getBoolean(PREFS_TUTORIAL, false)) {
            mTextStoryCode.setTextColor(Color.rgb(255,255,255));
            mTextContributor.setHintTextColor(Color.rgb(255,255,255));
            loadTutorial();
        }
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "Story Settings", "onCreate");
    }

    private void loadTutorial() {
        String[] titles = {
                "Share your Story",
                "Add a Contributor"
        };
        String[] msg = {
                "Here you'll see either an embed code or a link to your page on LivePost. Remember to share your link so others can read your story!",
                "Invite another LivePoster to help you cover a story. Just add the email or twitter username they used to register on LivePost (Make sure to include the @symbol if they used Twitter to login.)"
        };
        View[] views = {
                findViewById(R.id.text_story_code),
                findViewById(R.id.add_contributor_edit_text)
        };
        new MaterialTapTargetPrompt.Builder(StorySettingsActivity.this)
                .setTarget(views[mTutorialCount])
                .setFocalColour(Color.rgb(51, 171, 164))
                .setBackgroundColour(Color.rgb(51, 171, 164))
                .setPrimaryText(titles[mTutorialCount])
                .setSecondaryText(msg[mTutorialCount])
                .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener() {
                    @Override
                    public void onHidePrompt(MotionEvent event, boolean tappedTarget) {
                        //Do something such as storing a value so that this prompt is never shown again
                    }

                    @Override
                    public void onHidePromptComplete() {
                        mTutorialCount++;
                        if (mTutorialCount < 2) {
                            loadTutorial();
                        } else {
                            mTextStoryCode.setTextColor(Color.rgb(129,128,129));
                            mTextContributor.setHintTextColor(Color.rgb(234,234,234));
                            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putBoolean(PREFS_TUTORIAL, true);
                            editor.commit();
                        }
                    }
                })
                .show();
    }
    private void setupAddButton() {
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
                } else {
                    mListContributors.removeAllViewsInLayout();
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

    private void deleteStory() {
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
        ChatActivity.mChat.finish();
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
        mLoading.setVisibility(View.VISIBLE);
        //Search based on user email
        final String q = mTextContributor.getText().toString();
        if (!TextUtils.isEmpty(q)) {
            mFirebaseRef.child("users").orderByChild("email").equalTo(q).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.i(TAG, "Children Count: " + dataSnapshot.getChildrenCount());
                    mChildrenCount = dataSnapshot.getChildrenCount();
                    if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                        for (DataSnapshot item : dataSnapshot.getChildren()) {
                            User u = item.getValue(User.class);
                            if (u.getUid() != null)
                                addContributorQuery(u);
                        }
                    } else {
                        //Search based on Twitter handle
                        addUserByTwitter(q);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    mLoading.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), getString(R.string.story_settings_invalid_user_error), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void addUserByTwitter(String q) {
        mFirebaseRef.child("users").orderByChild("screenName").equalTo(q).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    Log.i(TAG, dataSnapshot.getValue().toString());
                    User u = dataSnapshot.getChildren().iterator().next().getValue(User.class);
                    addContributorQuery(u);
                } else {
                    mLoading.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), getString(R.string.story_settings_invalid_user_error), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), getString(R.string.story_settings_invalid_user_error), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void addContributorQuery(final User user) {
        if (user == null || !user.isActive()) {
            mLoading.setVisibility(View.GONE);
            if(mChildrenCount < 2){
                Toast.makeText(this, getString(R.string.story_settings_invalid_user_error), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        final String userKey = user.getUid();
        //Members Query
        final Map<String, Object> map = new HashMap<String, Object>();
        final Map<String, Object> k = new HashMap<String, Object>();
        k.put("role", "pending");
        k.put("uid", userKey);
        k.put("name", user.getName());
        map.put(userKey, k);

        mFirebaseRef.getRoot().child("members/" + mId).updateChildren(map);

        //Invites Query
        final Invite i = new Invite(mId, mStory.getTitle(), mUser.getName(), mUser.getProfile_picture());
        final Map<String, Object> invitesMap = new HashMap<String, Object>();
        invitesMap.put(mId, i);

        mFirebaseRef.getRoot().child("users/" + userKey).child("/invites").updateChildren(invitesMap);
        mLoading.setVisibility(View.GONE);
        mTextContributor.setText("");
        mTextContributor.clearListSelection();
        mTextContributor.clearFocus();
    }
}