package com.littleboss.smartnote;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.littleboss.smartnote.Utils.DateUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

class NoteNotExistException extends Exception {
    public NoteNotExistException(String msg) {
        super(msg);
    }
}
class SameTitleNoteExistedException extends Exception {
    public SameTitleNoteExistedException(String msg) {
        super(msg);
    }
}

public class NoteDatabase {
    static NoteDatabase instance = null;
    static SQLiteDatabase db;
    static String noteDatabasePath = "data/data/com.littleboss.smartnote/app_databases/data.db";

    //
    public static void dropDatabaseIfExist() {
        File file = new File(noteDatabasePath);
        if(file.exists()) {
            file.delete();
        }
    }

    private NoteDatabase() {
        new File(noteDatabasePath).getParentFile().mkdirs();
        db = SQLiteDatabase.openOrCreateDatabase(noteDatabasePath, null);
        try {
            db.execSQL("create table catagories (_id integer primary key autoincrement, catagory text);");
            db.execSQL("create table notes (_id integer primary key autoincrement, title text, create_time text, modify_time text, content text, catagory_list text);");
        }
        catch (Exception e) {
            Log.i("err NoteDatabase() : ", e.toString());
        }
    }

    /**
     * 使用单例模式，返回数据库接口的单例。
     *
     * @return 返回数据库接口单例。
     */
    public static NoteDatabase getInstance() {
        if (instance == null)
            instance = new NoteDatabase();
        return instance;
    }

    /**
     * 根据笔记标题获取笔记内容。
     *
     * @param title 笔记标题
     * @return 标题恰好与输入字符串相同的笔记内容，若为空笔记或不存在返回空字符串
     */
    public String getNotesByTitle(String title) {
        Cursor cursor = db.rawQuery("select * from notes where title = ?;", new String[] {title});
        if (cursor == null || cursor.getCount() == 0) {
            return "";
        }
        cursor.moveToFirst();
        String content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
        return content;
    }

    /**
     * 将一条新的笔记存入数据库，或者修改一条已经存在的笔记。
     *
     * @param oldTitle 待修改的笔记题目，若为空则视为将一条新的笔记存入数据库。
     * @param newTitle 新笔记的标题，或者待修改的笔记的新标题。
     * @param content  新笔记的内容，或者待修改的笔记将被改成的内容。
     */
    public void saveNoteByTitle(@NonNull String oldTitle, @Nullable String newTitle, @Nullable String content, @Nullable String tagsListString) throws NoteNotExistException, SameTitleNoteExistedException {
        if(tagsListString!=null)
        {
            Cursor cursor = null;
            if (!oldTitle.equals("")) {
                cursor = db.rawQuery("select * from notes where title = ?;", new String[]{oldTitle});
                if (cursor != null && cursor.getCount() > 0) {
                    db.execSQL(
                            "update notes set catagory_list = ? where title = ?;",
                            new String[]{tagsListString, oldTitle}
                    );
                }
            }
        }
        else
        {
            Cursor cursor;
            String modify_time;
            if (!oldTitle.equals("")) {
                cursor = db.rawQuery("select * from notes where title = ?;", new String[]{oldTitle});
                if (cursor != null && cursor.getCount() > 0)
                {
                    modify_time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    db.execSQL(
                            "update notes set modify_time = ?, content = ?, title = ? where title = ?;",
                            new String[] {modify_time, content, newTitle, oldTitle}
                    );
                }
            }
            else
            {
                String create_time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                modify_time = create_time;
                if (newTitle.equals(""))
                    newTitle = "note @ " + create_time;
                cursor = db.rawQuery("select * from notes where title = ?;", new String[]{newTitle});
                cursor.close();
                db.execSQL(
                        "insert into notes (title, create_time, modify_time, content, catagory_list) values (?, ?, ?, ?, ?);",
                        new String[]{newTitle, create_time, modify_time, content, ""}
                );
            }
        }
    }

    /**
     * Sets test mode.
     * level >= 1
     */
    public void setTestMod(int level) {
        db.execSQL("create table testMod (_id integer primary key autoincrement, mod integer);");
        Cursor cursor = null;
        cursor = db.rawQuery("select * from testMod", new String[]{});
        if (cursor == null || cursor.getCount() == 0)
            db.execSQL("insert into testMod (mod) values (?);", new String[] {String.valueOf(level)});
        else
            db.execSQL(
                    "update testMod set mod = ? where _id=1;", new String[] {String.valueOf(level)}
            );

    }

