package com.littleboss.smartnote;

import java.util.LinkedList;

public class DataAccess {
    public static LinkedList<String> getNotesTitleList()
    {
        LinkedList<String> list=new LinkedList<>();
        list.add(new Notes("testnote1","hahah").title);
        list.add(new Notes("testnote11","hahah3123").title);
        list.add(new Notes("testnote12","hahah3").title);
        list.add(new Notes("testnote13","hahah333").title);
        list.add(new Notes("testnote1321w","hahah").title);
        list.add(new Notes("testnote11","hahah3123").title);
        list.add(new Notes("testnotqrfe12","hahah3").title);
        list.add(new Notes("testnote13","hahah333").title);
        list.add(new Notes("testnodte11","hahah3123").title);
        list.add(new Notes("testno123te12","hahah3").title);
        list.add(new Notes("testnofsdfte13","hahah333").title);
        list.add(new Notes("testnqwete1","hahah").title);
        list.add(new Notes("testnoqwete11","hahah3123").title);
        list.add(new Notes("testnowetqwerqwee12","hahah3").title);
        list.add(new Notes("testnote13","hahah333").title);
        list.add(new Notes("testno123te12","hahah3").title);
        list.add(new Notes("testnofsdfte13","hahah333").title);
        list.add(new Notes("testnqwete1","hahah").title);
        list.add(new Notes("testnoqwete11","hahah3123").title);
        list.add(new Notes("testnowetqwerqwee12","hahah3").title);
        list.add(new Notes("testnote13","hahah333").title);
        list.add(new Notes("testno123te12","hahah3").title);
        list.add(new Notes("testnofsdfte13","hahah333").title);
        list.add(new Notes("testnqwete1","hahah").title);
        list.add(new Notes("testnoqwete11","hahah3123").title);
        list.add(new Notes("testnowetqwerqwee12","hahah3").title);
        list.add(new Notes("testnote13","hahah333").title);
        return list;
    }
    public static void deleteNotesTitleList(LinkedList<String> list)
    {

    }
}
