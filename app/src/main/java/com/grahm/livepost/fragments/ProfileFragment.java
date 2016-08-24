package com.grahm.livepost.fragments;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.Button;

import com.grahm.livepost.R;

public class ProfileFragment extends Fragment {


    public ProfileFragment() {
    }

    public static ProfileFragment newInstance() {
        return  new ProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        final Button btnCreated = (Button) rootView.findViewById(R.id.btn_created);
        final Button btnContributed = (Button) rootView.findViewById(R.id.btn_contributed);
        final View zeroCreated = rootView.findViewById(R.id.view_zero_created);
        final View zeroContributed = rootView.findViewById(R.id.view_zero_contributed);
        btnCreated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zeroContributed.setVisibility(View.GONE);
                zeroCreated.setVisibility(View.VISIBLE);
                btnCreated.setBackgroundColor(Color.rgb(54, 68, 87));
                btnCreated.setTextColor(Color.WHITE);
                btnContributed.setBackgroundColor(Color.rgb(218, 218, 218));
                btnContributed.setTextColor(Color.BLACK);
            }
        });
        btnContributed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zeroCreated.setVisibility(View.GONE);
                zeroContributed.setVisibility(View.VISIBLE);
                btnContributed.setBackgroundColor(Color.rgb(54, 68, 87));
                btnContributed.setTextColor(Color.WHITE);
                btnCreated.setBackgroundColor(Color.rgb(218, 218, 218));
                btnCreated.setTextColor(Color.BLACK);
            }
        });
        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
