package com.grahm.livepost.specialViews;

import android.app.ProgressDialog;
import android.content.Context;

public class CustomProgressDialog extends ProgressDialog {

    public CustomProgressDialog(Context context) {
        super(context);
    }

    public void cancel(){
        this.dismiss();
    }
}