package com.grahm.livepost.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Query;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;
import com.google.gson.Gson;
import com.grahm.livepost.activities.LoginActivity;
import com.grahm.livepost.R;
import com.grahm.livepost.adapters.FirebaseListAdapter;
import com.grahm.livepost.objects.Update;
import com.grahm.livepost.objects.Story;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.specialViews.SwipeLayout;
import com.grahm.livepost.util.Utilities;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.apache.commons.io.FilenameUtils;

import java.util.HashMap;
import java.util.Map;

import butterknife.*;

public class ChatAdapter extends FirebaseListAdapter<Update> {
    private static final String TAG = "ChatAdapter";
    public static final String MSG_KEY = "msg";
    private String mChatKey;
    private View.OnClickListener likeListener;
    private View.OnClickListener disLikeListener;
    private FragmentActivity mActivity;
    protected int mNumChildren;
    protected TextView mFollowersView;
    private LruCache<String, Bitmap> mMemoryCache;
    protected String mUsername;
    private DisplayImageOptions mOptions;
    private ImageLoader mImageLoader;
    private Firebase mFirebaseRef;
    private Firebase mUserFirebaseRef;
    private User mUser;
    private String mUid;


    public ChatAdapter(Query ref, FragmentActivity activity, int layout, String chatKey) {
        super(ref, Update.class, layout, activity, false);
        this.mActivity = activity;
        this.mChatKey = chatKey;
        this.mUsername =
                mActivity.getSharedPreferences(mActivity.getResources()
                        .getString(R.string.preference_file_key),Context.MODE_PRIVATE).getString("username", null);
        ImageLoaderConfiguration config  =  new ImageLoaderConfiguration.Builder(activity)

                .build();
        mImageLoader = ImageLoader.getInstance();
        mImageLoader.init(config);
        mFirebaseRef =  new Firebase(mActivity.getResources().getString(R.string.firebase_url));
        String s = mActivity.getSharedPreferences(mActivity.getString(R.string.preference_file_key),Context.MODE_PRIVATE).getString("user",null);
        mUser= s!=null? new Gson().fromJson(s,User.class):null;
        AuthData a = mFirebaseRef.getAuth();
        if(a!=null){
            mUid = a.getUid();
        }
        mUserFirebaseRef = mFirebaseRef.child("users/"+mUid);
    }

    public void showDialog(Update m) {
        FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
        //EditPostDialogFragment newFragment = EditPostDialogFragment.newInstance(m);
        //Bundle b = new Bundle();
        //b.putSerializable(MSG_KEY,m);
        //newFragment.setArguments(b);
        //newFragment.show(fragmentManager, "dialog");
    }



    protected void likeMessage(final Firebase upvotesRef, final int likeCount,final String key){
        //mUserFirebaseRef.child("likes").

        mUserFirebaseRef.child("likes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(key)) {
                    Map<String, Object> m = new HashMap<String, Object>();
                    m.put(mUsername, true);
                    upvotesRef.child("likes").updateChildren(m);
                    Map<String, Object> n = new HashMap<String, Object>();
                    n.put(key, true);
                    mUserFirebaseRef.child("likes").updateChildren(n);

                    upvotesRef.child("countLikes").runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData currentData) {
                            currentData.setValue(likeCount + 1);
                            return Transaction.success(currentData); //we can also abort by calling Transaction.abort()
                        }

                        @Override
                        public void onComplete(FirebaseError firebaseError, boolean committed, DataSnapshot currentData) {
                            if (firebaseError != null)
                                Log.e(TAG, firebaseError.getMessage());
                            //This method will be called once with the results of the transaction.
                        }
                    });
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


    }
    public void loadBitmap(String resUrl, ImageView imageView) {
        //FilenameUtils.removeExtension(resUrl)+"_";
        //String s = resUrl.
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
        h.mTxtLikeView.setText(String.valueOf(m.getCount_likes()));
        h.mAuthorView.setText(m.getSender() + " :");
        if(m.getProfile_picture() != null){
            loadBitmap(m.getProfile_picture(), h.mImgProfileView);
        }
        final String msg = m.getMessage();
        if(msg.contains(".png")||msg.contains(".jpg")){
            h.mMessageView.setVisibility(View.GONE);
            h.mImgChatView.setVisibility(View.VISIBLE);
            loadBitmap(msg, h.mImgChatView);
        }else{
            h.mMessageView.setVisibility(View.VISIBLE);
            h.mImgChatView.setVisibility(View.GONE);
            h.mMessageView.setText(m.getMessage());
        }
        if(mUsername!=null && mUsername == m.getSender()){
            h.mImgDelete.setVisibility(View.VISIBLE);
            h.mImgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialog(m);
                }
            });
        }
        String timeMsg;
        timeMsg = Utilities.getTimeMsg(m.getTimestamp());
        if(!TextUtils.isEmpty(timeMsg)){
            h.mDateView.setText(timeMsg);
        }

        likeListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mFirebaseRef.getAuth()==null){
                    mActivity.startActivityForResult(new Intent(mActivity, LoginActivity.class), 1);
                }else {
                    likeMessage(mFirebaseRef.child("updates/" + mChatKey + "/" + key), m.getCount_likes(), key);
                }
            }
        };
        h.mImgLikeView.setOnClickListener(likeListener);
    }
    public class ChatViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mMessageView;
        public final TextView mAuthorView;
        public final TextView mTxtLikeView;
        public final TextView mDateView;
        public final ImageView mImgChatView;
        public final View mImgLikeView;
        public final ImageView mImgProfileView;;
        public final ImageView mImgDelete;

        public Update mItem;
        public ChatViewHolder(View view) {
            super(view);
            mView = view;
            mMessageView=(TextView)view.findViewById(R.id.message);
            mAuthorView=(TextView)view.findViewById(R.id.author);
            mTxtLikeView=(TextView)view.findViewById(R.id.txtLike)  ;
            mDateView=(TextView)view.findViewById(R.id.date);
            mImgChatView=(ImageView)view.findViewById(R.id.imgChat);
            mImgLikeView=(View)view.findViewById(R.id.imgLike);
            mImgProfileView=(ImageView)view.findViewById(R.id.imgProfile);;
            mImgDelete=(ImageView)view.findViewById(R.id.imgClose);
        }
    }

}