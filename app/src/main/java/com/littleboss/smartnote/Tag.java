package com.littleboss.smartnote;

import java.util.LinkedList;
import java.util.List;

public class Tag {
    String name;
    Tag (String name)
    {
        this.name=name;
    }
    public static List<Tag> getTagList(String tagsListString)
    {
        LinkedList<Tag> linkedList=new LinkedList<>();
        String[] tags=tagsListString.split(" ");
        for (String tag:tags)
        {
            if(tag.length()>0)
                linkedList.add(new Tag(tag));
        }
        return linkedList;
    }
}
