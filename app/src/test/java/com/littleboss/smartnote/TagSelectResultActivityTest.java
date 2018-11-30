package com.littleboss.smartnote;

import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

@RunWith(RobolectricTestRunner.class)
public class TagSelectResultActivityTest {
    NoteDatabase database = null;
    private TagSelectResultActivity activity;
    @Before
    public void setUp() throws Exception {
        ActivityController<TagSelectResultActivity> controller;
        try {
            NoteDatabase.dropDatabaseIfExist();
            database = NoteDatabase.getInstance();
            database.setTestMod(1);

            // for testing, inserted a data for database, inorder to return notesList
            database.saveNoteByTitle("", "test", "test", null);
            database.saveNoteByTitle("test", "", "", "test");
            database.saveNoteByTitle("test", "", "", "test1");
            controller = Robolectric.buildActivity(TagSelectResultActivity.class).create().start().resume().visible();
            activity = controller.get();
        }
        catch (Exception e) {
            Log.i("error : ", e.toString());
        }
    }

    @After
    public void afterTest() {
        database.closeConnection();
    }

    @Test
    public void startTest() throws Exception {
        activity.adapter.getItem(0);
    }
}