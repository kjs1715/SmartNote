package com.littleboss.smartnote;

import android.content.Context;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataStringParser {
    static LBAbstractView parseLabel(String label, Context context)
    {
        Pattern pattern=Pattern.compile("<([^<>]+)>([\\s\\S]*?)</([^<>]+)>");
        Matcher matcher=pattern.matcher(label);
        String type="",content="";
        if(matcher.find())
        {
            type=matcher.group(1);
            content=matcher.group(2);
        }
        if(type.equals("text"))
        {
            LBTextView lbTextView=new LBTextView(content,context);
            return lbTextView;
        }
        //TODO: add other kinds of views
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
