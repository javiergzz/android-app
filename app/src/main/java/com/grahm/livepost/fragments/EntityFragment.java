package com.grahm.livepost.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.grahm.livepost.R;

/**
 * Created by javiergonzalez on 6/21/16.
 */

public class EntityFragment extends Fragment {



    public static EntityFragment newInstance(String id, String author) {
        EntityFragment fragment = new EntityFragment();

        return fragment;
    }
    public EntityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void sendMessage() {

    }

    public  void loadImage(Context ctx, Intent data){

    }

}