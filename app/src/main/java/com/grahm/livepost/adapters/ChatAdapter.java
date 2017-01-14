package com.grahm.livepost.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.transcode.BitmapToGlideDrawableTranscoder;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.grahm.livepost.R;
import com.grahm.livepost.activities.PlayerActivity;
import com.grahm.livepost.fragments.EditPostDialogFragment;
import com.grahm.livepost.interfaces.OnFragmentInteractionListener;
import com.grahm.livepost.objects.Update;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.specialViews.SwipeLayout;
import com.grahm.livepost.util.Util;
import com.grahm.livepost.util.Utilities;
import com.objectlife.statelayout.StateLayout;
import com.stfalcon.frescoimageviewer.ImageViewer;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
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
        h.mContentView.setTag(new ChatTag(key, m));
        h.mAuthorView.setText(m.getSender() + " ");
        final String msg = m.getMessage();
        String mimeString = Util.getMimeTypeFromUrl(Utilities.cleanUrl(m.getMessage()));

        View.OnLongClickListener longClick = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String sender = m.getSender_key();
                showDialog(new ChatTag(key, m));
                return true;
            }
        };

        if (!TextUtils.isEmpty(mimeString)) {
            h.mMessageView.setVisibility(View.GONE);
            h.mImgChatView.setVisibility(View.VISIBLE);
            if (mimeString.contains("image")) {
                if (mimeString.contains("gif")) {
                    h.mContentView.setOnLongClickListener(longClick);
                    setupGifMessage(h, Utilities.cleanUrl(msg));
                } else {
                    h.mImgChatView.setOnLongClickListener(longClick);
                    setupImageMessage(h, Utilities.cleanUrl(msg));
                    View.OnClickListener openGallery = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String[] list = {msg};
                            new ImageViewer.Builder(mActivity, list)
                                    .setStartPosition(0)
                                    .show();
                        }
                    };
                    h.mImgChatView.setOnClickListener(openGallery);
                }
                h.mBtnShareFacebook.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shareOnFacebook(h.mImgChatView);
                    }
                });
                h.mBtnShareTwitter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tweetPhoto(h.mImgChatView);
                    }
                });
            } else if (mimeString.contains("video")) {
                h.mViewShareFacebook.setVisibility(View.GONE);
                h.mViewShareTwitter.setVisibility(View.GONE);
                h.mImgChatView.setOnLongClickListener(longClick);
                if (TextUtils.isEmpty(m.getThumb())) {
                    setupVideoMessage(h, msg);
                } else {
                    setupVideoMessageXml(h, msg, m.getThumb());
                }
                h.mImgChatView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (TextUtils.isEmpty(msg)) return;
                        String mimeString = Util.getMimeTypeFromUrl(msg);
                        if (!TextUtils.isEmpty(mimeString) && mimeString.contains("video")) {
                            Intent playIntent = new Intent(mActivity, PlayerActivity.class);
                            playIntent.putExtra(PlayerActivity.VIDEO_URL_KEY, msg);
                            mActivity.startActivity(playIntent);
                        }

                    }
                });
            }
        } else {
            h.mContentView.setOnLongClickListener(longClick);
            h.mViewShareFacebook.setVisibility(View.GONE);
            h.mBtnShareTwitter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tweet(msg);
                }
            });
            setupTextMessage(h, msg);
        }

        // TODO do smart tweet

        String timeMsg = null;
        Long timelong = m.getTimestamp();
        if (timelong != null) {
            Timestamp t = new Timestamp(timelong);
            timeMsg = Utilities.getTimeMsg(timelong);
            if (!TextUtils.isEmpty(timeMsg)) {
                // TODO replace with strings.xml
                String _str = "just posted";
                timeMsg = timeMsg.equals("in 0 minutes") ? _str : timeMsg.equals("0 minutes ago") ? _str : timeMsg;
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
                .centerCrop()
                .into(h.mImgChatView);
    }

    private void setupGifMessage(final ChatViewHolder h, String msg) {
        final Uri uri = Uri.parse(msg);
//        h.mGifIcon.setVisibility(View.VISIBLE);
        final Context context = mActivity.getApplicationContext();
        final BitmapRequestBuilder<Uri, GlideDrawable> thumbRequest = Glide
                .with(context)
                .load(uri)
                .asBitmap() // force first frame for Gif
                .transcode(new BitmapToGlideDrawableTranscoder(context), GlideDrawable.class)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.default_placeholder)
                .fitCenter();
        thumbRequest.into(h.mImgChatView);
//        h.mContentView.setOnClickListener(new View.OnClickListener() { // or any parent of imgFeed
//            @Override
//            public void onClick(View v) {
//                h.mProgressBar.setVisibility(View.VISIBLE);
        Glide
                .with(context)
                .load(uri) // load as usual (Gif as animated, other formats as Bitmap)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .thumbnail(thumbRequest)
                .dontAnimate()
                .listener(new RequestListener<Uri, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        h.mProgressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(h.mImgChatView);
//                h.mGifIcon.setVisibility(View.GONE);
//            }
//        });
//        swipeLayout(h);
    }

    private void shareOnFacebook(ImageView img) {
        img.buildDrawingCache();
        Bitmap bitmap = img.getDrawingCache();
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(bitmap)
                .build();
        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();
        shareDialog.show(content);
    }

    private void tweet(String message) {
        TweetComposer.Builder builder = new TweetComposer.Builder(mActivity);
        builder.text(message + " @LivePostApp");
        builder.show();
    }

    private void tweetPhoto(ImageView img) {
        img.buildDrawingCache();
        Bitmap bitmap = img.getDrawingCache();
        Uri imageUri = getLocalBitmapUri(bitmap);
        TweetComposer.Builder builder = new TweetComposer.Builder(mActivity);
        builder.text("@LivePostApp");
        builder.image(imageUri);
        builder.show();
    }

    public Uri getLocalBitmapUri(Bitmap bmp) {
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png");
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    private void swipeLayout(ChatViewHolder iholder) {
        iholder.mSwipeLayout.setClickToClose(true);
        iholder.mSwipeLayout.setLeftSwipeEnabled(false);
        iholder.mSwipeLayout.addDrag(SwipeLayout.DragEdge.Right, iholder.mSwipeLayout.findViewById(R.id.view_share));
    }

    private void setupVideoMessage(ChatViewHolder h, String msg) {
        h.mMessageView.setVisibility(View.GONE);
        h.mImgChatView.setBackgroundColor(Color.BLACK);
        h.mPlayIcon.setVisibility(View.VISIBLE);
    }

    private void setupVideoMessageXml(ChatViewHolder h, String videoUrl, String thumbUrl) {
        h.mMessageView.setVisibility(View.GONE);
        h.mImgChatView.setVisibility(View.VISIBLE);
        h.mPlayIcon.setVisibility(View.VISIBLE);
        Glide.with(mActivity)
                .load(thumbUrl)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .placeholder(R.drawable.default_placeholder)
                .fitCenter()
                .into(h.mImgChatView);
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final View mContentView;
        public final TextView mMessageView;
        public final TextView mAuthorView;
        public final TextView mDateView;
        public final ImageView mImgChatView;
        public final RelativeLayout mRelativeMsg;
        public final ImageView mPlayIcon;
        public final ImageView mGifIcon;
        public final SwipeLayout mSwipeLayout;
        public final View mViewShareFacebook;
        public final View mViewShareTwitter;
        public final ImageButton mBtnShareFacebook;
        public final ImageButton mBtnShareTwitter;
        public final ProgressBar mProgressBar;

        public Update mItem;

        public ChatViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = view.findViewById(R.id.content);
            mPlayIcon = (ImageView) view.findViewById(R.id.icon_play);
            mGifIcon = (ImageView) view.findViewById(R.id.icon_gif);
            mMessageView = (TextView) view.findViewById(R.id.message);
            mAuthorView = (TextView) view.findViewById(R.id.author);
            mDateView = (TextView) view.findViewById(R.id.date);
            mImgChatView = (ImageView) view.findViewById(R.id.imgChat);
            mRelativeMsg = (RelativeLayout) view.findViewById(R.id.msgArea);
            mSwipeLayout = (SwipeLayout) view.findViewById(R.id.swipeSurface);
            mViewShareFacebook = mSwipeLayout.findViewById(R.id.view_share_facebook);
            mViewShareTwitter = mSwipeLayout.findViewById(R.id.view_share_twitter);
            mBtnShareFacebook = (ImageButton) mSwipeLayout.findViewById(R.id.btn_share_facebook);
            mBtnShareTwitter = (ImageButton) mSwipeLayout.findViewById(R.id.btn_share_twitter);
            mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
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

    private void setConnectivityObservers(Query ref) {
        ref.addValueEventListener(new ValueEventListener() {
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
                    if (getItemCount() > 0)
                        switchMainActivityView(StateLayout.VIEW_CONTENT);
                    else
                        switchMainActivityView(StateLayout.VIEW_EMPTY);
                } else {
                    System.out.println("not connected");
                    if (getItemCount() <= 0) {
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

    private void switchMainActivityView(int state) {
        ((OnFragmentInteractionListener) mActivity).onFragmentInteraction(state, null);
    }

}