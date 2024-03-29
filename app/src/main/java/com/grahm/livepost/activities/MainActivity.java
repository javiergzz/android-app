package com.grahm.livepost.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.grahm.livepost.R;
import com.grahm.livepost.fragments.FragmentChatClass;
import com.grahm.livepost.fragments.HomeFragment;
import com.grahm.livepost.fragments.NewStoryFragment;
import com.grahm.livepost.fragments.ProfileFragment;
import com.grahm.livepost.interfaces.FragmentOnBackClickInterface;
import com.grahm.livepost.interfaces.OnFragmentInteractionListener;
import com.grahm.livepost.objects.FirebaseActivity;
import com.grahm.livepost.util.KeyboardUtil;
import com.grahm.livepost.util.Utilities;
import com.objectlife.statelayout.StateLayout;

import java.io.Serializable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class MainActivity extends FirebaseActivity implements OnFragmentInteractionListener, TabLayout.OnTabSelectedListener {

    public static final String TAG = "MainActivity";
    public static final int HOME_IDX = 0;
    public static final int FAVORITES_IDX = 3;
    public static final int NEW_STORY_IDX = 1;
    public static final int PROFILE_IDX = 2;
    public static final int CHAT_IDX = 4;
    public static final int VIEW_INTERACTIONS = 5;

    /*Fragment tags*/
    public static final String HOME_TAG = "home";
    public static final String FAVORITES_TAG = "favorites";
    public static final String NEW_STORY_TAG = "new";
    public static final String PROFILE_TAG = "profile";
    public static final String CHAT_TAG = "notifications";
    public static final String PREFS_TUTORIAL = "tutorial";
    /*Bundle Keys*/
    public static final String PAGE_KEY = "page";
    public static final String FRAG_ARGS = "frag_args";
    public static final String FRAG_KEY = "frag";
    public static final String STATE_KEY = "state";

    private FragmentsEnum mCurrentPage;
    private SectionsFragmentManager mSectionsFragmentManager;
    @BindView(R.id.sl_layout_state)
    public StateLayout mStateLayout;
    @BindView(R.id.toolbar)
    public Toolbar toolbar;
    @BindView(R.id.v_empty)
    public RelativeLayout mViewEmpty;
    @BindView(R.id.tabs)
    public TabLayout mTabLayout;

    private DatabaseReference mFirebaseRef;
    private int mTutorialCount = 0;
    public static boolean canContinue = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Bundle args = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
        setSupportActionBar(toolbar);
        setupNavigation(savedInstanceState);
        setupTabs();
        KeyboardUtil keyboardUtil = new KeyboardUtil(this, findViewById(R.id.main_content));
        keyboardUtil.enable();
        mFirebaseRef = FirebaseDatabase.getInstance().getReference("posts");
        mUser = Utilities.getUser(mFirebaseRef, this, args);
        SharedPreferences settings = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);
        if (!settings.getBoolean(PREFS_TUTORIAL, false)) {
            canContinue = false;
            loadTutorial();
        }
    }

    private void loadTutorial() {
        String[] titles = {
                "Your Stories",
                "New Stories",
                "Your Profile"
        };
        String[] msg = {
                "This is where all your stories will live. When you create a New Story it will appear here.",
                "Here you can create a new story and choose where to publish it.",
                "Here you'll be able to see the stories you've created and edit your profile information."
        };
        new MaterialTapTargetPrompt.Builder(MainActivity.this)
                .setTarget(((ViewGroup) mTabLayout.getChildAt(0)).getChildAt(mTutorialCount))
                .setFocalColour(Color.argb(180, 54, 68, 87))
                .setBackgroundColour(Color.rgb(51,171,164))
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
                        if(mTutorialCount < 3){
                            loadTutorial();
                        }else{
                            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putBoolean(PREFS_TUTORIAL, true);
                            editor.commit();
                            canContinue = true;
                        }
                    }
                })
                .show();
    }

    private void setupTabs() {
        mTabLayout.addOnTabSelectedListener(this);
    }

    private void setupNavigation(Bundle savedInstanceState) {
        mCurrentPage = savedInstanceState != null && savedInstanceState.containsKey(PAGE_KEY) ? (FragmentsEnum) savedInstanceState.getSerializable(PAGE_KEY) : FragmentsEnum.HOME;
        mSectionsFragmentManager = new SectionsFragmentManager(this.getBaseContext(), getSupportFragmentManager(), R.id.container);
        if (savedInstanceState == null) {
            mSectionsFragmentManager.setPage(mCurrentPage, savedInstanceState);
        } else {
            Fragment f = getSupportFragmentManager().getFragment(savedInstanceState, FRAG_KEY);
            mSectionsFragmentManager.setCurrentFragment(f);
        }

        mStateLayout.setContentViewResId(R.id.container)
                .setErrorViewResId(R.id.v_error)
                .setEmptyViewResId(R.id.v_empty)
                .setLoadingViewResId(R.id.v_loading)
                .initWithState(StateLayout.VIEW_LOADING);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivityForResult(new Intent(this, SettingsActivity.class), 1);
                return true;
        }
        ;
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.btn_new_story_h)
    public void newStory() {
        onFragmentInteraction(NEW_STORY_IDX, null);
    }

    @Override
    public void onFragmentInteraction(int id, Bundle args) {
        switch (id) {
            case HOME_IDX:
                mSectionsFragmentManager.setPage(FragmentsEnum.HOME, args);
                break;
            case FAVORITES_IDX:
                mSectionsFragmentManager.setPage(FragmentsEnum.FAVORITES, args);
                break;
            case PROFILE_IDX:
                mSectionsFragmentManager.setPage(FragmentsEnum.PROFILE, args);
                break;
            case NEW_STORY_IDX:
                mSectionsFragmentManager.setPage(FragmentsEnum.NEW_STORY, args);
                break;
            case CHAT_IDX:
                Intent mainIntent = new Intent(this, ChatActivity.class);
                mainIntent.putExtras(args);
                this.startActivity(mainIntent);
                break;
            case VIEW_INTERACTIONS:
                int state = args.getInt(STATE_KEY, StateLayout.VIEW_CONTENT);
                if (mStateLayout.getState() != state)
                    mStateLayout.setState(state);
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        switch (tab.getPosition()) {
            case 0:
                mCurrentPage = FragmentsEnum.HOME;
                break;
            case 1:
                mCurrentPage = FragmentsEnum.NEW_STORY;
                break;
            case 2:
                mCurrentPage = FragmentsEnum.PROFILE;
                break;
        }

        mSectionsFragmentManager.setPage(mCurrentPage, null);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        onTabSelected(tab);
    }

    public enum FragmentsEnum implements Serializable {

        HOME(HOME_TAG, R.string.home_str) {
            @Override
            public Fragment getInstance(Bundle args) {
                return HomeFragment.newInstance(args);
            }
        },
        FAVORITES(FAVORITES_TAG, R.string.favorites_str) {
            @Override
            public Fragment getInstance(Bundle args) {
                return HomeFragment.newInstance(args);
            }
        },

        NEW_STORY(NEW_STORY_TAG, R.string.new_session_str) {
            @Override
            public Fragment getInstance(Bundle args) {
                return NewStoryFragment.newInstance(args);
            }
        },
        PROFILE(PROFILE_TAG, R.string.profile_str) {
            @Override
            public Fragment getInstance(Bundle args) {
                return ProfileFragment.newInstance(args);
            }
        },
        CHAT(CHAT_TAG, R.string.chat_str) {
            @Override
            public Fragment getInstance(Bundle args) {
                return FragmentChatClass.newInstance(args);
            }
        };
        private final String tag;
        private final int titleId;

        private FragmentsEnum(final String tag, final int titleId) {
            this.tag = tag;
            this.titleId = titleId;
        }

        public String getTag() {
            return tag;
        }

        public String getTitle(Context ctx) {
            return ctx.getString(titleId);
        }

        public abstract Fragment getInstance(Bundle args);
    }

    private class SectionsFragmentManager {
        Fragment mCurrentFragment;
        FragmentManager mFragmentManager;
        Context mContext;
        int mContainerId;

        public SectionsFragmentManager(Context context, FragmentManager fm, int containerId) {
            mContext = context;
            mFragmentManager = fm;
            mContainerId = containerId;
        }

        public void setPage(FragmentsEnum page, Bundle args) {
            mCurrentPage = page;
            if (mFragmentManager.getBackStackEntryCount() > 6) {
                mFragmentManager.popBackStack(); // remove one (you can also remove more)
            }

            mCurrentFragment = page.getInstance(args);
            mFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.abc_popup_enter, R.anim.abc_fade_out)
                    .add(mContainerId, mCurrentFragment, page.getTag())
                    .addToBackStack(page.getTag())
                    .commit();
        }

        public Fragment getCurrentFragment() {
            return mCurrentFragment;
        }

        public void setCurrentFragment(Fragment f) {
            mCurrentFragment = f;
        }
    }

    @Override
    public void onBackPressed() {

        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            if(mSectionsFragmentManager.getCurrentFragment().getTag().equals("new") && NewStoryFragment.V_POSITION > 0){
                ((FragmentOnBackClickInterface) mSectionsFragmentManager.getCurrentFragment()).onClick();
            }else{
                super.onBackPressed();
                mSectionsFragmentManager.setCurrentFragment(getSupportFragmentManager().findFragmentById(R.id.container));
            }
        }
    }

}