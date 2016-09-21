package com.grahm.livepost.adapters;

import android.app.Activity;
import android.support.v4.text.TextUtilsCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.grahm.livepost.R;
import com.grahm.livepost.objects.User;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by Vyz on 2016-09-09.
 */
public class UsersAdapter extends FirebaseListAdapter<User> {
    private ImageLoader mImageLoader;
    public UsersAdapter(Query query, Activity activity){
        super(query,User.class);
        mImageLoader = ImageLoader.getInstance();
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new UserViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        User user = getItem(position);
        UserViewHolder h  = (UserViewHolder)holder;
        h.mTextView.setText(user.getName());
        String imageUrl = user.getProfile_picture();
        if(!TextUtils.isEmpty(imageUrl)){
            mImageLoader.displayImage(imageUrl,h.mImageView);
        }
    }
    class UserViewHolder extends RecyclerView.ViewHolder{
        public final View mView;
        public final ImageView mImageView;
        public final TextView mTextView;
        public UserViewHolder(View view){
            super(view);
            mView = view;
            mImageView = (ImageView)view.findViewById(R.id.imgProfile);
            mTextView = (TextView)view.findViewById(R.id.title);
        }
    }
}