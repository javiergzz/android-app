package com.grahm.livepost.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.grahm.livepost.R;
import com.grahm.livepost.activities.SettingsActivity;
import com.grahm.livepost.adapters.StoryListAdapter;
import com.grahm.livepost.objects.MultipartFormField;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.specialViews.RoundedImageView;
import com.grahm.livepost.util.Utilities;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    private static final String TAG_CLASS = "PROFILEFRAGMENT";
    @BindView(R.id.profile_pic)
    public RoundedImageView mImageView;
    @BindView(R.id.tabs_profile)
    public TabLayout mTabs;
    @BindView(R.id.pager)
    public ViewPager mViewPager;
    @BindView(R.id.profile_name)
    public TextView mTitleView;
    private User mUser;
    private ProfileViewsManager mProfileViewsManager;
    private DatabaseReference mFirebaseRef;

    private ProfileSectionsPagerAdapter mSectionsPagerAdapter;

    public static ProfileFragment newInstance(Bundle args) {
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseRef = FirebaseDatabase.getInstance().getReference();
        mUser = Utilities.getUser(mFirebaseRef, getActivity(), savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_settings);
        item.setVisible(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(Utilities.trimProfilePic(mUser), mImageView);

        if(mUser!=null) {
            mTitleView.setText(mUser.getName());
        }

        setupNavigation(view, inflater);

        return view;

    }

    private void setupNavigation(View view, LayoutInflater inflater) {
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mProfileViewsManager = new ProfileViewsManager();
        mSectionsPagerAdapter = new ProfileSectionsPagerAdapter(inflater);

        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabs.setupWithViewPager(mViewPager);
        mTabs.getTabAt(0).setText(getString(R.string.my_posts));
        mTabs.getTabAt(1).setText(getString(R.string.contributed_posts));
    }

    //Background callback to avoid callbacks from other fragments.
    @OnClick(R.id.background)
    public void doNothing(View v) {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("user", mUser);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public class ProfileViewsManager {
        public List<MultipartFormField> list;

        public ProfileViewsManager() {
            list = new ArrayList<MultipartFormField>();
            list.add(new CreatedPostsField());
            list.add(new ContributedPostsField());
        }


        public class ContributedPostsField extends MultipartFormField {
            public int getTitle() {
                return R.string.contributed_posts;
            }

            public int getLayout() {
                return R.layout.profile_list;
            }

            public boolean onValidate() {
                return true;
            }

            public void onSetup(ViewGroup layout) {
                Query q = mFirebaseRef.child("users").child(mUser.getAuthorString()).child("posts_contributed_to");
                RecyclerView recyclerView = ButterKnife.findById(layout, R.id.profile_list);
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                recyclerView.setAdapter(new StoryListAdapter(q, (AppCompatActivity) getActivity(), 0, false));
            }
        }

        public class CreatedPostsField extends MultipartFormField {
            public int getTitle() {
                return R.string.my_posts;
            }

            public int getLayout() {
                return R.layout.profile_list;
            }

            public boolean onValidate() {
                return true;
            }

            public void onSetup(ViewGroup layout) {
                Query q = mFirebaseRef.child("posts").orderByChild("author").equalTo(mUser.getAuthorString());
                RecyclerView recyclerView = ButterKnife.findById(layout, R.id.profile_list);
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                recyclerView.setAdapter(new StoryListAdapter(q, (AppCompatActivity) getActivity(), 0, false));
            }
        }
    }

    public class ProfileSectionsPagerAdapter extends PagerAdapter {
        LayoutInflater mInflater;
        MultipartFormField mCurrentPage;

        public ProfileSectionsPagerAdapter(LayoutInflater inflater) {
            mInflater = inflater;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            mCurrentPage = mProfileViewsManager.list.get(position);
            ViewGroup layout = (ViewGroup) mInflater.inflate(mCurrentPage.getLayout(), collection, false);
            mCurrentPage.onSetup(layout);
            collection.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return ((View) object) == view;
        }

        @Override
        public int getCount() {
            return mProfileViewsManager.list.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(mCurrentPage != null){
                return getString(mCurrentPage.getTitle());
            }
            return getString(R.string.my_posts);
        }
    }
}
