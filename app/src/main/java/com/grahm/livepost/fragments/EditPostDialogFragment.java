package com.grahm.livepost.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.text.TextUtilsCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.grahm.livepost.R;
import com.grahm.livepost.adapters.ChatAdapter;
import com.grahm.livepost.objects.Update;
import com.grahm.livepost.util.Utilities;
import com.nostra13.universalimageloader.core.ImageLoader;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.aprilapps.easyphotopicker.EasyImage;

public class EditPostDialogFragment extends DialogFragment  {
    private static final String TAG = "EditPostDialogFragment";
    public static final String TYPE_KEY = "TYPE";
    private Update mMsg;
    private ImageLoader mImageLoader;
    private DatabaseReference mFirebaseRef;
    private String mKey;
    private String mStoryKey;
    private int mChatType = Utilities.MSG_TYPE_TEXT;
    @BindView(R.id.chat_content) public EditText mTextEdit;
    @BindView(R.id.chat_img) public ImageView mImageEdit;
    public static EditPostDialogFragment newInstance(String storyKey,String key, Update m) {
        EditPostDialogFragment f = new EditPostDialogFragment();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putSerializable(ChatAdapter.MSG_KEY, m);
        args.putString(ChatAdapter.KEY_KEY,key);
        args.putString(ChatAdapter.STORY_KEY,storyKey);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageLoader = ImageLoader.getInstance();
    }

    private void restoreState(Bundle savedInstanceState){
        Bundle args = savedInstanceState == null?getArguments():savedInstanceState;
        mStoryKey = args.getString(ChatAdapter.STORY_KEY);
        mKey = args.getString(ChatAdapter.KEY_KEY);
        mMsg = (Update) args.getSerializable(ChatAdapter.MSG_KEY);
        mChatType = args.getInt(TYPE_KEY,Utilities.deduceMessageType(mMsg.getMessage()));
        mFirebaseRef = FirebaseDatabase.getInstance().getReference("updates/"+mStoryKey+"/"+mKey);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_edit_dialog, null);
        ButterKnife.bind(this,v);
        restoreState(savedInstanceState);
        final String message = mMsg!=null?mMsg.getMessage():null;
        if(message!=null && mChatType == Utilities.MSG_TYPE_IMAGE){
            mTextEdit.setVisibility(View.GONE);
            mImageEdit.setVisibility(View.VISIBLE);
            mImageLoader.displayImage(Utilities.cleanUrl(message), mImageEdit);
            mImageEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addPictureCallback();
                }
            });
        }else{
            mImageEdit.setVisibility(View.GONE);
            mTextEdit.setVisibility(View.VISIBLE);
            mTextEdit.setText(message);
            mTextEdit.setSelection(mTextEdit.length());
        }

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(v)
                .setPositiveButton(getString(R.string.edit_confirm), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        editAction();
                    }
                })
                .setNegativeButton(getString(R.string.edit_delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteAction();
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }
    private void editAction(){
        switch (mChatType){
            case Utilities.MSG_TYPE_IMAGE:

                break;
            case Utilities.MSG_TYPE_VIDEO://TODO
                break;
            case Utilities.MSG_TYPE_TEXT:
                String input = mTextEdit.getText().toString();
                if(input!=mMsg.getMessage())
                    mMsg.setMessage(input);
                break;
        }
        mFirebaseRef.setValue(mMsg);
    }

    private void deleteAction(){
        switch (mChatType){
            case Utilities.MSG_TYPE_IMAGE:
                //TODO Remove image from S3
                break;
            case Utilities.MSG_TYPE_VIDEO://TODO
                break;
            default:
                break;
        }
        mFirebaseRef.removeValue();
        Log.i(TAG,"Deleting : "+mFirebaseRef.toString());
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(ChatAdapter.MSG_KEY,mMsg);
        outState.putString(ChatAdapter.KEY_KEY,mKey);
        outState.putString(ChatAdapter.STORY_KEY,mStoryKey);
        outState.putInt(TYPE_KEY,mChatType);
        super.onSaveInstanceState(outState);
    }
        public void addPictureCallback() {
            Log.d("FragmentChatClass","Choosing Image");
            Long l = System.currentTimeMillis()/1000L;
            EasyImage.openChooserWithDocuments(this, mStoryKey+"_"+mKey+"_"+l, 1);
        }

}
