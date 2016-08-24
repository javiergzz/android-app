package com.grahm.livepost.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.grahm.livepost.R;
import com.grahm.livepost.objects.Story;
import com.grahm.livepost.specialViews.SwipeLayout;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.apache.commons.io.FilenameUtils;

import java.util.Arrays;
import java.util.List;

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
//    private OnFragmentInteractionListener mOnFragmentInteractionListener;


    public HomeListAdapter(Query ref, AppCompatActivity activity, int layout, int listType, boolean searchingFlag) {
        super(ref, Story.class, layout, activity, searchingFlag);
        mListType = listType;
        mVItemLayout = listType == STAGGERED ? R.layout.item_story_staggered : R.layout.item_story;
        mCtx = activity.getApplicationContext();

        mActivity = activity;
//        mOnFragmentInteractionListener = (OnFragmentInteractionListener) mActivity;
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(mCtx)
                .memoryCache(new WeakMemoryCache())
                .denyCacheImageMultipleSizesInMemory()
                .threadPoolSize(5)
                .defaultDisplayImageOptions(options)
                .build();
        mImageLoader = ImageLoader.getInstance();
        if (!mImageLoader.isInited()) mImageLoader.init(config);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //if(viewType == TYPE_ITEM)
        return new ViewHolderI(LayoutInflater.from(parent.getContext()).inflate(mVItemLayout, parent, false));
        //else
        //    return new ViewHolderH(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_item_header, parent, false));
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
//                    mOnFragmentInteractionListener.onFragmentInteraction(MainActivity.CHAT_IDX, args);
                }
            });
            loadBitmap(story.getPosts_picture(), iholder.mIconView, iholder.mProgressImgView, false);
            if (mListType == LIST)
                swipeLayout(iholder.mView, key);
        }
    }


    private void swipeLayout(View v, final String key) {
        SwipeLayout swipeLayout = (SwipeLayout) v.findViewById(R.id.swipeSurface);
        swipeLayout.setClickToClose(true);
        //swipeLayout.setDragDistance();
        swipeLayout.setLeftSwipeEnabled(false);
        //set show mode.

        //add drag edge.(If the BottomView has 'layout_gravity' attribute, this line is unnecessary)
        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, v.findViewById(R.id.bottom_wrapper));

        ImageButton delButton = (ImageButton) swipeLayout.findViewById(R.id.delete);
        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Delete " + key);
                promptDeletion(key);
            }
        });
        ImageButton editButton = (ImageButton) swipeLayout.findViewById(R.id.edit);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Delete " + key);
                //TODO Edit intent
            }
        });
        swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {

            }

            @Override
            public void onOpen(SwipeLayout layout) {

            }

            @Override
            public void onStartClose(SwipeLayout layout) {

            }

            @Override
            public void onClose(SwipeLayout layout) {

            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

            }
        });
    }

    public void promptDeletion(final String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.delete_text);
        // Add the buttons
        builder.setPositiveButton(R.string.delete_confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO Security
                Firebase ref = new Firebase(mCtx.getString(R.string.firebase_url) + "sessions/").child(key);
                ref.removeValue();
                ref = new Firebase(mCtx.getString(R.string.firebase_url) + "messages/").child(key);
                ref.removeValue();
                Log.d(TAG, "Deleted Session " + key);
            }
        });
        builder.setNegativeButton(R.string.delete_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.dismiss();
            }
        });

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    void reupload(final String resUrl, final ImageView imageView, final ProgressBar progressBar) {
//        /** Image Compression and re-upload ran once **/
//        AmazonS3Client s3Client = new AmazonS3Client(new BasicAWSCredentials(GV.ACCESS_KEY_ID, GV.SECRET_KEY));
//        List<String> tempList = Arrays.asList(resUrl.split("/"));
//        String pictureName = tempList.get(tempList.size() - 1);
//        if (pictureName.contains(".")) {
//            List<String> tempList2 = Arrays.asList(pictureName.split("\\."));
//            pictureName = tempList2.get(0);
//        }
//        Uri uri = Uri.parse(resUrl);
//        new S3PutObjectTask(mCtx, s3Client, null, pictureName, false).execute(uri);
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
