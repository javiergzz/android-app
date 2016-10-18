package com.grahm.livepost.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.grahm.livepost.activities.MainActivity;
import com.grahm.livepost.interfaces.OnFragmentInteractionListener;
import com.grahm.livepost.R;


import java.sql.Date;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.grahm.livepost.objects.Story;


import org.apache.commons.io.FilenameUtils;

public class StoryLinearListAdapter extends FirebaseListAdapter<Story> {
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
    private OnFragmentInteractionListener mOnFragmentInteractionListener;


    public StoryLinearListAdapter(Query ref, AppCompatActivity activity, int listType) {
        super(ref, Story.class);
        mListType = listType;
        mVItemLayout = listType == STAGGERED?R.layout.item_session_staggered:R.layout.item_session;
        mCtx = activity.getApplicationContext();

        mActivity = activity;
        mOnFragmentInteractionListener =(OnFragmentInteractionListener) mActivity;
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
        final Story s = getItem(position);
        final String key = getItemKey(position);
        if(holder instanceof ViewHolderH){//Header View
            ViewHolderH hholder = (ViewHolderH)holder;
            hholder.mIdView.setText(s.getTitle());
        }else {//Item view
            final ViewHolderI iholder = (ViewHolderI)holder;
            iholder.mItem = s;
            final String lastMessage = s.getLast_message();
            if(lastMessage!=null && lastMessage!="") {
                if (lastMessage.contains(".png")||lastMessage.contains(".jpg")) {
                    iholder.mLastMsgView.setVisibility(View.GONE);
                } else {
                    iholder.mLastMsgView.setText(lastMessage);
                }
            }
            //iholder.mFollowersView.setText(String.valueOf(s.get()));
            iholder.mTitleView.setText(s.getTitle());
            String authorStr = s.getAuthor_name()==null?s.getAuthor(): s.getAuthor_name();
            String stringFormat = "By <b>" + authorStr + "</b>" + " in " + "<b>" + s.getCategory() + "</b>";
            iholder.mCategoryView.setText(Html.fromHtml(stringFormat));

            String lastTime= new SimpleDateFormat("dd/MM/yyyy").format(s.getLast_time());
            String timestamp =  new SimpleDateFormat("dd/MM/yyyy").format(s.getTimestamp());
            if(lastTime==null){
                lastTime = timestamp;
            }
            iholder.mDateTimeView.setText(lastTime);
            loadBitmap(s.getPosts_picture(), iholder.mIconView, iholder.mProgressImgView, false);
            iholder.mSelArea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle args = new Bundle();
                    args.putString("key", key);
                    args.putSerializable("story",iholder.mItem);
                    mOnFragmentInteractionListener.onFragmentInteraction(MainActivity.CHAT_IDX, args);
                }
            });
        }
    }

    public void promptDeletion(final String key){
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.delete_text);
        // Add the buttons
        builder.setPositiveButton(R.string.delete_confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO Security
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("posts/").child(key);
                ref.removeValue();
                ref =  FirebaseDatabase.getInstance().getReference("updates/").child(key);
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



    public void loadBitmap(final String resUrl, final ImageView imageView, final ProgressBar progressBar,final boolean retry) {

        String rawName;
        if(resUrl.contains("/")) {
            List<String> tempList = Arrays.asList(resUrl.split("/"));
            rawName=tempList.get(tempList.size() - 1);
        }else{
            rawName = resUrl;
        }
        String extLess =  FilenameUtils.removeExtension(rawName);
        String resString;
        /*We first try to load a thumbnail or medium sized image for List and staggered versions respectively, if we fail we load the original image*/
        if(retry)
            resString = extLess;
        else
            resString = mListType==LIST?extLess + "_thumb":extLess+"_l_thumb";

        resString=mCtx.getString(R.string.amazon_image_path)+resString+".jpg";

        progressBar.setVisibility(View.VISIBLE);
        Glide.with(mCtx)
                .load(resString)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(imageView);

    }

    public class ViewHolderI extends RecyclerView.ViewHolder {
        public final View mView;
        public final View mSelArea;
        public final TextView mTitleView;
        public final TextView mCategoryView;
        public final TextView mLastMsgView;
        public final TextView mDateTimeView;
        public final ImageView mIconView;
        public final ProgressBar mProgressImgView;
        public Story mItem;

        public ViewHolderI(View view) {
            super(view);
            mView = view;
            mSelArea = view.findViewById(R.id.sel_area);
            mTitleView = (TextView)view.findViewById(R.id.i_title);
            mCategoryView = (TextView)view.findViewById(R.id.i_category);
            mDateTimeView = (TextView)view.findViewById(R.id.i_datetime);
            mLastMsgView = (TextView)view.findViewById(R.id.i_lastMessage);
            mIconView = (ImageView) view.findViewById(R.id.i_imgProfile);
            mProgressImgView =(ProgressBar) view.findViewById(R.id.i_progress_img);
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
