package com.grahm.livepost.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author greg
 * @since 6/21/13
 *
 * This class is a generic way of backing an Android ListView with a Firebase location.
 * It handles all of the child events at the given Firebase location. It marshals received data into the given
 * class type. Extend this class and provide an implementation of <code>populateView</code>, which will be given an
 * instance of your list item mLayout and an instance your class that holds your data. Simply populate the view however
 * you like and this class will handle updating the list as the data changes.
 *
 * @param <T> The class type to use as a model for the data contained in the children of the given Firebase location
 */
public abstract class FirebaseListFilteredAdapter<T> extends  RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Query mRef;
    private Class<T> mModelClass;
    private LayoutInflater mInflater;
    private List<T> mModels;
    private Map<String, T> mModelKeys;
    private List<String> mKeys;
    private ChildEventListener mListener;
    private Map<String, Object> mFilter;


    /**
     * @param mRef        The Firebase location to watch for data changes. Can also be a slice of a location, using some
     *                    combination of <code>limit()</code>, <code>startAt()</code>, and <code>endAt()</code>,
     * @param mModelClass Firebase will marshall the data at a location into an instance of a class that you provide
     */
    public FirebaseListFilteredAdapter(DatabaseReference mRef, Class<T> mModelClass, final Map<String,Object> filter) {
        this.mFilter=filter;
        this.mRef = mRef;
        this.mModelClass = mModelClass;
        mModels = new ArrayList<T>();
        mModelKeys = new HashMap<String, T>();
        mKeys = new ArrayList<String>();
        for (String filterKey : mFilter.keySet()) {
            mRef.child(filterKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    T model;
                    String key;
                    model = dataSnapshot.getValue(FirebaseListFilteredAdapter.this.mModelClass);
                    key = dataSnapshot.getKey();
                    mModelKeys.put(key, model);
                    mModels.add(model);
                    mKeys.add(key);
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {
                    Log.e("Firebase Error", firebaseError.getMessage());
                }
            });
        }

    }

    public void cleanup() {
        // We're being destroyed, let go of our mListener and forget about all of the mModels
        Log.d("FirebaseListAdapter", "cleanup()");
        mRef.removeEventListener(mListener);
        mModels.clear();
        mModelKeys.clear();
    }

    @Override
    public int getItemCount() {
        return mModels.size();
    }


    @Override
    public long getItemId(int i) {
        return i;
    }

    public T getItem(int i){
        return mModels.get(i);
    }
    public String getItemKey(int i){
        return mKeys.get(i);
    }
}
