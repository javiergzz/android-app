package com.grahm.livepost.util;

/*
* By Jorge E. Hernandez (@lalongooo) 2015
* */

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.grahm.livepost.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/* 
 * This class just handles getting the client since we don't need to have more than
 * one per application
 */
public class Util {
    private static final String TAG = Util.class.getCanonicalName();
    private static AmazonS3Client sS3Client;
    private static CognitoCachingCredentialsProvider sCredProvider;

    public static CognitoCachingCredentialsProvider getCredProvider(Context context) {
        if (sCredProvider == null) {

            sCredProvider = new CognitoCachingCredentialsProvider(
                    context,                    /* get the context for the current activity */
                    GV.COGNITO_POOL_ID,  /* Identity Pool ID */
                    Regions.US_EAST_1           /* Region for your identity pool--US_EAST_1 or EU_WEST_1*/
            );

        }
        return sCredProvider;
    }

    public static String getPrefix(Context context) {
        return System.currentTimeMillis() + "/";
    }

    public static AmazonS3Client getS3Client(Context context) {
        if (sS3Client == null) {
            sS3Client = new AmazonS3Client(getCredProvider(context));
        }
        return sS3Client;
    }

    public static AmazonS3Client getS3ClientIdentifiedByKeys(Context context) {
        if (sS3Client == null) {
            sS3Client = new AmazonS3Client(new BasicAWSCredentials(GV.ACCESS_KEY_ID,GV.SECRET_KEY));
        }
        return sS3Client;
    }

    public static String getFileName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public static boolean doesBucketExist() {
        return sS3Client.doesBucketExist(GV.PICTURE_BUCKET.toLowerCase(Locale.US));
    }

    public static void createBucket() {
        sS3Client.createBucket(GV.PICTURE_BUCKET.toLowerCase(Locale.US));
    }

    public static void deleteBucket() {
        String name = GV.PICTURE_BUCKET.toLowerCase(Locale.US);
        List<S3ObjectSummary> objData = sS3Client.listObjects(name).getObjectSummaries();
        if (objData.size() > 0) {
            DeleteObjectsRequest emptyBucket = new DeleteObjectsRequest(name);
            List<KeyVersion> keyList = new ArrayList<>();
            for (S3ObjectSummary summary : objData) {
                keyList.add(new KeyVersion(summary.getKey()));
            }
            emptyBucket.withKeys(keyList);
            sS3Client.deleteObjects(emptyBucket);
        }
        sS3Client.deleteBucket(name);
    }

    public static boolean createLivePostApplicationFolder() {
        File f = new File(Environment.getExternalStorageDirectory(), File.separator + Config.LIVEPOST_APPLICATION_DIR_NAME);
        f.mkdirs();
        f = new File(Environment.getExternalStorageDirectory(), File.separator + Config.LIVEPOST_APPLICATION_DIR_NAME + Config.LIVEPOST_MEDIA_VIDEO_PATH);
        return f.mkdirs();
    }

    /**
     * Copy the data from the Uri to the specified file name in the application folder. Retrieves the name of the file copied if was successful copied, otherwise null.
     *
     * @param context  The current context.
     * @param uri      The uri to get the data from.
     * @param fileName The name of the new file. The extension is resolved by the MimeTypeMap class.
     */
    public static void copyFile(Context context, Uri uri, String fileName) {

        createLivePostApplicationFolder();

        if (shouldCopy(context, uri)) {
            ContentResolver resolver = context.getContentResolver();
            String destinationFilename = Environment.getExternalStorageDirectory().getPath() + File.separator + Config.LIVEPOST_APPLICATION_DIR_NAME + Config.LIVEPOST_MEDIA_VIDEO_PATH + fileName;
            InputStream in = null;
            FileOutputStream out = null;

            try {
                in = resolver.openInputStream(uri);

                File file = new File(destinationFilename);

                out = new FileOutputStream(file, false);
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
        }
    }

    // TODO: This method needs to handle the mime type and route the new file to the correct folder
    public static String getNewVideoFileName(Context context, Uri uri) {
        String fileExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(context.getContentResolver().getType(uri));
        return
                Config.LIVEPOST_VIDEO_FILES__NAME_PREFIX +
                        Config.LIVEPOST_FILE_NAMES_WORD_SEPARATOR +
                        DateFormat.format(Config.LIVEPOST_VIDEO_FILES_NAME_DATE_FORMAT, new Date()).toString() + "." + fileExtension;
    }

    public static Bitmap createThumbnail(String filePath) {
        Bitmap bitmap = null;

        if (filePath.contains(".mp4")) {
            File file = new File(filePath);
            if (file.exists()) {
                bitmap = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MICRO_KIND);
            } else {
                return null;
            }
        }

        return bitmap;
    }

    public static boolean shouldCopy(Context context, Uri uri) {
        // return !isApplicationMediaFile(getPathFromUri(context, uri));
        return false;
    }

    /**
     * Validates if the specified path matches to the applidation directory path.
     *
     * @param path The path to validate
     * @return True if the specified path contains the application directory path
     */
    public static boolean isApplicationMediaFile(String path) {
        return path.contains(Environment.getExternalStorageDirectory().getPath() + File.separator + Config.LIVEPOST_APPLICATION_DIR_NAME);
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPathFromUri(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * Returns the current date as formatted in the Config.LIVEPOST_DATE_FORMAT public static varible.
     * * @return A formatted String
     */
    public static String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                Config.LIVEPOST_DATE_FORMAT, Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
