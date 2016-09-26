package com.grahm.livepost.file;

/*
* By Jorge E. Hernandez (@lalongooo) 2015
* */

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.grahm.livepost.util.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    public static final String videoPath =
            Environment.getExternalStorageDirectory().getPath()
                    + File.separator
                    + Config.LIVEPOST_APPLICATION_DIR_NAME
                    + Config.LIVEPOST_MEDIA_VIDEO_PATH;
    public static final String thumbnailPath =
            Environment.getExternalStorageDirectory().getPath()
                    + File.separator
                    + Config.LIVEPOST_APPLICATION_DIR_NAME
                    + Config.LIVEPOST_MEDIA_VIDEO_PATH_THUMBNAIL;
    private static final String TAG = "FileUtils";

    public static String getNewFileName(Context context, Uri uri) {
        String fileExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(context.getContentResolver().getType(uri));
        return DateFormat.format(Config.LIVEPOST_VIDEO_FILES_NAME_DATE_FORMAT, new java.util.Date()).toString() + "." + fileExtension;
    }

    public static File copy(Context context, Uri uri, String fileName) {

        File mFile = null;
        ContentResolver resolver = context.getContentResolver();
        InputStream in = null;
        FileOutputStream out = null;

        try {
            in = resolver.openInputStream(uri);

            mFile = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + Config.LIVEPOST_APPLICATION_DIR_NAME + Config.LIVEPOST_MEDIA_VIDEO_PATH, fileName);
            out = new FileOutputStream(mFile, false);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        } catch (IOException e) {
            Log.e(TAG, "", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, "", e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Log.e(TAG, "", e);
                }
            }
        }
        createThumbnail(fileName);
        return mFile;
    }

    public static File move(Uri uri) {

        InputStream in = null;
        OutputStream out = null;
        String outputPath = Environment.getExternalStorageDirectory()
                + File.separator
                + Config.LIVEPOST_APPLICATION_DIR_NAME
                + Config.LIVEPOST_MEDIA_VIDEO_PATH;
        try {


            //create output directory if it doesn't exist
            File dir = new File(outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }


            in = new FileInputStream(uri.getPath());
            out = new FileOutputStream(outputPath + uri.getLastPathSegment());

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file
            new File(uri.getPath()).delete();


        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

        return new File(outputPath + uri.getLastPathSegment());
    }

    public static File copyToCacheDir(Context context, Uri uri, String fileName) {
        Log.e(TAG, "Start copy to cache");
        File mFile = null;
        ContentResolver resolver = context.getContentResolver();
        InputStream in = null;
        FileOutputStream out = null;

        try {
            try {
                in = resolver.openInputStream(uri);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Error while getting the InputStream");
                e.printStackTrace();
            }

            mFile = new File(context.getCacheDir(), fileName);
            out = new FileOutputStream(mFile, false);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, "", e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Log.e(TAG, "", e);
                }
            }
        }
        Log.e(TAG, "Finish copy to cache");
        return mFile;
    }

    public static File createThumbnail(String fileName) {
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(
                videoPath + fileName
                , MediaStore.Video.Thumbnails.MINI_KIND
        );

        File file = new File(thumbnailPath, fileName + ".png");
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, fOut);
            fOut.flush();
            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    public static Bitmap getBitmap(String fileName) {

        Bitmap bitmap;
        File thumb = new File(thumbnailPath, fileName + ".png");
        if (thumb.exists()) {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bitmap = BitmapFactory.decodeFile(thumb.getAbsolutePath(), bmOptions);
        } else {

            thumb = new File(videoPath, fileName);
            if (thumb.exists()) {
                bitmap = ThumbnailUtils.createVideoThumbnail(
                        videoPath + fileName
                        , MediaStore.Video.Thumbnails.MINI_KIND
                );
                createThumbnail(fileName);
            } else {
                bitmap = null;
            }
        }

        return bitmap;
    }

    public static void createLivePostApplicationFolder() {
        File f = new File(Environment.getExternalStorageDirectory(), File.separator + Config.LIVEPOST_APPLICATION_DIR_NAME);
        f.mkdirs();
        f = new File(Environment.getExternalStorageDirectory(), File.separator + Config.LIVEPOST_APPLICATION_DIR_NAME + Config.LIVEPOST_MEDIA_VIDEO_PATH);
        f.mkdirs();
        f = new File(Environment.getExternalStorageDirectory(), File.separator + Config.LIVEPOST_APPLICATION_DIR_NAME + Config.LIVEPOST_MEDIA_VIDEO_PATH_THUMBNAIL);
        f.mkdirs();
    }

    public interface FileTaskListener {
        void onFinish(File file);
    }

    public static class CopyTask extends AsyncTask<Void, Void, File> {

        private Context context;
        private Uri uri;
        private FileTaskListener listener;

        public CopyTask(Context context, Uri uri, FileTaskListener listener) {
            this.context = context;
            this.uri = uri;
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected File doInBackground(Void... voids) {
            File file = copyToCacheDir(context, uri, getNewFileName(context, uri));
            return file;
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            listener.onFinish(file);
        }
    }

    public static class MoveTask extends AsyncTask<Void, Void, File> {

        private Uri uri;
        private FileTaskListener listener;

        public MoveTask(Uri uri, FileTaskListener listener) {
            this.uri = uri;
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected File doInBackground(Void... voids) {
            return move(uri);
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            listener.onFinish(file);
        }
    }
}
