package com.grahm.livepost.objects;

import android.view.ViewGroup;

/**
 * Created by Vyz on 2016-02-26.
 */
public abstract class MultipartFormField {
    public abstract int getTitle();

    public abstract int getLayout();

    public abstract boolean onValidate();

    public void onSetup(ViewGroup layout) {
    }
}
