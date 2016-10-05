package com.grahm.livepost.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.daimajia.swipe.SimpleSwipeListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.grahm.livepost.R;
import com.grahm.livepost.asynctask.S3PutObjectTask;
import com.grahm.livepost.fragments.FragmentChatClass;
import com.grahm.livepost.interfaces.OnFragmentInteractionListener;
import com.grahm.livepost.specialViews.RoundedImageView;
import com.grahm.livepost.specialViews.SwipeLayout;
import com.grahm.livepost.activities.MainActivity;
import com.grahm.livepost.objects.Story;
import com.grahm.livepost.util.GV;
import com.grahm.livepost.util.Utilities;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;


import org.apache.commons.io.FilenameUtils;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import butterknife.*;
import butterknife.ButterKnife;

public class StoryListAdapter extends FirebaseListAdapter<Story> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    public static final int LIST = 1;
    public static final int STAGGERED = 0;
    private static final String TAG = "StoryListAdapter";
    private static final String HEADER_ID = "header";
    public static MainActivity.FragmentsEnum page = MainActivity.FragmentsEnum.HOME;
    private int mVItemLayout;
    private int mListType;
    private Context mCtx;
    private AppCompatActivity mActivity;
    private ImageLoader mImageLoader;
    private OnFragmentInteractionListener mOnFragmentInteractionListener;


    public StoryListAdapter(Query ref, AppCompatActivity activity, int listType, boolean searchingFlag) {
        super(ref, Story.class, searchingFlag);
        Log.e(TAG, ref.toString());
        mListType = listType;
        mVItemLayout = R.layout.item_session;
        mCtx = activity.getApplicationContext();

        mActivity = activity;
        mOnFragmentInteractionListener = (OnFragmentInteractionListener) mActivity;
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(mCtx).build();
        mImageLoader = ImageLoader.getInstance();
        if (!mImageLoader.isInited()) mImageLoader.init(config);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolderI(LayoutInflater.from(parent.getContext()).inflate(mVItemLayout, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final Story s = getItem(position);
        final String key = getItemKey(position);
        if (holder instanceof ViewHolderH) {//Header View
            ViewHolderH hholder = (ViewHolderH) holder;
            hholder.mIdView.setText(s.getTitle());
        } else {//Item view
            final ViewHolderI iholder = (ViewHolderI) holder;
            iholder.mItem = s;
            final String lastMessage = s.getLast_message();
            if (lastMessage != null && lastMessage != "") {
                if (lastMessage.contains("https://") || lastMessage.contains("http://")) {
                    iholder.mLastMsgView.setVisibility(View.GONE);
                } else {
                    iholder.mLastMsgView.setText(lastMessage);
                }
            }
            iholder.mTitleView.setText(s.getTitle());
            String authorStr = s.getAuthor_name() == null ? s.getAuthor() : s.getAuthor_name();
            String stringFormat = "By <b>" + authorStr + "</b>" + " in " + "<b>" + s.getCategory() + "</b>";
            iholder.mCategoryView.setText(Html.fromHtml(stringFormat));
            String lastTime = Utilities.getTimeMsg(s.getLast_time());
            iholder.mDateTimeView.setText(lastTime);
            loadBitmap(s.getPosts_picture(), iholder.mIconView, iholder.mProgressImgView, false);
            iholder.mSelArea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle args = new Bundle();
                    args.putString("key", key);
                    args.putSerializable("story", iholder.mItem);
                    mOnFragmentInteractionListener.onFragmentInteraction(MainActivity.CHAT_IDX, args);
                }
            });
        }
    }


    public void promptDeletion(final String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.delete_text);
        // Add the buttons
        builder.setPositiveButton(R.string.delete_confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO Security
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("posts/").child(key);
                ref.removeValue();
                ref = FirebaseDatabase.getInstance().getReference("updates/").child(key);
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
        /** Image Compression and re-upload ran once **/
        AmazonS3Client s3Client = new AmazonS3Client(new BasicAWSCredentials(GV.ACCESS_KEY_ID, GV.SECRET_KEY));
        List<String> tempList = Arrays.asList(resUrl.split("/"));
        String pictureName = tempList.get(tempList.size() - 1);
        if (pictureName.contains(".")) {
            List<String> tempList2 = Arrays.asList(pictureName.split("\\."));
            pictureName = tempList2.get(0);
        }
        Uri uri = Uri.parse(resUrl);
        new S3PutObjectTask(mCtx, s3Client, null, pictureName, false).execute(uri);
    }

    public void loadBitmap(final String resUrl, final ImageView imageView, final ProgressBar progressBar, final boolean retry) {

        String rawName;
        if (resUrl.contains("/")) {
            List<String> tempList = Arrays.asList(resUrl.split("/"));
            rawName = tempList.get(tempList.size() - 1);
        } else {
            rawName = resUrl;
        }
        String extLess = FilenameUtils.removeExtension(rawName);
        String resString;
        /*We first try to load a thumbnail or medium sized image for List and staggered versions respectively, if we fail we load the original image*/
//        if (retry)
//            resString = extLess;
//        else
//            resString = mListType == LIST ? extLess + "_thumb" : extLess + "_l_thumb";
//
//        resString = mCtx.getString(R.string.amazon_image_path) + resString + ".jpg";

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
                    reupload(resUrl, imageView, progressBar);
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
        public final View mSelArea;
        public final TextView mTitleView;
        public final TextView mCategoryView;
        public final TextView mLastMsgView;
        public final TextView mDateTimeView;
        public final RoundedImageView mIconView;
        public final ProgressBar mProgressImgView;
        public Story mItem;

        public ViewHolderI(View view) {
            super(view);
            mView = view;
            mSelArea = view.findViewById(R.id.sel_area);
            mTitleView = (TextView) view.findViewById(R.id.i_title);
            mCategoryView = (TextView) view.findViewById(R.id.i_category);
            mDateTimeView = (TextView) view.findViewById(R.id.i_datetime);
            mLastMsgView = (TextView) view.findViewById(R.id.i_lastMessage);
            mIconView = (RoundedImageView) view.findViewById(R.id.i_imgProfile);
            mProgressImgView = (ProgressBar) view.findViewById(R.id.i_progress_img);
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
