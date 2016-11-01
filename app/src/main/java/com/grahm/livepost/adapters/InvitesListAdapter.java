package com.grahm.livepost.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.grahm.livepost.R;
import com.grahm.livepost.activities.MainActivity;
import com.grahm.livepost.interfaces.OnFragmentInteractionListener;
import com.grahm.livepost.objects.Invite;
import com.grahm.livepost.objects.Story;
import com.grahm.livepost.util.Utilities;

import org.apache.commons.io.FilenameUtils;

import java.util.Arrays;
import java.util.List;

import static com.grahm.livepost.util.Utilities.mUser;

public class InvitesListAdapter extends FirebaseListAdapter<Invite> {
    private static final String TAG = "InvitesListAdapter";

    private DatabaseReference mFirebaseRef;
    private int mVItemLayout;
    private int mListType;
    private Context mCtx;
    private AppCompatActivity mActivity;


    public InvitesListAdapter(Query ref, AppCompatActivity activity, int listType, boolean searchingFlag) {
        super(ref, Invite.class, searchingFlag);
        Log.e(TAG, ref.toString());
        mListType = listType;
        mVItemLayout = R.layout.item_invite;
        mCtx = activity.getApplicationContext();
        mActivity = activity;
        mFirebaseRef = FirebaseDatabase.getInstance().getReference();
        Utilities.getUser(mFirebaseRef,activity,null);

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new InviteViewHolder(LayoutInflater.from(parent.getContext()).inflate(mVItemLayout, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final Invite invite = getItem(position);
        final String key = getItemKey(position);

        final InviteViewHolder iholder = (InviteViewHolder) holder;
        iholder.mItem = invite;
        String authorStr = invite.getSenderKey();
        String stringFormat = "<b>" + authorStr + "</b>" + " has invited you to contribute to " + "<b>" + invite.getStoryTitle() + "</b>";
        iholder.mTitleView.setText(Html.fromHtml(stringFormat));
        String lastTime = Utilities.getTimeMsg(invite.getTimestamp());
        iholder.mDateTimeView.setText(lastTime);
        Glide.with(mCtx).load(Utilities.trimPicture(invite.getSenderProfilePicture())).asBitmap().centerCrop().into(new BitmapImageViewTarget(iholder.mIconView) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(mCtx.getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                iholder.mIconView.setImageDrawable(circularBitmapDrawable);
            }
        });
        iholder.mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accept(iholder.mItem);
            }
        });
        iholder.mDeclineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decline(iholder.mItem);
            }
        });

    }
    private void decline(final Invite invite){
        mFirebaseRef.child("members/"+invite.getStoryId()+"/"+mUser.getUserKey()).removeValue();
        mFirebaseRef.child("users/"+mUser.getUserKey()+"/invites/"+invite.getStoryId()).removeValue();
        Toast.makeText(mActivity, mActivity.getString(R.string.toast_declined), Toast.LENGTH_SHORT).show();
    }
    private void accept(final Invite invite){
        mFirebaseRef.child("members/"+invite.getStoryId()+"/"+mUser.getUserKey()+"/role").setValue("contributor");
        mFirebaseRef.child("users/"+mUser.getUserKey()+"/invites/"+invite.getStoryId()).removeValue();
        mFirebaseRef.child("posts/" +invite.getStoryId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null) {
                    Story s = (Story) dataSnapshot.getValue();
                    if(s!=null)
                        mFirebaseRef.child("users/" + mUser.getUserKey() + "/posts_contributed_to/" + invite.getStoryId()).setValue(s);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Toast.makeText(mActivity, mActivity.getString(R.string.toast_accepted), Toast.LENGTH_SHORT).show();
    }


    public class InviteViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final View mSelArea;
        public final TextView mTitleView;
        public final TextView mDateTimeView;
        public final ImageView mIconView;
        public final ProgressBar mProgressImgView;
        public final Button mAcceptButton;
        public final Button mDeclineButton;
        public Invite mItem;

        public InviteViewHolder(View view) {
            super(view);
            mView = view;
            mSelArea = view.findViewById(R.id.sel_area);
            mTitleView = (TextView) view.findViewById(R.id.i_title);
            mDateTimeView = (TextView) view.findViewById(R.id.i_datetime);
            mIconView = (ImageView) view.findViewById(R.id.i_imgProfile);
            mProgressImgView = (ProgressBar) view.findViewById(R.id.i_progress_img);
            mAcceptButton = (Button) view.findViewById(R.id.btn_accept);
            mDeclineButton = (Button) view.findViewById(R.id.btn_decline);
        }
    }


}
