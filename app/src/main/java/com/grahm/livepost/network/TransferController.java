package com.grahm.livepost.network;

/*
* By Jorge E. Hernandez (@lalongooo) 2015
* */

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.amazonaws.mobileconnectors.s3.transfermanager.Download;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.mobileconnectors.s3.transfermanager.Upload;
import com.grahm.livepost.BuildConfig;
import com.grahm.livepost.file.FileUtils;
import com.grahm.livepost.util.Util;
import com.grahm.livepost.util.GV;


import java.io.File;

public class TransferController {

    private static final String TAG = TransferController.class.getCanonicalName();
    private TransferManager mTransferManager;
    private Context context;
    private String mFileName;

    public TransferController(Context context) {
        this.context = context;
        this.mTransferManager = new TransferManager(Util.getCredProvider(context));
    }

    public Upload upload(Uri uri) {
        mFileName = uri.getLastPathSegment();
        File file = new File(uri.getPath());
        Upload upload = null;

        if (file.exists()) {
            upload = mTransferManager.upload(GV.PICTURE_BUCKET, System.currentTimeMillis() + mFileName, file);
        }

        new UploadThumbnailTask().execute(mFileName);
        return upload;
    }

    public void uploadThumbnail(File file) {
        if (file != null && file.exists()) {
            mTransferManager.upload(GV.PICTURE_BUCKET, System.currentTimeMillis() + file.getName(), file);
        }
    }

    public Download downloadThumbnail(String fileName) {
        File file = new File(FileUtils.thumbnailPath, fileName + ".png");
        return mTransferManager.download(GV.PICTURE_BUCKET, System.currentTimeMillis() + fileName + ".png", file);
    }

    public Download downloadVideo(String fileName) {
        File file = new File(FileUtils.videoPath, fileName);
        return mTransferManager.download(GV.PICTURE_BUCKET, System.currentTimeMillis() + fileName, file);
    }

    public String getFileName() {
        return mFileName;
    }

    class UploadThumbnailTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            File file = FileUtils.createThumbnail(params[0]);
            uploadThumbnail(file);
            return null;
        }
    }

    // TODO: Delete the temp file created
}
