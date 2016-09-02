package com.grahm.livepost.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.grahm.livepost.R;
import com.grahm.livepost.adapters.StoryLinearListAdapter;
import com.grahm.livepost.adapters.StoryListAdapter;
import com.grahm.livepost.objects.MultipartFormField;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.util.Utilities;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.*;
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
    @BindView(R.id.profile_pic) public ImageView mImageView;
    @BindView(R.id.pager) public ViewPager mViewPager;
    @BindView(R.id.profile_name) TextView mTitleView;
    @BindView(R.id.profile_email) TextView mEmailView;
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
        mUser = Utilities.getUser(mFirebaseRef,getActivity(),savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);

        ImageLoader imageLoader = ImageLoader.getInstance();
        String[] parts = mUser.getProfile_picture().split("\\?");
        imageLoader.displayImage(parts[0], mImageView);

        mTitleView.setText(mUser.getName());
        mEmailView.setText(mUser.getEmail());
        setupNavigation(view,inflater);
        return view;

    }

    private void setupNavigation(View view,LayoutInflater inflater){
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mProfileViewsManager = new ProfileViewsManager();
        mSectionsPagerAdapter = new ProfileSectionsPagerAdapter(inflater);

        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }
    //Background callback to avoid callbacks from other fragments.
    @OnClick(R.id.background)
    public void doNothing(View v){}

    @OnClick({R.id.created_posts,R.id.contributed_posts})
    public void pagerClick(View view){
        int idx = Integer.parseInt((String) view.getTag());
        mViewPager.setCurrentItem(idx, true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("user",mUser);
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
                return 0;
            }

            public int getLayout() {
                return R.layout.profile_list;
            }

            public boolean onValidate() {
                return true;
            }

            public void onSetup(ViewGroup layout) {
                Map<String,Object> m = mUser.getPosts_contributed();
                if(m!=null) {
                    final RecyclerView recyclerView = ButterKnife.findById(layout, R.id.profile_list);
                    final DatabaseReference f = mFirebaseRef.child("posts");
                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    final StoryLinearListAdapter storyLinearListAdapter = new StoryLinearListAdapter(f, (AppCompatActivity) getActivity(), 1, mUser.getPosts_contributed());
                    recyclerView.setAdapter(storyLinearListAdapter);

                    mFirebaseRef.child("users/"+uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //mUser = new Gson().fromJson(getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE).getString("user", null), User.class);
                            mUser = dataSnapshot.getValue(User.class);
                            getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit().putString("user",new Gson().toJson(mUser,User.class)).commit();
                            StoryLinearListAdapter sessionLinearListAdapter = new StoryLinearListAdapter(f, (AppCompatActivity) getActivity(), 1, mUser.getPosts_contributed());
                            recyclerView.setAdapter(sessionLinearListAdapter);
                            sessionLinearListAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError firebaseError) {

                        }
                    });
                }
            }
        }

        public class CreatedPostsField extends MultipartFormField {
            public int getTitle() {
                return 0;
            }

            public int getLayout() {
                return R.layout.profile_list;
            }

            public boolean onValidate() {
                return true;
            }

            public void onSetup(ViewGroup layout) {
                mUser.getEmail();
                Query q = mFirebaseRef.child("posts").orderByChild("author").equalTo(mUser.getEmail().replace(".",""));
                    RecyclerView recyclerView = ButterKnife.findById(layout, R.id.profile_list);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    recyclerView.setAdapter(new StoryListAdapter(q, (AppCompatActivity) getActivity(), 0, 1, false));
            }
        }
    }
    public class ProfileSectionsPagerAdapter extends PagerAdapter {
        LayoutInflater mInflater;
        MultipartFormField mCurrentPage;

        public ProfileSectionsPagerAdapter(LayoutInflater inflater){
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
            return getString(mCurrentPage.getTitle());
        }
    }
}
