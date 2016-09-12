package com.grahm.livepost.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.Query;
import com.grahm.livepost.R;
import com.grahm.livepost.activities.MainActivity;
import com.grahm.livepost.interfaces.OnFragmentInteractionListener;
import com.grahm.livepost.objects.Story;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * Created by javiergonzalez on 6/21/16.
 */

public class HomeListAdapter extends FirebaseListAdapter<Story> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    public static final int LIST = 1;
    public static final int STAGGERED = 0;
    private static final String TAG = "HomeListAdapter";
    private static final String HEADER_ID = "header";
    private int mVItemLayout;
    private int mListType;
    private Context mCtx;
    private AppCompatActivity mActivity;
    private ImageLoader mImageLoader;
    private OnFragmentInteractionListener mOnFragmentInteractionListener;


    public HomeListAdapter(Query ref, AppCompatActivity activity, int layout, int listType, boolean searchingFlag) {
        super(ref, Story.class, layout, activity, searchingFlag);
        mListType = listType;
        mVItemLayout = listType == STAGGERED ? R.layout.item_story_staggered : R.layout.item_story;
        mCtx = activity.getApplicationContext();

        mActivity = activity;
        mOnFragmentInteractionListener = (OnFragmentInteractionListener) mActivity;
        mImageLoader = ImageLoader.getInstance();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolderI(LayoutInflater.from(parent.getContext()).inflate(mVItemLayout, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final Story story = getItem(position);
        final String key = getItemKey(position);

        if (holder instanceof ViewHolderH) {//Header View
            ViewHolderH hholder = (ViewHolderH) holder;
            hholder.mIdView.setText(story.getTitle());
        } else {//Item view
            final ViewHolderI iholder = (ViewHolderI) holder;
            iholder.mItem = story;
            final String lastMessage = story.getLast_message();
            if (lastMessage != null && lastMessage != "") {
                if (lastMessage.contains(".png")) {
                    iholder.mLastMsgView.setVisibility(View.GONE);
                } else {
                    iholder.mLastMsgView.setText(lastMessage);
                }
            }
            iholder.mTitleView.setText(story.getTitle());
            iholder.mCategoryView.setText(" in " + story.getCategory());

            iholder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle args = new Bundle();
                    args.putString("key", key);
                    args.putString("author", iholder.mItem.getAuthor());
                    mOnFragmentInteractionListener.onFragmentInteraction(MainActivity.CHAT_IDX, args);
                }
            });
            if (story.getPosts_picture() != null && !story.getPosts_picture().isEmpty()) {
                String[] parts = story.getPosts_picture().split("\\?");
                loadBitmap(parts[0], iholder.mIconView, iholder.mProgressImgView, false);
            }
        }
    }

    public void loadBitmap(final String resUrl, final ImageView imageView, final ProgressBar progressBar, final boolean retry) {
        mImageLoader.displayImage(resUrl, imageView, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                ((ImageView) view).setImageResource(R.drawable.default_placeholder);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                Log.e(TAG, "failed to load " + imageUri);
                if (!retry) {
                    //reupload(resUrl, imageView, progressBar);
                    //loadBitmap(imageUri, imageView, progressBar, true);
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    public class ViewHolderI extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitleView;
        public final TextView mCategoryView;
        public final TextView mLastMsgView;
        public final ImageView mIconView;
        public final ProgressBar mProgressImgView;
        public Story mItem;

        public ViewHolderI(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.title);
            mCategoryView = (TextView) view.findViewById(R.id.category);
            mLastMsgView = (TextView) view.findViewById(R.id.lastMessage);
            mIconView = (ImageView) view.findViewById(R.id.imgProfile);
            mProgressImgView = (ProgressBar) view.findViewById(R.id.progress_img);
        }
    }

    public class ViewHolderH extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public Story mItem;

        public ViewHolderH(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.title);
        }
    }

    @Override
    public int getItemViewType(int position) {
        //final Session s = getItem(position);
        //if(s.getCategory()==HEADER_ID)
        //    return TYPE_HEADER;
        return TYPE_ITEM;
    }


}
