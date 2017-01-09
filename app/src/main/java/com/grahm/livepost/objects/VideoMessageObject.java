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

    public static String[] parserXMLVideo(String message){
        String[] str = new String[2];
        Matcher _matcher = videoMessagePattern.matcher(message);
        if(_matcher.matches()){
            str[0] = _matcher.group(1);
            str[1] = _matcher.group(2);
        }else{
            str[0] = Utilities.cleanUrl(message);
            str[1] = null;
        }
        return str;
    }
}
