package com.grahm.livepost.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.grahm.livepost.fragments.LPLoginFragment;
import com.grahm.livepost.fragments.LoginFragment;
import com.grahm.livepost.fragments.NameFragment;
import com.grahm.livepost.fragments.ProfilePictureFragment;
import com.grahm.livepost.fragments.SignUpFragment;
import com.grahm.livepost.interfaces.OnFragmentInteractionListener;

/**
 * Created by javiergonzalez on 8/26/16.
 */

public class ProfilePagerAdapter extends FragmentStatePagerAdapter {
    private int numPages;
    private OnFragmentInteractionListener mListener;
    public ProfilePagerAdapter(FragmentManager fm, int numPages, OnFragmentInteractionListener listener) {
        super(fm);
        this.numPages = numPages;
        this.mListener = listener;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new LoginFragment();
            case 1:
                return new ProfilePictureFragment().newInstance(mListener);
            case 2:
                return new NameFragment().newInstance(mListener);
            case 3:
                return new SignUpFragment().newInstance(mListener);
            case 4:
                return new LPLoginFragment().newInstance();
            default:
                return new LoginFragment();
        }

    }

    @Override
    public int getCount() {
        return numPages;
    }
}
