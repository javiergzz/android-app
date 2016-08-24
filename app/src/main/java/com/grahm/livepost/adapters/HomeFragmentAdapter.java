package com.grahm.livepost.adapters;

import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;

import com.grahm.livepost.R;
import com.grahm.livepost.fragments.EntityFragment;
import com.grahm.livepost.fragments.HomeFragment;
import com.grahm.livepost.fragments.ProfileFragment;

/**
 * Created by javiergonzalez on 6/21/16.
 */

public class HomeFragmentAdapter extends FragmentPagerAdapter {

    private static final int HOME_IDX = 0;
    private static final int NEW_IDX = 1;
    private static final int PROFILE_IDX = 2;
    private static final int TOTAL_PAGES = 3;
    private AppCompatActivity activity;

    public HomeFragmentAdapter(FragmentManager fm, AppCompatActivity _activity) {
        super(fm);
        this.activity = _activity;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case HOME_IDX:
                return new HomeFragment();
            case NEW_IDX:
                return new EntityFragment();
            case PROFILE_IDX:
                return new ProfileFragment();
        }
        return new HomeFragment();
    }

    @Override
    public int getCount() {
        return TOTAL_PAGES;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case HOME_IDX:
                return activity.getString(R.string.tab_name_home);
            case NEW_IDX:
                return activity.getString(R.string.tab_name_new);
            case PROFILE_IDX:
                return activity.getString(R.string.tab_name_profile);
        }
        return null;
    }
}
    