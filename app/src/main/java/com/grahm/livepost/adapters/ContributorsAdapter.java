package com.grahm.livepost.adapters;

import android.app.Activity;
import android.support.v4.text.TextUtilsCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.grahm.livepost.R;
import com.grahm.livepost.objects.FirebaseActivity;
import com.grahm.livepost.objects.User;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Map;

/**
 * Created by Vyz on 2016-09-09.
 */
public class ContributorsAdapter extends FirebaseListFilteredAdapter<User> {
    private static final String TAG = "ContributorsAdapter";
    DatabaseReference mFirebaseRef;
    String mStoryId;
    public ContributorsAdapter(Query query, String storyId, Map<String,Object> filter){
        super(query.getRef(),User.class,filter);
        //(DatabaseReference mRef, Class<T> mModelClass, Activity activity, final Map<String,Object> filter)
        mFirebaseRef = FirebaseDatabase.getInstance().getReference();
        mStoryId = storyId;
        mFirebaseRef.child("members/"+mStoryId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new UserViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final User user = getItem(position);
        UserViewHolder h  = (UserViewHolder)holder;
        h.mTextView.setText(user.getName());
        h.mButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteContributor(user.getAuthorString());
            }
        });
    }
    private void deleteContributor(String id){
        //Remove contributors entry
        mFirebaseRef.child("members/"+mStoryId+"/"+id).removeValue();
        //Remove user entry
        mFirebaseRef.child("users/" +id + "/posts_contributed_to/").child(mStoryId).removeValue();
        notifyDataSetChanged();
    }
    class UserViewHolder extends RecyclerView.ViewHolder{
        public final View mView;
        public final TextView mTextView;
        public final Button mButtonView;
        public UserViewHolder(View view){
            super(view);
            mView = view;
            mTextView = (TextView)view.findViewById(R.id.contributor_text);
            mButtonView = (Button)view.findViewById(R.id.remove_contributor_button);
        }
    }
}