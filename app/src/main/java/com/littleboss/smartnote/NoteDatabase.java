package com.littleboss.smartnote;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.littleboss.smartnote.Utils.DateUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
class NoAudiosYetException extends Exception {
    public NoAudiosYetException(String msg) { super(msg); }
}

/**
 * 包含有笔记创建日期、修改日期、内容的类，描述笔记信息。
 */
class NoteInfo {
    public String create_time, modify_time, content;

    /**
     * 笔记信息类的构造函数。
     *
     * @param _create_time 字符串，笔记的创建日期。
     * @param _modify_time 字符串，笔记的最近修改日期。
     * @param _content     字符串，笔记内容。
     */
    public NoteInfo(String _create_time, String _modify_time, String _content) {
        create_time = _create_time;
        modify_time = _modify_time;
        content = _content;
    }
}

public class NoteDatabase {
    static NoteDatabase instance = null;
    static SQLiteDatabase db;
    static String noteDatabasePath = "data/data/com.littleboss.smartnote/app_databases/data.db";

    private NoteDatabase() {
        new File(noteDatabasePath).getParentFile().mkdirs();
        db = SQLiteDatabase.openOrCreateDatabase(noteDatabasePath, null);
        try {
            db.execSQL("create table audiopos (pos text);");
            db.execSQL("create table catagories (_id integer primary key autoincrement, catagory text);");
            db.execSQL("create table notes (_id integer primary key autoincrement, title text, create_time text, modify_time text, content text, catagory_list text);");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() {
        db.close();
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
    public static String getNotesByTitle(String title) {
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
     * @throws NoteNotExistException         当待修改的笔记题目不为空但在数据库中并不存在会抛出此异常。
     * @throws SameTitleNoteExistedException 新笔记的标题或者待修改笔记的新标题已经存在。
     */
    public static void saveNoteByTitle(String oldTitle, String newTitle, String content) throws NoteNotExistException, SameTitleNoteExistedException {
        Cursor cursor = null;
        String modify_time = "";
        if (!oldTitle.equals("")) {
            cursor = db.rawQuery("select * from notes where title = ?;", new String[] {oldTitle});
            if (cursor != null && cursor.getCount() > 0) {
                modify_time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                db.execSQL(
                        "update notes set modify_time = ?, content = ? where title = ?;",
                        new String[] {modify_time, content, oldTitle}
                );
            }
            else {
                throw new NoteNotExistException("Note whose title is \"" + oldTitle + "\" doesn't exist!");
            }
            return;
        }

        String create_time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        if (modify_time.equals(""))
            modify_time = create_time;
        if (newTitle.equals(""))
            newTitle = "note @ " + create_time;
        cursor = db.rawQuery("select * from notes where title = ?;", new String[] {newTitle});
        if (cursor != null && cursor.getCount() > 0) {
            throw new SameTitleNoteExistedException("Note whose title is \"" + newTitle + "\" exists!");
        }
        db.execSQL(
                "insert into notes (title, create_time, modify_time, content, catagory_list) values (?, ?, ?, ?, ?);",
                new String[] {newTitle, create_time, modify_time, content, ""}
        );
    }

    /**
     * 获取所有笔记的标题列表。
     *
     * @return 所有笔记的标题列表。若不存在任何笔记，则返回null。
     */
    public static LinkedList<ListData> getNotesTitleList() {
        Cursor cursor = db.rawQuery("select * from notes;", null);
        if (cursor == null || cursor.getCount() == 0) {
            cursor.close();
            return new LinkedList<>();
        }
        cursor.moveToFirst();
        LinkedList<ListData> titleList = new LinkedList();
        while (true) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String create_string = cursor.getString(cursor.getColumnIndexOrThrow("create_time"));
            String modify_string = cursor.getString(cursor.getColumnIndexOrThrow("modify_time"));
            Date create = DateUtils.String2Date(create_string);
            Date modify = DateUtils.String2Date(modify_string);
            titleList.add(
                    new ListData(title, create, modify)
            );
            if (cursor.isLast())
                break;
            cursor.moveToNext();
        }
        cursor.close();
        return titleList;
    }

    /**
     * 在数据库中删除标题在给出的列表中的所有笔记。
     *
     * @param deleteTitleList 删除笔记的标题列表。
     * @throws NoteNotExistException 当列表中的某条笔记的标题在数据库中并不存在会抛出此异常。
     */
    public static void deleteNotesTitleList(LinkedList<ListData> deleteTitleList) throws NoteNotExistException {
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

    /**
     * Sets latest audio location.
     *
     * @param loc 最近一次录音的存放位置的完整地址。
     */
    public static void setLatestAudioLocation(String loc) {
        Cursor cursor = db.rawQuery("select * from audiopos", null);
        if (cursor != null && cursor.getCount() > 0) {
            try {
                db.execSQL("drop table audiopos");
                db.execSQL("create table audiopos (pos text);");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        db.execSQL("insert into audiopos (pos) values (?);", new String[] {loc});
    }

    /**
     * 获取最近一次录音存放的完整地址.
     *
     * @throws NoAudiosYetException 调用此函数时还未进行任何录音时会抛出此异常。
     */
    public static String getLatestAudioLocation() throws NoAudiosYetException{
        Cursor cursor = db.rawQuery("select * from audiopos", null);
        if (cursor == null || cursor.getCount() == 0)
            throw new NoAudiosYetException("No audios yet!");
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndexOrThrow("pos"));
    }
}
