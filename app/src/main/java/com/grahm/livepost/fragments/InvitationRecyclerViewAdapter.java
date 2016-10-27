package com.grahm.livepost.fragments;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.grahm.livepost.R;
import com.grahm.livepost.adapters.FirebaseListAdapter;
import com.grahm.livepost.interfaces.OnFragmentInteractionListener;
import com.grahm.livepost.objects.Invite;
import com.grahm.livepost.objects.Story;
import com.grahm.livepost.objects.Update;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.util.Utilities;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Invite} and makes a call to the
 * TODO: Replace the implementation with code for your data type.
 */
public class InvitationRecyclerViewAdapter extends FirebaseListAdapter {
    private static final String TAG = "InvitationAdapter";
    private final FirebaseDatabase mFirebaseDB;
    private final Query mQuery;
    private User mUser;

    public InvitationRecyclerViewAdapter(Query ref) {
        super(ref, Invite.class,true);
        mQuery = ref;
        mFirebaseDB = FirebaseDatabase.getInstance();
        mUser = Utilities.mUser;
    }

    @Override
    public InviteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invite, parent, false);
        return new InviteViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final InviteViewHolder h = (InviteViewHolder) holder;
        final Invite i = (Invite)getItem(position);
        h.mItem = i;
        h.mContentView.setText(i.getSenderKey()+" has invited you to "+i.getStoryTitle());
        if(i.getTimestamp()!=null)
            h.mDateTimeView.setText(Utilities.getTimeMsg(i.getTimestamp()));
        Glide.with(h.mView.getContext()).
                load(i.getSenderProfilePicture())
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .placeholder(R.drawable.default_placeholder)
                .fitCenter()
                .into(h.mImageView);
        h.mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFirebaseDB.getReference("users/"+mUser.getUserKey()+"/invites/"+i.getStoryId()).removeValue();
                mFirebaseDB.getReference("posts/"+i.getStoryId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue()!=null){
                            Story s = dataSnapshot.getValue(Story.class);
                            if(s!=null){
                                mFirebaseDB.getReference("users/"+mUser.getUserKey()+"/posts_contributed_to/"+i.getStoryId()).setValue(s);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG,"Error:"+databaseError.getDetails());
                    }
                });

                mFirebaseDB.getReference("members/"+i.getStoryId()+"/"+mUser.getUserKey()+"/role").setValue("contributor");
                Log.d(TAG,"Accepted");
            }
        });
        h.mDeclineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFirebaseDB.getReference("users/"+mUser.getUserKey()+"/invites/"+i.getStoryId()).removeValue();
                mFirebaseDB.getReference("members/"+i.getStoryId()+"/"+mUser.getUserKey()).removeValue();
                Log.d(TAG,"Declined");
            }
        });
    }


    public class InviteViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mContentView;
        public final TextView mDateTimeView;
        public final ImageView mImageView;
        public final ProgressBar mProgressBar;
        public final Button mAcceptButton;
        public final Button mDeclineButton;
        public Invite mItem;

        public InviteViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = (TextView) view.findViewById(R.id.i_title);
            mImageView = (ImageView) view.findViewById(R.id.i_imgProfile);
            mProgressBar = (ProgressBar)view.findViewById(R.id.i_progress_img);
            mDateTimeView = (TextView) view.findViewById(R.id.i_datetime);
            mAcceptButton = (Button) view.findViewById(R.id.btn_accept);
            mDeclineButton = (Button) view.findViewById(R.id.btn_decline);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
