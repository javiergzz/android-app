package com.grahm.livepost.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.gson.Gson;
import com.grahm.livepost.R;
import com.grahm.livepost.fragments.EditPostDialogFragment;
import com.grahm.livepost.objects.Update;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.util.Utilities;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.sql.Timestamp;

import butterknife.*;

public class ChatAdapter extends FirebaseListAdapter<Update> {
    private static final String TAG = "ChatAdapter";
    public static final String MSG_KEY = "msg";
    private String mChatKey;
    private FragmentActivity mActivity;
    protected String mUsername;
    private ImageLoader mImageLoader;
    private FirebaseDatabase mFirebaseDB;
    private DatabaseReference mFirebaseRef;
    private DatabaseReference mUserFirebaseRef;
    private User mUser;
    private String mUid;


    public ChatAdapter(Query ref, FragmentActivity activity, int layout, String chatKey) {
        super(ref, Update.class, layout, activity, false);
        this.mActivity = activity;
        this.mChatKey = chatKey;
                ImageLoaderConfiguration config  =  new ImageLoaderConfiguration.Builder(activity).build();
        mImageLoader = ImageLoader.getInstance();
        if(!mImageLoader.isInited()) mImageLoader.init(config);
        mFirebaseDB=FirebaseDatabase.getInstance();
        FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mFirebaseUser!=null){
            mUid = mFirebaseUser.getUid();
            mUser= Utilities.getUser(mFirebaseDB.getReference(),activity,null);
        }
    }

    public void showDialog(Update m) {
        FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
        EditPostDialogFragment newFragment = EditPostDialogFragment.newInstance(m);
        Bundle b = new Bundle();
        b.putSerializable(MSG_KEY,m);
        newFragment.setArguments(b);
        newFragment.show(fragmentManager, "dialog");
    }




    public void loadBitmap(String resUrl, ImageView imageView) {
        mImageLoader.displayImage(resUrl, imageView);
    }
    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ChatViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_one, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ChatViewHolder h = (ChatViewHolder)holder;
        final Update m = getItem(position);
        final String key = getItemKey(position);
        h.mAuthorView.setText(m.getSender() + " ");
        final String msg = m.getMessage();
        if(msg.contains(".png")||msg.contains(".jpg")){
            h.mMessageView.setVisibility(View.GONE);
            h.mImgChatView.setVisibility(View.VISIBLE);
            loadBitmap(Utilities.cleanUrl(msg), h.mImgChatView);
        }else{
            h.mMessageView.setVisibility(View.VISIBLE);
            h.mImgChatView.setVisibility(View.GONE);
            h.mMessageView.setText(m.getMessage());
        }
        if(mUsername!=null && mUsername == m.getSender()){
            h.mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //TODO
                    return false;
                }
            });
            //Edition
        }
        String timeMsg;
        timeMsg = Utilities.getTimeMsg(new Timestamp(m.getTimestamp()));
        if(!TextUtils.isEmpty(timeMsg)){
            h.mDateView.setText(timeMsg);
        }

    }
    public class ChatViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mMessageView;
        public final TextView mAuthorView;
        public final TextView mDateView;
        public final ImageView mImgChatView;

        public Update mItem;
        public ChatViewHolder(View view) {
            super(view);
            mView = view;
            mMessageView=(TextView)view.findViewById(R.id.message);
            mAuthorView=(TextView)view.findViewById(R.id.author);
            mDateView=(TextView)view.findViewById(R.id.date);
            mImgChatView=(ImageView)view.findViewById(R.id.imgChat);
        }
    }

}