package com.grahm.livepost.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.grahm.livepost.R;
import com.grahm.livepost.activities.MainActivity;
import com.grahm.livepost.adapters.ChatAdapter;
import com.grahm.livepost.asynctask.PostImageTask;
import com.grahm.livepost.interfaces.OnFragmentInteractionListener;
import com.grahm.livepost.interfaces.OnPutImageListener;
import com.grahm.livepost.objects.Update;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.util.Utilities;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class FragmentChatClass extends Fragment implements AbsListView.OnItemClickListener {

    private static final String TAG_CLASS = "FragmentChatClass";
    public static final String TAG_AUTHOR = "author";
    public static final String TAG_ID = "key";
    public static final String TAG_USER = "user";
    private static final int TAKE_PICTURE = 0;
    private static final int PHOTO_SELECTED = 1;

    @BindView(R.id.messageInput)
    EditText mInputText;
    @BindView(R.id.msg_list)
    RecyclerView mListView;
    public static MainActivity.FragmentsEnum page = MainActivity.FragmentsEnum.CHAT;
    private DatabaseReference mFirebaseRef;
    private PostImageTask mPostTask;
    private String mId, mAuthor;
    private OnFragmentInteractionListener mListener;
    private ValueEventListener mConnectedListener;
    private Uri mIimageUri;
    private OnPutImageListener putImageListener;
    private ChatAdapter mMessagesListAdapter;
    private User mUser;

    public static FragmentChatClass newInstance() {
        FragmentChatClass fragment = new FragmentChatClass();
        return fragment;
    }

    public static FragmentChatClass newInstance(Bundle args) {
        FragmentChatClass fragment = new FragmentChatClass();
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentChatClass() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(TAG_ID, mId);
        outState.putString(TAG_AUTHOR, mAuthor);
        super.onSaveInstanceState(outState);
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null){
            mId = savedInstanceState.getString(TAG_ID);
            mAuthor = savedInstanceState.getString(TAG_AUTHOR);
        } else if (getArguments() != null) {
            mId = getArguments().getString(TAG_ID);
            mAuthor = getArguments().getString(TAG_AUTHOR);
        }
        EasyImage.configuration(getActivity())
                .setImagesFolderName("images")
                .saveInAppExternalFilesDir()
                .setCopyExistingPicturesToPublicLocation(true);
        mFirebaseRef = FirebaseDatabase.getInstance().getReference("updates").child(mId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);
        ButterKnife.bind(this, view);

        putImageListener = new OnPutImageListener() {
            @Override
            public void onSuccess(String url) {
                mUser = Utilities.getUser(mFirebaseRef,getActivity(),savedInstanceState);
                mFirebaseRef.getRoot().child("posts/" + mId + "/last_message").setValue(url);
                Update m = new Update(0,null,url,mUser.getProfile_picture(), mUser.getName(), mUser.getEmail());
                // Create a new, auto-generated child of that chat location, and save our chat data there
                DatabaseReference r = mFirebaseRef.push();
                r.setValue(m);
                r.child("timestamp").setValue(ServerValue.TIMESTAMP);
            }
        };
        return view;
    }

    @OnEditorAction(R.id.messageInput)
    public boolean editorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            sendMessage();
        }
        return true;
    }

    @OnClick(R.id.btnAddPicture)
    public void addPictureCallback() {
        Log.d("FragmentChatClass","Choosing Image");
        Long l = System.currentTimeMillis()/1000L;
        EasyImage.openChooserWithDocuments(this, mId+"_"+l, 1);
    }


    @Override
    public void onStart() {
        super.onStart();
        // Setup our view and list adapter. Ensure it scrolls to the bottom as data changes
        SharedPreferences prefs = getActivity().getSharedPreferences("ChatPrefs", 0);
        Gson gson = new Gson();
        String storedUsrJson = prefs.getString("user", null);
        if (TextUtils.isEmpty(storedUsrJson))
            mUser = null;
        else
            mUser = gson.fromJson(storedUsrJson, User.class);

        LinearLayout messageBar = (LinearLayout) getView().findViewById(R.id.listFooter);

        try {
            LinearLayoutManager llm = new LinearLayoutManager(getActivity());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            mListView.setLayoutManager(llm);
            mMessagesListAdapter = new ChatAdapter(mFirebaseRef.limitToLast(50), getActivity(), mId, mUser);
            mListView.setAdapter(mMessagesListAdapter);
            mMessagesListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    mListView.scrollToPosition(mMessagesListAdapter.getItemCount() - 1);
                }
            });
        } catch (Exception e) {
            Log.e(TAG_CLASS, "Bad adapter:"+e.toString());
        }
        // Finally, a little indication of connection status
        mConnectedListener = mFirebaseRef.getRoot().child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = (Boolean) dataSnapshot.getValue();
                if (connected) {
                    Log.i(TAG_CLASS, "Connected to Firebase");
                } else {
                    Log.i(TAG_CLASS, "Disconnected from Firebase");
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                // No-op
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mFirebaseRef.getRoot().child(".info/connected").removeEventListener(mConnectedListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EasyImage.handleActivityResult(requestCode, resultCode, data, getActivity(), new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                Toast toast = Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT);
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                //Handle the image
                onPhotoReturned(imageFile);
            }
        });
    }

    private void onPhotoReturned(File imageFile){
        Uri uri = Uri.fromFile(imageFile);

        mPostTask = new PostImageTask(getActivity(),putImageListener, "",true);
        if(uri!= null) mPostTask.execute(uri);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @OnClick(R.id.btnSend)
    public void sendMessage() {
        final EditText inputText = (EditText) getView().findViewById(R.id.messageInput);
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String input = inputText.getText().toString();
        Gson gson = new Gson();
        String storedUsrJson = sharedPref.getString("user", null);
        SharedPreferences.Editor SPEdit = sharedPref.edit();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mUser = Utilities.getUser(mFirebaseRef,getContext(),null);
            if (!TextUtils.isEmpty(input)) {
                //TODO
                mFirebaseRef.getRoot().child("posts/" + mId + "/last_message").setValue(input);
                String str = mUser.getProfile_picture()==null?"":mUser.getProfile_picture();
                String[] parts = str.split("\\?");

                Update m = new Update(0,null,input,parts[0], mUser.getName(), mUser.getEmail());
                // Create a new, auto-generated child of that chat location, and save our chat data there
                DatabaseReference r = mFirebaseRef.push();
                r.setValue(m);
                r.child("timestamp").setValue(ServerValue.TIMESTAMP);
                //Update Shared Preferences
                Map<String,Object> map = mUser.getPosts_contributed_to()==null?new HashMap<String,Object>():mUser.getPosts_contributed_to();
                map.put(mId, true);
                mUser.setPosts_contributed_to(map);
                SPEdit.putString(new Gson().toJson(mUser), "user");
                SPEdit.commit();

                mFirebaseRef.getRoot().child("users/"+uid+"/posts_contributed").updateChildren(mUser.getPosts_contributed_to());
                inputText.setText("");
            }

    }

}