package com.grahm.livepost.adapters;

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

import com.google.firebase.database.Query;
import com.grahm.livepost.R;
import com.grahm.livepost.activities.ChatActivity;
import com.grahm.livepost.fragments.EditPostDialogFragment;
import com.grahm.livepost.objects.Update;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.util.Utilities;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TweetUi;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;

import java.io.Serializable;
import java.sql.Timestamp;

public class ChatAdapter extends FirebaseListAdapter<Update> {
    private static final String TAG = "ChatAdapter";
    public static final String MSG_KEY = "msg";
    public static final String KEY_KEY = "key";
    public static final String STORY_KEY = "story";
    public static final int KEY_IDX = 0;
    public static final int ITEM_IDX = 1;
    private String mChatKey;
    private FragmentActivity mActivity;
    protected String mUsername;
    private ImageLoader mImageLoader;
    private User mUser;


    public ChatAdapter(Query ref, FragmentActivity activity, String chatKey, User user) {
        super(ref, Update.class, false);
        this.mActivity = activity;
        this.mChatKey = chatKey;
        mUser = user;
        //Init imageloader if necessary
        mImageLoader = ImageLoader.getInstance();
        if (!mImageLoader.isInited()) {
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(activity).build();
            mImageLoader.init(config);
        }
        mUsername = user.getAuthorString();
    }

    public void showDialog(ChatTag tag) {
        FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
        EditPostDialogFragment newFragment = EditPostDialogFragment.newInstance(mChatKey, tag.key, tag.update);
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
        final String msg = m.getMessage();
        if (msg.contains(".png") || msg.contains(".jpg")) {
            h.mMessageView.setVisibility(View.GONE);
            h.mImgChatView.setVisibility(View.VISIBLE);
            loadBitmap(Utilities.cleanUrl(msg), h.mImgChatView);
            // TODO do smart tweet
//        } else if(TextUtils.isDigitsOnly(msg)){
//            TweetUtils.loadTweet(Long.parseLong(msg), new Callback<Tweet>() {
//                @Override
//                public void success(Result<Tweet> result) {
//                    h.mRelativeMsg.addView(new TweetView(mActivity, result.data));
//                }
//
//                @Override
//                public void failure(TwitterException exception) {
//                    // Toast.makeText(...).show();
//                }
//            });
        }else{
            h.mMessageView.setVisibility(View.VISIBLE);
            h.mImgChatView.setVisibility(View.GONE);
            h.mMessageView.setText(m.getMessage());
        }


        String timeMsg = null;
        Long timelong = m.getTimestamp();
        if (timelong != null) {
            Timestamp t = new Timestamp(timelong);
            timeMsg = Utilities.getTimeMsg(t);
            if (!TextUtils.isEmpty(timeMsg)) {
                h.mDateView.setText(timeMsg);
            }
        }


    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mMessageView;
        public final TextView mAuthorView;
        public final TextView mDateView;
        public final ImageView mImgChatView;
        public final RelativeLayout mRelativeMsg;

        public Update mItem;

        public ChatViewHolder(View view) {
            super(view);
            mView = view;
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Log.d(TAG, "Longclick");
                    ChatTag u = (ChatTag) v.getTag();
                    String sender  = u.update.getSender_key();
                    if (mUsername != null && mUsername.equals(sender)) {
                        //Edition dialog
                        showDialog((ChatTag) v.getTag());
                    }
                    return true;
                }
            });
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

}