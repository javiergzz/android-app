package com.grahm.livepost.ui;

import android.content.Context;
import com.grahm.livepost.specialViews.CustomProgressDialog;


public class Controls {

    private static CustomProgressDialog dialog;

    public static void createDialog(Context context, String message, boolean cancelable){
        dialog = new CustomProgressDialog(context);
        dialog.setMessage(message);
        dialog.setCancelable(cancelable);
        dialog.show();
    }

    public static void dismissDialog(){
        if(dialog != null){
            dialog.cancel();
        }
    }

    public static void setDialogMessage(String message){
        dialog.setMessage(message);
    }
}
