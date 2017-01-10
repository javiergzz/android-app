package com.grahm.livepost.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.grahm.livepost.objects.VideoMessageObject;
import com.grahm.livepost.util.Utilities;

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
    private DatabaseReference mFirebaseRef;
    private PostImageTask mPostTask;
    private String mKey;
    private String mStoryKey;
    private Uri mLoadedUri;
    private int mChatType = Utilities.MSG_TYPE_TEXT;
    @BindView(R.id.chat_content)
    public EditText mTextEdit;
    @BindView(R.id.chat_img)
    public ImageView mImageEdit;

    private AlertDialog mAlertDialog;
    private boolean photoIsChanged = false;

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

    private void restoreState(Bundle savedInstanceState) {
        Bundle args = savedInstanceState == null ? getArguments() : savedInstanceState;
        mStoryKey = args.getString(ChatAdapter.STORY_KEY);
        mKey = args.getString(ChatAdapter.KEY_KEY);
        mMsg = (Update) args.getSerializable(ChatAdapter.MSG_KEY);
        mChatType = args.getInt(TYPE_KEY, Utilities.deduceMessageType(mMsg.getMessage()));
        mFirebaseRef = FirebaseDatabase.getInstance().getReference("updates/" + mStoryKey + "/" + mKey);
        mLoadedUri = new Gson().fromJson(args.getString(IMAGE_URI_KEY), Uri.class);
    }

    private String getText() {
        String str = "";
        switch (mChatType) {
            case Utilities.MSG_TYPE_IMAGE:
            case Utilities.MSG_TYPE_VIDEO:
            case Utilities.MSG_TYPE_VIDEO_W_THUMBNAIL:
                str = getString(R.string.dialog_cancel);
                break;
            default:
                str = getString(R.string.dialog_edit);
                break;
        }
        return str;
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
                .setPositiveButton(getText(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        editAction();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteAction();
                    }
                });

        // Create the AlertDialog object and return it
        mAlertDialog = builder.create();
        return mAlertDialog;
    }

    private void chooseViewElements() {
        final String message = mMsg != null ? mMsg.getMessage() : null;
        if (message == null) return;
        switch (mChatType) {
            case Utilities.MSG_TYPE_IMAGE:
                mTextEdit.setVisibility(View.GONE);
                mImageEdit.setVisibility(View.VISIBLE);
                Glide.with(this).load(Utilities.cleanUrl(message)).into(mImageEdit);
                break;
            case Utilities.MSG_TYPE_VIDEO:
                mTextEdit.setVisibility(View.GONE);
                mImageEdit.setVisibility(View.VISIBLE);
                if (TextUtils.isEmpty(mMsg.getThumb())) {
                    Glide.with(this).load(R.drawable.default_placeholder).into(mImageEdit);
                } else {
                    Glide.with(this).load(mMsg.getThumb()).into(mImageEdit);
                }
                mImageEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {//Do nothing
                    }
                });
                break;
            case Utilities.MSG_TYPE_VIDEO_W_THUMBNAIL:
                VideoMessageObject v = new VideoMessageObject(message);
                mTextEdit.setVisibility(View.GONE);
                mImageEdit.setVisibility(View.VISIBLE);
                if (TextUtils.isEmpty(mMsg.getThumb())) {
                    Glide.with(this).load(R.drawable.default_placeholder).into(mImageEdit);
                } else {
                    Glide.with(this).load(mMsg.getThumb()).into(mImageEdit);
                }
                mImageEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {//Do nothing
                    }
                });
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
            case Utilities.MSG_TYPE_IMAGE: //TODO
            case Utilities.MSG_TYPE_VIDEO:
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
                new DeleteImageTask(getActivity(), deleteUrl);
                break;
            case Utilities.MSG_TYPE_VIDEO://TODO
                break;
            default:
                VideoMessageObject v = new VideoMessageObject(mMsg.getMessage());

                new DeleteVideoTask(getActivity(), v.videoUrl, v.thumbnailUrl);
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
        outState.putString(IMAGE_URI_KEY, new Gson().toJson(mLoadedUri));
        super.onSaveInstanceState(outState);
    }

}