package com.grahm.livepost.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.grahm.livepost.R;
import com.grahm.livepost.objects.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Vyz on 2016-09-09.
 */
public class ContributorsAdapter extends FirebaseListFilteredAdapter<User> {
    private static final String TAG = "ContributorsAdapter";
    DatabaseReference mFirebaseRef;
    String mStoryId;
    Context mContext;
    Map<String, Object> mMap;
    private static final String TAG_PENDING = "pending";
    public ContributorsAdapter(Context context, Query query, String storyId, Map<String, Object> filter) {
        super(query.getRef(), User.class, filter);
        mFirebaseRef = FirebaseDatabase.getInstance().getReference();
        mStoryId = storyId;
        mContext = context;
        mMap = filter;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new UserViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final User user = getItem(position);
        if (user == null) {
            return;
        }
        final UserViewHolder h = (UserViewHolder) holder;
        String postfix = "";

        Map<String, Object> contributor = (Map<String, Object>) mMap.values().toArray()[position];
        String role = contributor.get("role").toString();
        Log.i(TAG, "role: " + role);
        if (role.equals(TAG_PENDING)) {
            postfix = "(Pending)";
            h.mTextView.setTextColor(mContext.getResources().getColor(R.color.light_grey));
        }

        h.mTextView.setText(user.getName() + postfix);
        h.mButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteContributor(user.getUserKey());
            }
        });
        if (user.getProfile_picture() != null) {
            Glide.with(mContext)
                    .load(user.getProfile_picture()).asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .fitCenter().centerCrop().into(new BitmapImageViewTarget(h.mImageView) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    h.mImageView.setImageDrawable(circularBitmapDrawable);
                }
            });
        }
    }

    private void deleteContributor(String id) {
        //Remove contributors entry
        mFirebaseRef.child("members/" + mStoryId + "/" + id).removeValue();
        //Remove user entry
        mFirebaseRef.child("users/" + id + "/posts_contributed_to/").child(mStoryId).removeValue();
        //Remove invite if it exists
        mFirebaseRef.child("users/" + id + "/invites/").child(mStoryId).removeValue();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mImageView;
        public final TextView mTextView;
        public final Button mButtonView;

        public UserViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = (ImageView) view.findViewById(R.id.contributor_image);
            mTextView = (TextView) view.findViewById(R.id.contributor_text);
            mButtonView = (Button) view.findViewById(R.id.remove_contributor_button);
        }
    }
}