package com.grahm.livepost.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.grahm.livepost.fragments.LoginFragment;
import com.grahm.livepost.fragments.ProfilePictureFragment;

/**
 * Created by javiergonzalez on 8/26/16.
 */

public class ProfilePagerAdapter extends FragmentStatePagerAdapter {
    private int numPages;
    public ProfilePagerAdapter(FragmentManager fm, int numPages) {
        super(fm);
        this.numPages = numPages;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new LoginFragment();
            case 1:
                return new ProfilePictureFragment();
            default:
                return new LoginFragment();
        }

    }

    @Override
    public int getCount() {
        return numPages;
    }
}