    /**
     * Gets test mod.
     *
     * @return the test mod or -1
     */
    public int getTestMod() {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from testMod", new String[]{});
        }
        catch (Exception e) {
            //e.printStackTrace();
            Log.i("err getTestMod() : ", e.toString());
        }
        if (cursor == null || cursor.getCount() == 0)
            return -1;
        cursor.moveToFirst();
        return cursor.getInt(cursor.getColumnIndexOrThrow("mod"));
    }

    /**
     * Close connection.
     */
    public void closeConnection() {
        db.close();
        instance = null;
    }
    /**
     * 获取所有笔记的标题列表。
     *
     * @return 所有笔记的标题列表。若不存在任何笔记，则返回null。
     */
    public LinkedList<ListData> getNotesTitleList() {
        Cursor cursor = db.rawQuery("select * from notes;", null);
        if(cursor == null ) {
            return new LinkedList<>();
        }
        if (cursor.getCount() == 0) {
            cursor.close();

            return new LinkedList<>();
        }
        cursor.moveToFirst();
        LinkedList<ListData> titleList = new LinkedList();
        while (true) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String create_string = cursor.getString(cursor.getColumnIndexOrThrow("create_time"));
            String modify_string = cursor.getString(cursor.getColumnIndexOrThrow("modify_time"));
            String tagListString = cursor.getString(cursor.getColumnIndexOrThrow("catagory_list"));
            Date create = DateUtils.String2Date(create_string);
            Date modify = DateUtils.String2Date(modify_string);
            titleList.add(
                    new ListData(title, create, modify,tagListString)
            );
            if (cursor.isLast())
                break;
            cursor.moveToNext();
        }
        cursor.close();
        return titleList;
    }

    public LinkedList<ListData> getNotesTitleListContainTags(Collection<Tag> tags, boolean matchAll) {
        Cursor cursor = db.rawQuery("select * from notes;", null);
        if(cursor == null ) {
            return new LinkedList<>();
        }
        if (cursor.getCount() == 0) {
            cursor.close();

            return new LinkedList<>();
        }
        cursor.moveToFirst();
        LinkedList<ListData> titleList = new LinkedList<>();
        while (true) {
            String tagListString = cursor.getString(cursor.getColumnIndexOrThrow("catagory_list"));
            int flag;
            if(matchAll)
            {
                flag=1;
                for(Tag tag:tags)
                {
                    if(!tagListString.contains(tag.name))
                    {
                        flag=0;
                        break;
                    }
                }
            }
            else
            {
                if(tags.size()==0)
                    flag=1;
                else
                    flag=0;
                for(Tag tag:tags)
                {
                    if(tagListString.contains(tag.name))
                    {
                        flag=1;
                        break;
                    }
                }
            }
            if(flag==1)
            {
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String create_string = cursor.getString(cursor.getColumnIndexOrThrow("create_time"));
                String modify_string = cursor.getString(cursor.getColumnIndexOrThrow("modify_time"));
                Date create = DateUtils.String2Date(create_string);
                Date modify = DateUtils.String2Date(modify_string);
                titleList.add(
                        new ListData(title, create, modify,tagListString)
                );
            }
            if (cursor.isLast())
                break;
            cursor.moveToNext();
        }
        cursor.close();
        return titleList;
    }

    public LinkedList<ListData> getNotesTitleListContainKeywords(String keywords) {
        String[] p = keywords.split(" ");
        LinkedList<ListData> resList=new LinkedList<>();
        LinkedList<ListData> notesList = getNotesTitleList();
        for(ListData listData:notesList )
        {
            if(contains(p, listData.title))
                resList.add(listData);
        }
        return resList;
    }

    boolean contains(String [] keywords, String str)
    {
        for(String s:keywords)
            if(!str.contains(s))
                return false;
        return true;
    }

    /**
     * 在数据库中删除标题在给出的列表中的所有笔记。
     *
     * @param deleteTitleList 删除笔记的标题列表。
     * @throws NoteNotExistException 当列表中的某条笔记的标题在数据库中并不存在会抛出此异常。
     */
    public void deleteNotesTitleList(LinkedList<ListData> deleteTitleList) throws NoteNotExistException {
        Cursor cursor;
        for (ListData deleted: deleteTitleList) {
            String deleteTitle = deleted.title;
            cursor = db.rawQuery("select * from notes where title = ?;", new String[] {deleteTitle});
            if (cursor == null || cursor.getCount() == 0) {
                throw new NoteNotExistException("Note whose title is \"" + deleteTitle + "\" doesn't exist!");
            }
            cursor.close();
            db.execSQL("delete from notes where title = ?;", new String[] {deleteTitle});
        }
    }

    public List<Tag> getAllTagsList()
    {
        updateCatagoryList();
        Cursor cursor = db.rawQuery("select * from catagories;", null);
        if (cursor.getCount() == 0) {
            cursor.close();
            return new LinkedList<>();
        }
        cursor.moveToFirst();
        String tagString = cursor.getString(1);
        return Tag.getTagList(tagString);
    }

    public void updateCatagoryList()
    {
        Cursor cursor = db.rawQuery("select catagory_list from notes;", null);
        if (cursor.getCount() == 0) {
            cursor.close();
            return;
        }
        cursor.moveToFirst();
        Set<Tag> tagsSet = new TreeSet<>();
        while (true) {
            String tagString = cursor.getString(0);
            tagsSet.addAll(Tag.getTagList(tagString));
            if (cursor.isLast())
                break;
            cursor.moveToNext();
        }
        cursor.close();
        cursor=db.rawQuery("select * from catagories;", null);
        if(cursor.getCount()==0)
        {
            db.execSQL(
                    "insert into catagories (catagory) values (?);",
                    new String[] {Tag.getTagListString(tagsSet)}
            );
        }
        else
        {
            db.execSQL(
                    "update catagories set catagory = ? where _id = 1;",
                    new String[] {Tag.getTagListString(tagsSet)}
            );
        }
    }
}
