package com.grahm.livepost.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.grahm.livepost.R;
import com.grahm.livepost.adapters.HomeFragmentAdapter;
import com.grahm.livepost.fragments.FragmentChatClass;
import com.grahm.livepost.fragments.HomeFragment;
import com.grahm.livepost.fragments.NewStoryFragment;
import com.grahm.livepost.fragments.ProfileFragment;
import com.grahm.livepost.interfaces.OnFragmentInteractionListener;
import com.grahm.livepost.objects.FirebaseActivity;

import java.io.Serializable;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends FirebaseActivity implements OnFragmentInteractionListener, TabLayout.OnTabSelectedListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    public static final String TAG = "MainActivity";
    public static final int HOME_IDX = 0;
    public static final int FAVORITES_IDX = 3;
    public static final int NEW_STORY_IDX = 1;
    public static final int PROFILE_IDX = 2;
    public static final int CHAT_IDX = 4;
    public static final String REBOOT_REQ = "reboot_req";

    /*Fragment tags*/
    public static final String HOME_TAG = "home";
    public static final String FAVORITES_TAG = "favorites";
    public static final String NEW_STORY_TAG = "new";
    public static final String PROFILE_TAG = "profile";
    public static final String CHAT_TAG = "notifications";
    /*Bundle Keys*/
    public static final String PAGE_KEY = "page";
    public static final String FRAG_ARGS = "frag_args";
    public static final String FRAG_KEY = "frag";

    private FragmentsEnum mCurrentPage;
    private SectionsFragmentManager mSectionsFragmentManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupNavigation(savedInstanceState);
        setupTabs();
    }

    private void setupTabs() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setOnTabSelectedListener(this);
    }

    private void setupNavigation(Bundle savedInstanceState) {
        mCurrentPage = savedInstanceState != null && savedInstanceState.containsKey(PAGE_KEY) ? (FragmentsEnum) savedInstanceState.getSerializable(PAGE_KEY) : FragmentsEnum.HOME;
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsFragmentManager = new SectionsFragmentManager(this.getBaseContext(), getSupportFragmentManager(), R.id.container);
        if (savedInstanceState == null) {
            mSectionsFragmentManager.setPage(mCurrentPage, savedInstanceState);
        } else {
            Fragment f = getSupportFragmentManager().getFragment(savedInstanceState, FRAG_KEY);
            mSectionsFragmentManager.setCurrentFragment(f);
        }
        getSupportActionBar().setTitle(mCurrentPage.getTitle(this));
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
            getSupportActionBar().setTitle(page.getTitle(mContext));
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
            super.onBackPressed();
            mSectionsFragmentManager.setCurrentFragment(getSupportFragmentManager().findFragmentById(R.id.container));
            getSupportActionBar().setTitle(mCurrentPage.getTitle(this));
        }
    }

    protected void launchLogin() {
//        startActivityForResult(new Intent(this, Login.class), 1);
    }


}
