package com.littleboss.smartnote;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.LinearLayout;

import java.util.Iterator;
import java.util.LinkedList;

public class LBSimpleViewGroup {
    LinearLayout linearLayout;
    LinkedList<LBAbstractView> list;
    LBSimpleViewGroup(@NonNull LinearLayout layout)
    {
        this.linearLayout=layout;
        list=new LinkedList<>();
    }

    LBSimpleViewGroup(@NonNull LinearLayout layout,String viewGroupDataString)
    {
        this.linearLayout=layout;
        list=new LinkedList<>();
        //todo
    }
    void addView(LBAbstractView view, int position) {
        linearLayout.addView((View)view, position);
        list.add(position,view);
    }

    int getSize()
    {
        return list.size();
    }

    void appendView(LBAbstractView view)
    {
        addView(view,getSize());
    }
    LBAbstractView getView(int position)
    {
        return list.get(position);
    }
    void deleteView(int position)
    {
        linearLayout.removeViewAt(position);
        list.remove(position);
    }
    void notifyDataChanged()
    {

    }

    @Override
    public String toString()
    {
        StringBuffer res=new StringBuffer("");
        for(Iterator<LBAbstractView> iterator=list.iterator();iterator.hasNext();)
        {
            res.append(iterator.next().toString());
        }
        return res.toString();
    }


//    void addViewToCursor(LBAbstractView view);
//    void moveViewUp(int position);
//    void moveViewDown(int position);
//    void swapViewPosition(int position1,int position2);

}
