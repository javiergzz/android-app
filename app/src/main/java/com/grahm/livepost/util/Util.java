package com.grahm.livepost.util;


import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/*
 * This class just handles getting the client since we don't need to have more than
 * one per application
 */
public class Util {
    private static final String TAG = Util.class.getCanonicalName();

    public static String getPrefix(Context context) {
        return System.currentTimeMillis() + "/";
    }

    public static String getFileName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
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

//    public static void setupFfmpeg(Context context) {
//        FFmpeg ffmpeg = FFmpeg.getInstance(context);
//        try {
//            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
//
//                @Override
//                public void onStart() {
//                }
//
//                @Override
//                public void onFailure() {
//                }
//
//                @Override
//                public void onSuccess() {
//                }
//
//                @Override
//                public void onFinish() {
//                }
//            });
//        } catch (FFmpegNotSupportedException e) {
//            // Handle if FFmpeg is not supported by device
//            Log.e(TAG, e.getMessage());
//        }
//    }

    public static Intent buildIntent(Context context, String chooserTitle, int type) {
        try {
            Uri outputFileUri = createCameraPictureFile(context);

            ArrayList cameraIntents = new ArrayList();
            Intent captureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
            Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            PackageManager packageManager = context.getPackageManager();
            List camList = packageManager.queryIntentActivities(captureIntent, 0);
            Iterator galleryIntent = camList.iterator();

            while (galleryIntent.hasNext()) {
                ResolveInfo chooserIntent = (ResolveInfo) galleryIntent.next();
                String packageName = chooserIntent.activityInfo.packageName;
                Intent intent = new Intent(captureIntent);
                intent.setComponent(new ComponentName(chooserIntent.activityInfo.packageName, chooserIntent.activityInfo.name));
                intent.setPackage(packageName);
                intent.putExtra("output", outputFileUri);
                cameraIntents.add(intent);
                intent = new Intent(videoIntent);
                cameraIntents.add(intent);
            }

            Intent galleryIntent1;
            galleryIntent1 = createDocumentsIntent(context, type);


            Intent chooserIntent1 = Intent.createChooser(galleryIntent1, chooserTitle);
            chooserIntent1.putExtra("android.intent.extra.INITIAL_INTENTS", (Parcelable[]) cameraIntents.toArray(new Parcelable[cameraIntents.size()]));
            return chooserIntent1;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }

    }

    private static Intent createDocumentsIntent(Context context, int type) {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        return intent;
    }

    private static Uri createCameraPictureFile(Context context) throws IOException {
        File imagePath = getCameraPicturesLocation(context);
        Uri uri = Uri.fromFile(imagePath);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString("pl.aprilapps.easyphotopicker.photo_uri", uri.toString());
        editor.putString("pl.aprilapps.easyphotopicker.last_photo", imagePath.toString());
        editor.apply();
        return uri;
    }

    public static File getCameraPicturesLocation(Context context) throws IOException {
        String localdir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
        File dir = new File(localdir, "EasyImage");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File imageFile = File.createTempFile(UUID.randomUUID().toString(), ".jpg", dir);
        return imageFile;
    }

    public static File pickedExistingPicture(Context context, Uri photoUri) throws IOException {
        InputStream pictureInputStream = context.getContentResolver().openInputStream(photoUri);
        String localdir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
        File directory = new File(localdir, "EasyImage");
        File photoFile = new File(directory, UUID.randomUUID().toString() + "." + getMimeType(context, photoUri));
        photoFile.createNewFile();
        writeToFile(pictureInputStream, photoFile);
        return photoFile;
    }


    private static File takenCameraPicture(Context context) throws IOException, URISyntaxException {
        URI imageUri = new URI(PreferenceManager.getDefaultSharedPreferences(context).getString("pl.aprilapps.easyphotopicker.photo_uri", (String) null));
        notifyGallery(context, imageUri);
        return new File(imageUri);
    }

    private static void notifyGallery(Context context, URI pictureUri) throws URISyntaxException {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(pictureUri);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    public static String getMimeType(Context context, Uri uri) {
        String extension;
        if (uri.getScheme().equals("content")) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }

        return extension;
    }

    public static String getMimeTypeFromUri(Context context, Uri uri) {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String mimeType = mime.getMimeTypeFromExtension(getMimeType(context, uri));
        return mimeType;
    }

    public static String getExtensionFromUri(Context context, Uri uri) {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
    }


    public static String getMimeTypeFromUrl(String url) {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
    }


    public static void writeToFile(InputStream in, File file) {
        try {
            FileOutputStream e = new FileOutputStream(file);
            byte[] buf = new byte[1024];

            int len;
            while ((len = in.read(buf)) > 0) {
                e.write(buf, 0, len);
            }

            e.close();
            in.close();
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

    public static Bitmap loadBitmapFromUri(Context context, Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } catch (Exception e) {
            if (e != null)
                Log.e(TAG, e.getMessage());
        }
        return bitmap;
    }

}