package com.grahm.livepost.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.grahm.livepost.R;
import com.grahm.livepost.objects.Update;
import com.nostra13.universalimageloader.core.ImageLoader;

public class EditPostDialogFragment extends DialogFragment {
    public static final String MSG_KEY = "msg";
    private Update mMsg;
    private ImageLoader mImageLoader;

    public static EditPostDialogFragment newInstance(Update m) {
        EditPostDialogFragment f = new EditPostDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putSerializable(MSG_KEY, m);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMsg = (Update)getArguments().getSerializable(MSG_KEY);
        mImageLoader = ImageLoader.getInstance();
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_edit_dialog, null);


        ImageButton imgButton = (ImageButton)v.findViewById(R.id.img);
        EditText text = (EditText)v.findViewById(R.id.content);
        String message = mMsg.getMessage();
        if(message.contains(".png")||message.contains(".jpg")){
            text.setVisibility(View.GONE);
            mImageLoader.displayImage(message, (ImageView) imgButton);
            imgButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }else{
            imgButton.setVisibility(View.GONE);
            text.setText(mMsg.getMessage());
            text.setSelection(text.length());
        }

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(v)
                .setPositiveButton(getString(R.string.edit_confirm), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                    }
                })
                .setNegativeButton(getString(R.string.edit_delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(MSG_KEY,mMsg);
        super.onSaveInstanceState(outState);
    }
}
