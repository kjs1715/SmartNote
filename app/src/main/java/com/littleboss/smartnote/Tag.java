package com.littleboss.smartnote;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Tag implements Comparable<Tag> {
    String name;
    Tag (String name)
    {
        this.name=name.trim();
    }
    public static List<Tag> getTagList(String tagsListString)
    {
        if(tagsListString==null||tagsListString.length()==0)
        {
            return new LinkedList<>();
        }
        LinkedList<Tag> linkedList=new LinkedList<>();
//        System.out.println(tagsListString);
        String[] tags=tagsListString.split(" ");
        for (String tag:tags)
        {
            if(tag.length()>0)
                linkedList.add(new Tag(tag));
        }
        return linkedList;
    }
    public static String getTagListString(Collection<Tag> collection)
    {
        if(collection==null || collection.size()==0)
            return "";
        StringBuilder res=new StringBuilder("");
        for (Tag tag:collection)
        {
            res.append(tag.name);
            res.append(" ");
        }
        System.out.println("getTagListString = "+res.toString());
        return res.toString();
    }
    public static View getTextView(Context context, Tag tag)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        ConstraintLayout constraintLayout=new ConstraintLayout(context);
        inflater.inflate(R.layout.textview, constraintLayout);
        ((TextView)((ConstraintLayout)constraintLayout.getChildAt(0)).getChildAt(0)).setText(tag.name);
        return constraintLayout;
    }

    @Override
    public int compareTo(@NonNull Tag other)
    {
        return this.name.compareTo(other.name);
    }
    public static List<Tag> removeDuplicate(List<Tag> list) {
        List<Tag> listTemp = new ArrayList();
        for(int i=0;i<list.size();i++){
            if(!listTemp.contains(list.get(i))){
                listTemp.add(list.get(i));
            }
        }
        return listTemp;
    }

    @Override
    public boolean equals(Object other)
    {
        return other instanceof Tag && ((Tag)other).name.equals(this.name);
    }
}
