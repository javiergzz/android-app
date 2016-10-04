package com.grahm.livepost.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.grahm.livepost.R;
import com.grahm.livepost.adapters.ChatAdapter;
import com.grahm.livepost.asynctask.DeleteImageTask;
import com.grahm.livepost.asynctask.DeleteVideoTask;
import com.grahm.livepost.asynctask.PostImageTask;
import com.grahm.livepost.interfaces.OnPutImageListener;
import com.grahm.livepost.objects.Update;
import com.grahm.livepost.util.GV;
import com.grahm.livepost.util.Util;
import com.grahm.livepost.util.Utilities;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class EditPostDialogFragment extends DialogFragment {
    private static final String TAG = "EditPostDialogFragment";
    public static final String TYPE_KEY = "TYPE";
    public static final String IMAGE_URI_KEY = "img_uri";
    private Update mMsg;
    private ImageLoader mImageLoader;
    private DatabaseReference mFirebaseRef;
    private AmazonS3Client mS3client;
    private PostImageTask mPostTask;
    private String mKey;
    private String mStoryKey;
    private Uri mLoadedUri;
    private int mChatType = Utilities.MSG_TYPE_TEXT;
    @BindView(R.id.chat_content)
    public EditText mTextEdit;
    @BindView(R.id.chat_img)
    public ImageView mImageEdit;
    private OnPutImageListener putImageListener = new OnPutImageListener() {
        @Override
        public void onSuccess(String url) {
            mMsg.setMessage(url);
            mFirebaseRef.child(Update.MESSAGE_FIELD_STR).setValue(url);
        }
    };
    public static EditPostDialogFragment newInstance(String storyKey, String key, Update m) {
        EditPostDialogFragment f = new EditPostDialogFragment();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putSerializable(ChatAdapter.MSG_KEY, m);
        args.putString(ChatAdapter.KEY_KEY, key);
        args.putString(ChatAdapter.STORY_KEY, storyKey);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageLoader = ImageLoader.getInstance();
    }

    private void restoreState(Bundle savedInstanceState) {
        Bundle args = savedInstanceState == null ? getArguments() : savedInstanceState;
        mStoryKey = args.getString(ChatAdapter.STORY_KEY);
        mKey = args.getString(ChatAdapter.KEY_KEY);
        mMsg = (Update) args.getSerializable(ChatAdapter.MSG_KEY);
        mChatType = args.getInt(TYPE_KEY, Utilities.deduceMessageType(mMsg.getMessage()));
        mFirebaseRef = FirebaseDatabase.getInstance().getReference("updates/" + mStoryKey + "/" + mKey);
        mS3client = new AmazonS3Client(new BasicAWSCredentials(GV.ACCESS_KEY_ID, GV.SECRET_KEY));
        mLoadedUri = new Gson().fromJson(args.getString(IMAGE_URI_KEY),Uri.class);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_edit_dialog, null);
        ButterKnife.bind(this, v);
        restoreState(savedInstanceState);
        chooseViewElements();

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
    private void chooseViewElements(){
        final String message = mMsg != null ? mMsg.getMessage() : null;
        if (message == null) return;

        switch (mChatType) {
            case Utilities.MSG_TYPE_IMAGE:
                mTextEdit.setVisibility(View.GONE);
                mImageEdit.setVisibility(View.VISIBLE);
                mImageLoader.displayImage(Utilities.cleanUrl(message), mImageEdit);
                mImageEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG,"Add picture callback");
                        addPictureCallback();
                    }
                });
                break;
            case Utilities.MSG_TYPE_VIDEO://TODO
                mTextEdit.setVisibility(View.GONE);
                mImageEdit.setVisibility(View.VISIBLE);
                mImageLoader.displayImage(Utilities.cleanVideoUrl(message).replace(".mp4",".png"), mImageEdit);
                mImageEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {//Do nothing
                    }
                });
                mImageEdit.setVisibility(View.GONE);
                break;
            default:
                mImageEdit.setVisibility(View.GONE);
                mTextEdit.setVisibility(View.VISIBLE);
                mTextEdit.setText(message);
                mTextEdit.setSelection(mTextEdit.length());
                break;
        }
    }
    private void editAction() {
        switch (mChatType) {
            case Utilities.MSG_TYPE_IMAGE:
                mPostTask = new PostImageTask(getActivity(),mS3client,putImageListener,true);
                if(mLoadedUri!= null) mPostTask.execute(mLoadedUri);
                break;
            case Utilities.MSG_TYPE_VIDEO://TODO

                break;
            case Utilities.MSG_TYPE_TEXT:
                String input = mTextEdit.getText().toString();
                if (input != mMsg.getMessage())
                    mMsg.setMessage(input);
                mFirebaseRef.setValue(mMsg);
                break;
        }

    }

    private void deleteAction() {
        switch (mChatType) {
            case Utilities.MSG_TYPE_IMAGE:
                String deleteUrl = Utilities.cleanUrl(mMsg.getMessage());
                new DeleteImageTask(getActivity(),mS3client,deleteUrl);
                break;
            case Utilities.MSG_TYPE_VIDEO://TODO
                break;
            default:
                String deleteVideoUrl = Utilities.cleanVideoUrl(mMsg.getMessage());
                new DeleteVideoTask(getActivity(),mS3client,deleteVideoUrl);
                break;
        }
        mFirebaseRef.removeValue();
        Log.i(TAG, "Deleting : " + mFirebaseRef.toString());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(ChatAdapter.MSG_KEY, mMsg);
        outState.putString(ChatAdapter.KEY_KEY, mKey);
        outState.putString(ChatAdapter.STORY_KEY, mStoryKey);
        outState.putInt(TYPE_KEY, mChatType);
        outState.putString(IMAGE_URI_KEY,new Gson().toJson(mLoadedUri));
        super.onSaveInstanceState(outState);
    }

    public void addPictureCallback() {
        Log.d("FragmentChatClass", "Choosing Image");
        Long l = System.currentTimeMillis() / 1000L;
        EasyImage.openChooserWithDocuments(this, mStoryKey + "_" + mKey + "_" + l, 1);
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
        mLoadedUri = Uri.fromFile(imageFile);
        mImageLoader.displayImage(mLoadedUri.toString(),mImageEdit);
    }

}
