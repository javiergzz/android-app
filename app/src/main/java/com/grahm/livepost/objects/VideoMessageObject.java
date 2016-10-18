package com.grahm.livepost.objects;

import com.grahm.livepost.util.Utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Vyz on 2016-10-18.
 */

public class VideoMessageObject {
    public static final Pattern videoMessagePattern = Pattern.compile("^\\<video\\>(.+)\\<\\/video\\>\\<thumb\\>(.+)\\<\\/thumb\\>");
    public final String videoUrl;
    public final String thumbnailUrl;
    public final Matcher matcher;
    public VideoMessageObject(final String message){
        matcher = videoMessagePattern.matcher(message);
        if(matcher.matches()){
            videoUrl = matcher.group(1);
            thumbnailUrl = matcher.group(2);
        }else{
            videoUrl = Utilities.cleanUrl(message);
            thumbnailUrl = null;
        }
    }
}
