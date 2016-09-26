package com.grahm.livepost.util;

/**
 * Created by javiergonzalez on 8/28/16.
 */

public class Config {
    public static final String ACCESS_KEY_ID = "AKIAJMYYZK5VKLSL7CWQ";
    public static final String SECRET_KEY = "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";
    public static final String PROFILE_PICTURE_BUCKET = "livepostrocks/profile";
    /**
     * The maximum duration of a video that can be uploaded to Sessions
     */
    public static final int LIVEPOST_VIDEO_MAX_DURATION_SECS = 15;

    /**
     * Application root directory. All media files wi'll be stored here.
     */
    public static final String LIVEPOST_APPLICATION_DIR_NAME = "LivePost";

    /**
     * Application folder for video files
     */
    public static final String LIVEPOST_MEDIA_VIDEO_PATH = "/Media/LivePost Video/";

    /**
     * Application folder for video files thumbs
     */
    public static final String LIVEPOST_MEDIA_VIDEO_PATH_THUMBNAIL = "/Media/LivePost Video/Thumbs/";

    /**
     * LivePost video files prefix
     */
    public static final String LIVEPOST_VIDEO_FILES__NAME_PREFIX = "VID";

    /**
     * LivePost file names word separator
     */
    public static final String LIVEPOST_FILE_NAMES_WORD_SEPARATOR = "-";

    /**
     * LivePost video files name date format
     */
    public static final String LIVEPOST_VIDEO_FILES_NAME_DATE_FORMAT = "yyyyMMdd-HHmmss";

    /**
     * Date format used for the LivePost application
     */
    public static final String LIVEPOST_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * This variable helps to identify if some opened file belongs to the WhatsApp application folder. If this is the case, won't try to compress, simple upload it.
     */
    public static final String WHATSAPP_APP_NAME = "WhatsApp";

    /**
     * Max size (bytes) of the files to be attached.
     */
    public static final int MAX_UPLOAD_FILE_SIZE = 16777216;
}
