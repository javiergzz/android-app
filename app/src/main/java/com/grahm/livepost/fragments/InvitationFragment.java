package com.grahm.livepost.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.grahm.livepost.R;
import com.grahm.livepost.adapters.InvitesListAdapter;
import com.grahm.livepost.objects.Invite;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.util.Utilities;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * A fragment representing a list of Items.
 * <p/>
 */
public class InvitationFragment extends Fragment {
    private static final String TAG = "InvitationFragment";
    private static final String N_NEW_KEY = "nnew";
    @BindView(R.id.invitations_list) public RecyclerView mRecyclerView;
    private int mNew;
    private User mUser;
    private InvitesListAdapter mInvitesListAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public InvitationFragment() {
    }

    public static InvitationFragment newInstance(Bundle args) {
        InvitationFragment fragment = new InvitationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Retrieve arguments
        mUser= Utilities.mUser;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invitation_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            //mRecyclerView.setAdapter(new InvitationRecyclerViewAdapter(new ArrayList<Invite>(mUser.getInvites().values())));
        }
        return view;
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
