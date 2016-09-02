package com.grahm.livepost.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.DataSetObserver;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.grahm.livepost.activities.MainActivity;
import com.grahm.livepost.activities.NewStory;
import com.grahm.livepost.activities.LoginActivity;
import com.grahm.livepost.adapters.StoryListAdapter;
import com.grahm.livepost.R;
import com.grahm.livepost.objects.Story;
import com.grahm.livepost.interfaces.OnFragmentInteractionListener;

import java.util.List;
import java.util.Map;

public class ListSessionsFragment extends Fragment {
    private static final String TAG= "ListSessionsFragment";


    private int mPresentationType;
    private DatabaseReference mFirebaseRef;
    private ValueEventListener mConnectedListener;
    private StoryListAdapter mStoryListAdapter;
    private OnFragmentInteractionListener mOnFragmentInteractionListener;

    public static ListSessionsFragment newInstance() {
        ListSessionsFragment fragment = new ListSessionsFragment();
        return fragment;
    }
    public static ListSessionsFragment newInstance(Bundle args) {
        ListSessionsFragment fragment = new ListSessionsFragment();
        if(args!=null)
            fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseRef = FirebaseDatabase.getInstance().getReference("posts");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        Context context = view.getContext();
        if (view instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) view;
                LinearLayoutManager llm = new LinearLayoutManager(context);
                llm.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(llm);
                setupAdapter(recyclerView);
        }
        return view;
    }
    private void setupAdapter(final RecyclerView recyclerView){
        mStoryListAdapter = new StoryListAdapter(mFirebaseRef.limitToLast(50),(AppCompatActivity)getActivity(), R.layout.item_session,1,false);
        recyclerView.setAdapter(mStoryListAdapter);
        mStoryListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                //recyclerView.scrollToPosition(mSessionListAdapter.getItemCount() - 1);
                recyclerView.scrollToPosition(0);
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();


        mConnectedListener = mFirebaseRef.getRoot().child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = (Boolean) dataSnapshot.getValue();
                if (connected) {
                    Log.i(TAG, "Connected to Firebase");
                    //saveSessions();
                } else {
                    Log.i(TAG, "Disconnected from Firebase");
                    //getSessions();
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                // No-op
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        mFirebaseRef.getRoot().child(".info/connected").removeEventListener(mConnectedListener);
        mStoryListAdapter.cleanup();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            //mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}
