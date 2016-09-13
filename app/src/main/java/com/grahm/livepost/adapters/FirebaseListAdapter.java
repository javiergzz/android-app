package com.grahm.livepost.adapters;

/**
 * Created by javiergonzalez on 6/21/16.
 */

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.grahm.livepost.objects.Story;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
public abstract class FirebaseListAdapter<T> extends  RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Query mRef;
    private Class<T> mModelClass;
    private List<T> mModels;
    private Map<String, T> mModelKeys;
    private List<String> mKeys;
    private ChildEventListener mListener;
    private boolean mSearchingFlag ;


    /**
     * @param mRef        The Firebase location to watch for data changes. Can also be a slice of a location, using some
     *                    combination of <code>limit()</code>, <code>startAt()</code>, and <code>endAt()</code>,
     * @param mModelClass Firebase will marshall the data at a location into an instance of a class that you provide
     */
    public FirebaseListAdapter(Query mRef, final Class<T> mModelClass) {
        this(mRef, mModelClass,false);
    }
    public FirebaseListAdapter(Query mRef, final Class<T> mModelClass,final boolean keepsynced) {
        this.mSearchingFlag=mSearchingFlag;
        this.mRef = mRef;
        this.mModelClass = mModelClass;
        mModels = new ArrayList<T>();
        mModelKeys = new HashMap<String, T>();
        mKeys = new ArrayList<String>();
        mRef.keepSynced(true);
        // Look for all child events. We will then map them to our own internal ArrayList, which backs ListView
        mListener = this.mRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                T model;
                String key;
                try {
                    if(dataSnapshot.hasChild("_source")){
                        model = dataSnapshot.child("_source").getValue(FirebaseListAdapter.this.mModelClass);
                        Log.d("Snapshot:",dataSnapshot.toString());
                    }else {
                        model = dataSnapshot.getValue(FirebaseListAdapter.this.mModelClass);
                    }
                    key = dataSnapshot.hasChild("_id")? dataSnapshot.child("_id").getValue().toString():dataSnapshot.getKey();
                    mModelKeys.put(key, model);
                    // Insert into the correct location, based on previousChildName
                    if (previousChildName == null) {
                        mModels.add(0, model);
                        mKeys.add(0, key);
                    } else {
                        T previousModel = mModelKeys.get(previousChildName);
                        int previousIndex = mModels.indexOf(previousModel);
                        int nextIndex = previousIndex + 1;
                        if (nextIndex == mModels.size()) {
                            mModels.add(model);
                            mKeys.add(key);
                        } else {
                            mModels.add(nextIndex, model);
                            mKeys.add(nextIndex, key);
                        }
                    }
                }catch(Exception e){
                    key = dataSnapshot.hasChild("_id")? dataSnapshot.child("_id").getValue().toString():dataSnapshot.getKey();
                    Log.e("FirebaseListAdapter",key+" has a bad model: "+dataSnapshot.toString());
                }finally {
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // One of the mModels changed. Replace it in our list and name mapping
                String modelName = dataSnapshot.hasChild("_id")? dataSnapshot.child("_id").getValue().toString():dataSnapshot.getKey();
                Log.e("change",s+dataSnapshot.getKey());
                T oldModel = mModelKeys.get(modelName);
                T newModel;
                if(dataSnapshot.hasChild("_source")){
                    newModel = dataSnapshot.child("_source").getValue(FirebaseListAdapter.this.mModelClass);
                }else {
                    newModel = dataSnapshot.getValue(FirebaseListAdapter.this.mModelClass);
                }

                int index = mModels.indexOf(oldModel);
                //Return if model is no longer found
                if(index == -1) return;
                mModels.set(index, newModel);
                mModelKeys.put(modelName, newModel);
                mKeys.set(index, modelName);

                notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild("_id")){
                    Log.e("childReMoved","snap:"+dataSnapshot.getKey());
                    String modelName = dataSnapshot.getKey();
                    // A model was removed from <></>he list. Remove it from our list and the name mapping
                    T oldModel = mModelKeys.get(modelName);
                    mModels.remove(oldModel);
                    mKeys.remove(oldModel);
                    mModelKeys.remove(modelName);
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                if(dataSnapshot.hasChild("_id")){
                    return;
                }
                Log.e("childMoved","snap:"+dataSnapshot.getKey());
                // A model changed position in the list. Update our list accordingly
                String modelName = dataSnapshot.hasChild("_id")? dataSnapshot.child("_id").getValue().toString():dataSnapshot.getKey();
                T oldModel = mModelKeys.get(modelName);

                T newModel;
                if(dataSnapshot.hasChild("_source")){
                    newModel = dataSnapshot.child("_source").getValue(FirebaseListAdapter.this.mModelClass);
                }else {
                    newModel = dataSnapshot.getValue(FirebaseListAdapter.this.mModelClass);
                }
                int index = mModels.indexOf(oldModel);
                //Return if old model is no longer found
                if(index==-1)return;

                mModels.remove(index);
                mKeys.remove(index);
                if (previousChildName == null) {
                    mModels.add(0, newModel);
                    mKeys.add(0, modelName);
                } else {
                    T previousModel = mModelKeys.get(previousChildName);
                    int previousIndex = mModels.indexOf(previousModel);
                    int nextIndex = previousIndex + 1;
                    if (nextIndex == mModels.size()) {
                        mModels.add(newModel);
                        mKeys.add(modelName);
                    } else {
                        mModels.add(nextIndex, newModel);
                        mKeys.add(nextIndex, modelName);
                    }
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseListAdapter", "Listen was cancelled, no more updates will occur");
            }
        });
    }

    public void cleanup() {
        // We're being destroyed, let go of our mListener and forget about all of the mModels
        Log.d("FirebaseListAdapter","cleanup()");
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