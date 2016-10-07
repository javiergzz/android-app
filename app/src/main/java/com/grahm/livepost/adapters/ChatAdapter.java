package com.grahm.livepost.adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.grahm.livepost.R;
import com.grahm.livepost.activities.MainActivity;
import com.grahm.livepost.activities.PlayerActivity;
import com.grahm.livepost.asynctask.UploadVideoThumbTask;
import com.grahm.livepost.fragments.EditPostDialogFragment;
import com.grahm.livepost.interfaces.OnFragmentInteractionListener;
import com.grahm.livepost.objects.Update;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.util.GV;
import com.grahm.livepost.util.Util;
import com.grahm.livepost.util.Utilities;
import com.objectlife.statelayout.StateLayout;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatAdapter extends FirebaseListAdapter<Update> {
    private static final String TAG = "ChatAdapter";
    public static final String MSG_KEY = "msg";
    public static final String KEY_KEY = "key";
    public static final String STORY_KEY = "story";
    private String mChatKey;
    private FragmentActivity mActivity;
    protected String mUsername;


    public ChatAdapter(Query ref, FragmentActivity activity, String chatKey, User user) {
        super(ref, Update.class, false);
        this.mActivity = activity;
        this.mChatKey = chatKey;
        mUsername = user.getUserKey();
        setConnectivityObservers(ref);
    }

    public void showDialog(ChatTag tag) {
        FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
        EditPostDialogFragment newFragment = EditPostDialogFragment.newInstance(mChatKey, tag.key, tag.update);
        newFragment.show(fragmentManager, "dialog");
    }


    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ChatViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_one, parent, false));
    }

    private static final Pattern videoMessagePattern = Pattern.compile("^\\<video\\>(.+)\\<\\/video\\>\\<thumb\\>(.+)\\<\\/thumb\\>");

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final ChatViewHolder h = (ChatViewHolder) holder;
        final Update m = getItem(position);
        if (m == null) {
            Log.e(TAG, "Error: Empty Item at position " + position);
            return;
        }
        final String key = getItemKey(position);
        h.mItem = m;
        h.mView.setTag(new ChatTag(key, m));
        h.mAuthorView.setText(m.getSender() + " ");
        final String msg = Utilities.cleanUrl(m.getMessage());
        String mimeString = Util.getMimeTypeFromUrl(msg);
        if (!TextUtils.isEmpty(mimeString)) {
            h.mMessageView.setVisibility(View.GONE);
            h.mImgChatView.setVisibility(View.VISIBLE);
            if (mimeString.contains("image")) {
                setupImageMessage(h, msg);
            } else if (mimeString.contains("video")) {
                setupVideoMessage(h, msg,key);
            }
        } else {
            //Check if first character is < to avoid pattern matching in messages that don't look like xml
            if (!TextUtils.isEmpty(msg)&& msg.charAt(0) == '<'){
                Matcher matcher = videoMessagePattern.matcher(msg);
                if(matcher.matches())
                    setupVideoMessageXml(h,matcher);
            } else {
                setupTextMessage(h, msg);
            }
        }

        // TODO do smart tweet

        String timeMsg = null;
        Long timelong = m.getTimestamp();
        if (timelong != null) {
            Timestamp t = new Timestamp(timelong);
            timeMsg = Utilities.getTimeMsg(timelong);
            if (!TextUtils.isEmpty(timeMsg)) {
                h.mDateView.setText(timeMsg);
            }
        }
    }

    private void setupTextMessage(ChatViewHolder h, String msg) {
        h.mMessageView.setVisibility(View.VISIBLE);
        h.mPlayIcon.setVisibility(View.GONE);
        h.mImgChatView.setVisibility(View.GONE);
        h.mMessageView.setText(msg);
    }

    private void setupImageMessage(ChatViewHolder h, String msg) {
        h.mPlayIcon.setVisibility(View.GONE);
        Glide.with(mActivity)
                .load(msg)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .placeholder(R.drawable.default_placeholder)
                .fitCenter()
                .into(h.mImgChatView);

    }

    private void setupVideoMessage(ChatViewHolder h, String msg, String key) {
        //Set placeholder to image view
        h.mPlayIcon.setVisibility(View.VISIBLE);
        h.mImgChatView.setImageResource(R.drawable.default_placeholder);
        //Normalize message:
        //1.Generate thumbnail from video.
        //2.Set thumbnail to the image view.
        //3.Upload thumbnail to s3
        //4.Update firebase entry as XML
        Log.d(TAG, "video:" + Utilities.cleanVideoUrl(h.mItem.getMessage()));
        try {
            Bitmap bmp = Utilities.retriveVideoFrameFromVideo(Utilities.cleanVideoUrl(msg));
            h.mImgChatView.setImageBitmap(bmp);
            new UploadVideoThumbTask(FirebaseDatabase.getInstance().getReference("updates/"+key),h.mItem,mActivity, new AmazonS3Client(new BasicAWSCredentials(GV.ACCESS_KEY_ID, GV.SECRET_KEY)) )
                    .execute(bmp);
        }catch (Throwable e){
            Log.e(TAG, "Error:"+e);
        }
        //Set video click callback
        setVideoClickCallback(h,msg,null);
    }
    private void setVideoClickCallback(final ChatViewHolder h, final String msg, final Matcher matcher){
        h.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Click event");
                ChatTag u = (ChatTag) v.getTag();
                String message = u.update.getMessage();
                if (TextUtils.isEmpty(message)) return;
                String mimeString = Util.getMimeTypeFromUrl(message);
                String videoUrl = null;

                if (!TextUtils.isEmpty(mimeString) && mimeString.contains("video")) {
                    //Non-XML callback
                    videoUrl =  u.update.getMessage();
                } else if (matcher!=null && !TextUtils.isEmpty(matcher.group(1))) {
                    //XML callback
                    videoUrl = matcher.group(1);
                }
                if(videoUrl !=null) {
                    Log.d(TAG, "Play Video");
                    Intent playIntent = new Intent(mActivity, PlayerActivity.class);
                    playIntent.putExtra(PlayerActivity.VIDEO_URL_KEY, videoUrl);
                    mActivity.startActivity(playIntent);
                }

            }
        });
    }
    private void setupVideoMessageXml(ChatViewHolder h, Matcher matcher) {
        h.mMessageView.setVisibility(View.GONE);
        h.mImgChatView.setVisibility(View.VISIBLE);
        h.mPlayIcon.setVisibility(View.VISIBLE);
        Glide.with(mActivity)
                .load(matcher.group(2))
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .placeholder(R.drawable.default_placeholder)
                .fitCenter()
                .into(h.mImgChatView);

    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mMessageView;
        public final TextView mAuthorView;
        public final TextView mDateView;
        public final ImageView mImgChatView;
        public final RelativeLayout mRelativeMsg;
        public final ImageView mPlayIcon;

        public Update mItem;

        public ChatViewHolder(View view) {
            super(view);
            mView = view;
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Log.d(TAG, "Longclick");
                    ChatTag u = (ChatTag) v.getTag();
                    String sender = u.update.getSender_key();
                    if (mUsername != null && mUsername.equals(sender)) {
                        //Edition dialog
                        showDialog((ChatTag) v.getTag());
                    }
                    return true;
                }
            });
            mPlayIcon = (ImageView) view.findViewById(R.id.icon_play);
            mMessageView = (TextView) view.findViewById(R.id.message);
            mAuthorView = (TextView) view.findViewById(R.id.author);
            mDateView = (TextView) view.findViewById(R.id.date);
            mImgChatView = (ImageView) view.findViewById(R.id.imgChat);
            mRelativeMsg = (RelativeLayout) view.findViewById(R.id.msgArea);
        }
    }

    public class ChatTag implements Serializable {
        public Update update;
        public String key;

        ChatTag() {
        }

        ChatTag(String key, Update update) {
            this.key = key;
            this.update = update;
        }
    }
    private void setConnectivityObservers(Query ref){
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                switchMainActivityView(StateLayout.VIEW_CONTENT);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                switchMainActivityView(StateLayout.VIEW_ERROR);
            }
        });
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    System.out.println("connected");
                    switchMainActivityView(StateLayout.VIEW_CONTENT);
                } else {
                    System.out.println("not connected");
                    if(getItemCount()<=0){
                        switchMainActivityView(StateLayout.VIEW_ERROR);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled");
                switchMainActivityView(StateLayout.VIEW_ERROR);
            }
        });
    }
    private void switchMainActivityView(int state){
        ((OnFragmentInteractionListener)mActivity).onFragmentInteraction(state,null);
    }

}