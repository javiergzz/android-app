package com.grahm.livepost.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.grahm.livepost.R;
import com.grahm.livepost.objects.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

/**
 * Created by Vyz on 2016-09-09.
 */
public class UsersAdapter extends ArrayAdapter<User> {
    private static final String TAG = "UsersAdapter";
    private DatabaseReference mFirebaseRef;
    private ArrayList<User> mUsers = new ArrayList<User>();
    HashMap<String, User> mIdMap = new HashMap<String, User>();
    private LayoutInflater lInflater;

    String mStoryId;
    public UsersAdapter(DatabaseReference ref, Context context, String storyId){
        super(context,android.R.layout.simple_dropdown_item_1line);
        lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //(DatabaseReference mRef, Class<T> mModelClass, Activity activity, final Map<String,Object> filter)
        mStoryId = storyId;
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    User u = dataSnapshot.getValue(User.class);
                    String key = dataSnapshot.getKey();
                    if (u.getName() != null) {
                        add(u);
                        mUsers.add(u);
                        mIdMap.put(key,u);
                        notifyDataSetChanged();
                        Log.e(TAG, u.getName());
                    }
                }catch (Exception e){
                    Log.e(TAG,e.getMessage());
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String userKey = dataSnapshot.getKey();
                User oldUser = mIdMap.get(userKey);
                User newUser = dataSnapshot.getValue(User.class);
                int index = mUsers.indexOf(oldUser);
                //Return if model is no longer found
                if(index == -1) return;
                mUsers.set(index, newUser);
                mIdMap.put(userKey, newUser);
                insert(newUser,index);

                notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String userKey = dataSnapshot.getKey();
                // A model was removed from <></>he list. Remove it from our list and the name mapping
                User oldUser = mIdMap.get(userKey);
                mUsers.remove(oldUser);
                mIdMap.remove(userKey);
                remove(oldUser);
                notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                String userKey = dataSnapshot.getKey();
                User oldUser = mIdMap.get(userKey);
                User newUser = dataSnapshot.getValue(User.class);
                int index = mUsers.indexOf(oldUser);
                //Return if model is no longer found
                if(index == -1) return;
                mUsers.remove(index);
                remove(oldUser);

                if (previousChildName == null) {
                    mUsers.add(0, newUser);
                    insert(newUser,0);
                } else {
                    User previousUser = mIdMap.get(previousChildName);
                    int previousIndex = mUsers.indexOf(previousUser);
                    int nextIndex = previousIndex + 1;
                    if (nextIndex == mUsers.size()) {
                        mUsers.add(newUser);
                        add(newUser);
                    } else {
                        mUsers.add(nextIndex, newUser);
                        insert(newUser,nextIndex);
                    }
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Listen was cancelled, no more updates will occur");
            }
        });
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        User user = getItem(position);


        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
        }

         // getting Integer

        ((TextView) view).setText(user.getName());
        view.setTag(user);
        return view;
    }
}
