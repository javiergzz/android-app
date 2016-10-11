package com.grahm.livepost.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.firebase.database.Query;
import com.grahm.livepost.R;
import com.grahm.livepost.activities.PlayerActivity;
import com.grahm.livepost.fragments.EditPostDialogFragment;
import com.grahm.livepost.objects.Update;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.specialViews.SwipeLayout;
import com.grahm.livepost.util.Util;
import com.grahm.livepost.util.Utilities;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.sql.Timestamp;

public class ChatAdapter extends FirebaseListAdapter<Update> {
    private static final String TAG = "ChatAdapter";
    public static final String MSG_KEY = "msg";
    public static final String KEY_KEY = "key";
    public static final String STORY_KEY = "story";
    private String mChatKey;
    private FragmentActivity mActivity;
    protected String mUsername;
    private CallbackManager callbackManager;
    private ShareDialog shareDialog;

    public ChatAdapter(Query ref, FragmentActivity activity, String chatKey, User user) {
        super(ref, Update.class, false);
        this.mActivity = activity;
        this.mChatKey = chatKey;
        mUsername = user.getUserKey();
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(activity);

        // this part is optional
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
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
        if (!TextUtils.isEmpty(mimeString) && (mimeString.contains("image") || mimeString.contains("video"))) {
            h.mMessageView.setVisibility(View.GONE);
            h.mImgChatView.setVisibility(View.VISIBLE);
            if (mimeString.contains("image")) {
                h.mPlayIcon.setVisibility(View.GONE);
                Glide.with(mActivity).load(msg).diskCacheStrategy(DiskCacheStrategy.RESULT).placeholder(R.drawable.default_placeholder).fitCenter().into(h.mImgChatView);
            } else if (mimeString.contains("video")) {
                h.mPlayIcon.setVisibility(View.VISIBLE);
                Log.e(TAG, "video:" + Utilities.cleanVideoUrl(h.mItem.getMessage()));
                //Bitmap thumb = ThumbnailUtils.createVideoThumbnail(Utilities.cleanVideoUrl(h.mItem.getMessage()), MediaStore.Images.Thumbnails.MINI_KIND);
                try {
                    Glide.with(mActivity).load(msg.replace(".mp4", ".png")).into(h.mImgChatView);
                } catch (Throwable e) {
                    h.mImgChatView.setImageResource(R.drawable.default_placeholder);
                    Log.e(TAG, e.getMessage());
                }
            }
            h.mBtnShareFacebook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareOnFacebook();
                }
            });
            h.mBtnShareTwitter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        tweetPhoto(msg);
                    } catch (MalformedURLException e) {
                        Log.e("TW_MalformedURL", e.getMessage());
                    } catch (URISyntaxException e) {
                        Log.e("TW_URISyntax", e.getMessage());
                    }
                }
            });
        } else {
            h.mMessageView.setVisibility(View.VISIBLE);
            h.mPlayIcon.setVisibility(View.GONE);
            h.mImgChatView.setVisibility(View.GONE);
            h.mMessageView.setText(msg);
            h.mViewShareFacebook.setVisibility(View.GONE);
            h.mBtnShareTwitter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tweet(msg);
                }
            });
        }

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

        String timeMsg = null;
        Long timelong = m.getTimestamp();
        if (timelong != null) {
            Timestamp t = new Timestamp(timelong);
            timeMsg = Utilities.getTimeMsg(timelong);
            if (!TextUtils.isEmpty(timeMsg)) {
                h.mDateView.setText(timeMsg);
            }
        }

        swipeLayout(h);

    }

    private void shareOnFacebook() {
        ShareLinkContent linkContent = new ShareLinkContent.Builder()
                .setContentTitle("Hello Facebook")
                .setContentDescription(
                        "The 'Hello Facebook' sample  showcases simple Facebook integration")
                .setContentUrl(Uri.parse("http://developers.facebook.com/android"))
                .build();

        shareDialog.show(linkContent);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage, String title) throws IOException {

        File f = new File(inContext.getCacheDir(), title);
        f.createNewFile();

        //Convert bitmap to byte array
        Bitmap bitmap = inImage;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

        //write the bytes in file
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();

        return Uri.fromFile(f);
    }

    private void tweet(String message) {
        TweetComposer.Builder builder = new TweetComposer.Builder(mActivity);
        builder.text(message + " @LivePostApp");
        builder.show();
    }

    private void tweetPhoto(String url) throws MalformedURLException, URISyntaxException {
        TweetComposer.Builder builder = new TweetComposer.Builder(mActivity);
        Log.i("URL IMAGE: ", Utilities.trimPicture(url));
        try {
            Bitmap bitmap = Glide.
                    with(mActivity).
                    load(Utilities.trimPicture(url)).
                    asBitmap().
                    into(-1, -1).
                    get();
            String title = mChatKey + "_" + (System.currentTimeMillis() / 1000L);
            Uri uri = getImageUri(mActivity, bitmap, title);
            builder.text("@LivePostApp");
            builder.image(uri);
            builder.show();
        } catch (Exception e) {
            Log.e("TW_PHOTO", e.getMessage());
        }
    }

    private void swipeLayout(ChatViewHolder iholder) {
        iholder.mSwipeLayout.setClickToClose(true);
        iholder.mSwipeLayout.setLeftSwipeEnabled(false);
        iholder.mSwipeLayout.addDrag(SwipeLayout.DragEdge.Right, iholder.mSwipeLayout.findViewById(R.id.view_share));
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mMessageView;
        public final TextView mAuthorView;
        public final TextView mDateView;
        public final ImageView mImgChatView;
        public final RelativeLayout mRelativeMsg;
        public final ImageView mPlayIcon;
        public final SwipeLayout mSwipeLayout;
        public final View mViewShareFacebook;
        public final ImageButton mBtnShareFacebook;
        public final ImageButton mBtnShareTwitter;

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
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Click");

                    ChatTag u = (ChatTag) v.getTag();
                    String sender = u.update.getSender_key();
                    String message = u.update.getMessage();
                    if (TextUtils.isEmpty(message)) return;
                    String mimeString = Util.getMimeTypeFromUrl(message);
                    if (!TextUtils.isEmpty(mimeString) || mimeString.contains("video")) {
                        if (mUsername != null && mUsername.equals(sender)) {
                            //Edition dialog
                            Log.d(TAG, "Play Video");
                            Intent playIntent = new Intent(mActivity, PlayerActivity.class);
                            playIntent.putExtra(PlayerActivity.UPDATE_KEY, u.update);
                            mActivity.startActivity(playIntent);
                        }
                    } else if (mimeString.contains("gif")) {


                    }

                    Log.d(TAG, "uname:" + mUsername + " sender:" + sender);
                }
            });
            mPlayIcon = (ImageView) view.findViewById(R.id.icon_play);
            mMessageView = (TextView) view.findViewById(R.id.message);
            mAuthorView = (TextView) view.findViewById(R.id.author);
            mDateView = (TextView) view.findViewById(R.id.date);
            mImgChatView = (ImageView) view.findViewById(R.id.imgChat);
            mRelativeMsg = (RelativeLayout) view.findViewById(R.id.msgArea);
            mSwipeLayout = (SwipeLayout) view.findViewById(R.id.swipeSurface);
            mViewShareFacebook = mSwipeLayout.findViewById(R.id.view_share_facebook);
            mBtnShareFacebook = (ImageButton) mSwipeLayout.findViewById(R.id.btn_share_facebook);
            mBtnShareTwitter = (ImageButton) mSwipeLayout.findViewById(R.id.btn_share_twitter);
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