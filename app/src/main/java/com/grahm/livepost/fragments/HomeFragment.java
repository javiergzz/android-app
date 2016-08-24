package com.grahm.livepost.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.grahm.livepost.R;
import com.grahm.livepost.adapters.HomeListAdapter;

/**
 * Created by javiergonzalez on 6/21/16.
 */

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private ValueEventListener mConnectedListener;
    private Firebase ref;
    private Context mContext;
    private HomeListAdapter mHomeListAdapter;

    public HomeFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ref = new Firebase(getString(R.string.firebase_url)).child("users/javier@livepostnews/posts_created");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        Context context = rootView.getContext();
        mContext = rootView.getContext();
        if (rootView instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) rootView;
            LinearLayoutManager llm = new LinearLayoutManager(context);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(llm);
            setupAdapter(recyclerView);
        }

        return rootView;
    }

    private void setupAdapter(final RecyclerView recyclerView){
        mHomeListAdapter = new HomeListAdapter(ref.orderByPriority(), (AppCompatActivity)getActivity(), R.layout.item_story, 1 ,false);
        recyclerView.setAdapter(mHomeListAdapter);
        mHomeListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                recyclerView.scrollToPosition(0);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mConnectedListener = ref.getRoot().child(".info/connected").addValueEventListener(new ValueEventListener() {
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
            public void onCancelled(FirebaseError firebaseError) {
                // No-op
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        ref.getRoot().child(".info/connected").removeEventListener(mConnectedListener);
    }
}