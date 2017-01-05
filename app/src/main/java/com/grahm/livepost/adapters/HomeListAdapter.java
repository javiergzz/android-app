package com.grahm.livepost.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.grahm.livepost.R;
import com.grahm.livepost.activities.MainActivity;
import com.grahm.livepost.interfaces.OnFragmentInteractionListener;
import com.grahm.livepost.objects.Story;
import com.grahm.livepost.util.Utilities;
import com.objectlife.statelayout.StateLayout;

import java.sql.Timestamp;
import java.util.Map;

/**
 * Created by javiergonzalez on 6/21/16.
 */

public class HomeListAdapter extends FirebaseListJointAdapter<Story> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final String TAG = "HomeListAdapter";
    private Context mCtx;
    private AppCompatActivity mActivity;
    private OnFragmentInteractionListener mOnFragmentInteractionListener;
    private Map<String, Object> mFilter;


    public HomeListAdapter(Query query, DatabaseReference ref, AppCompatActivity activity, Map<String, Object> filter) {
        super(query, ref, Story.class, filter);
        mCtx = activity.getApplicationContext();
        mActivity = activity;
        mOnFragmentInteractionListener = (OnFragmentInteractionListener) mActivity;
        mFilter = filter;
        setConnectivityObservers(query);

    }
    private void setConnectivityObservers(Query ref){
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(getItemCount()<=0){
                    switchMainActivityView(StateLayout.VIEW_EMPTY);
                }else {
                    switchMainActivityView(StateLayout.VIEW_CONTENT);
                }
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
                    if(getItemCount()<=0){
                        switchMainActivityView(StateLayout.VIEW_EMPTY);
                    }else {
                        switchMainActivityView(StateLayout.VIEW_CONTENT);
                    }
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

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolderI(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_story, parent, false));
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
            if (!TextUtils.isEmpty(lastMessage)) {
                String mimeString =  MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(lastMessage));
                if(!TextUtils.isEmpty(mimeString) && (mimeString.contains("image"))){
                    iholder.mLastMsgView.setText("Image");
                } else if (!TextUtils.isEmpty(mimeString) && mimeString.contains("video")) {
                    iholder.mLastMsgView.setText("Video");
                } else {
                    iholder.mLastMsgView.setText(lastMessage);
                }
            }
            iholder.mTitleView.setText(story.getTitle());
            String stringFormat = "<b>" + story.getAuthor_name() + "</b>" + " in " + "<b>" + story.getCategory() + "</b>";
            iholder.mCategoryView.setText(Html.fromHtml(stringFormat));

            iholder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle args = new Bundle();
                    args.putString("key", key);
                    args.putSerializable("story", iholder.mItem);
                    if(MainActivity.canContinue){
                        mOnFragmentInteractionListener.onFragmentInteraction(MainActivity.CHAT_IDX, args);
                    }
                }
            });
            if (story.getPosts_picture() != null && !story.getPosts_picture().isEmpty()) {
                String[] parts = story.getPosts_picture().split("\\?");
                loadBitmap(parts[0], iholder.mIconView, iholder.mProgressImgView, false);
            }
            String timeMsg = null;
            Long timelong = story.getLast_time();
            if (timelong != null) {
                Timestamp t = new Timestamp(timelong);
                timeMsg = Utilities.getTimeMsg(timelong);
                if (!TextUtils.isEmpty(timeMsg)) {
                    iholder.mTimestamp.setText(timeMsg);
                }
            }
        }
    }

    public void loadBitmap(final String resUrl, final ImageView imageView, final ProgressBar progressBar, final boolean retry) {
        progressBar.setVisibility(View.VISIBLE);
        Glide.with(mCtx)
                .load(resUrl)
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
        public final TextView mTitleView;
        public final TextView mCategoryView;
        public final TextView mLastMsgView;
        public final ImageView mIconView;
        public final ProgressBar mProgressImgView;
        public Story mItem;
        public final TextView mTimestamp;

        public ViewHolderI(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.s_title);
            mCategoryView = (TextView) view.findViewById(R.id.s_category);
            mLastMsgView = (TextView) view.findViewById(R.id.lastMessage);
            mIconView = (ImageView) view.findViewById(R.id.imgProfile);
            mProgressImgView = (ProgressBar) view.findViewById(R.id.progress_img);
            mTimestamp = (TextView) view.findViewById(R.id.h_datetime);
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

    private void switchMainActivityView(int state){
        Bundle b = new Bundle();
        b.putInt(MainActivity.STATE_KEY, state);
        mOnFragmentInteractionListener.onFragmentInteraction(MainActivity.VIEW_INTERACTIONS,b);
    }


}
