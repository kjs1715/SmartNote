package com.littleboss.smartnote;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

@RunWith(RobolectricTestRunner.class)
public class TabSelectActivityTest {
    NoteDatabase database = null;
    private ActivityController<TagSelectActivity> controller;
    private TagSelectActivity activity;
    @Before
    public void setUp() throws Exception {
        try {
            NoteDatabase.dropDatabaseIfExist();
            database = NoteDatabase.getInstance();
            database.setTestMod(1);

            // for testing, inserted a data for database, inorder to return notesList
            database.saveNoteByTitle("", "test", "test", null);
            database.saveNoteByTitle("test", "", "", "test");
            database.saveNoteByTitle("test", "", "", "test1");

            controller = Robolectric.buildActivity(TagSelectActivity.class).create().start().resume().visible();
            activity = controller.get();
        }
        catch (Exception e) {
            Log.i("setup err : ", e.toString());
        }
    }

    @After
    public void afterTest() {
        database.closeConnection();
    }

    @Test
    public void testMethods() throws Exception {
        View view = activity.getTestView();
        view.performClick();
        view.performClick();
    }


    @Test
    public void startTest() throws Exception {
        Button button = activity.findViewById(R.id.sbutton);
        activity.setTitlE(" ");
        button.performClick();
    }
}
