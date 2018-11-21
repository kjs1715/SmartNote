package com.littleboss.smartnote;

import android.content.Context;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataStringParser {
    DataStringParser(){}
    static LBAbstractView parseLabel(String label, Context context)
    {
        Pattern pattern=Pattern.compile("<([^<>]+)>([\\s\\S]*?)</([^<>]+)>");
        Matcher matcher=pattern.matcher(label);
        String type="";
        String content="";
        if(matcher.find())
        {
            type=matcher.group(1);
            content=matcher.group(2);
        }
        if(type.equals("text"))
        {
            return new LBTextView(content,context);
        }
        else if(type.equals("image"))
        {
            return new LBImageView(content,context);
        }
        else if(type.equals("video"))
        {
            return new LBVideoView(content,context);
        }
        else if(type.equals("audio"))
        {
            return new LBAudioView(content,context);
        }
        else
        {
            return new LBTextView(label,context);
        }
    }

    static List<String> parse(String dataString)
    {
        LinkedList<String> linkedList=new LinkedList<>();
        Pattern pattern=Pattern.compile("<([^<>]+)>([\\s\\S]*?)</([^<>]+)>");
        Matcher matcher=pattern.matcher(dataString);
        while(matcher.find())
        {
            linkedList.add(matcher.group(0));
        }
        return linkedList;
    }
}
