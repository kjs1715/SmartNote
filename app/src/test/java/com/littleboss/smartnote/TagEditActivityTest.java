package com.littleboss.smartnote;

import android.app.AlertDialog;
import android.support.design.widget.FloatingActionButton;
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
public class TagEditActivityTest {
    NoteDatabase database = null;
    private ActivityController<TagEditActivity> controller;
    private TagEditActivity activity;
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
            controller = Robolectric.buildActivity(TagEditActivity.class).create().start().resume().visible();
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
    public void startTest() throws Exception {
        Button button = activity.findViewById(R.id.sbutton);
        activity.setTitlE(" ");
        button.performClick();
    }

    @Test
    public void addTagDialog() throws Exception {
        activity.setTitlE(" ");
        FloatingActionButton button = activity.findViewById(R.id.addTagFab);
        button.performClick();
    }

    @Test
    public void testMethods() throws Exception {
        View view =  activity.getTestView();
        view.performClick();
        view.performClick();
    }

    @Test
    public void testAddTagDialog() throws Exception {
        AlertDialog alertDialog = activity.addTagDialog();
        Tag tag = new Tag("test1");

        alertDialog.show();
        Button b = alertDialog.getButton(-1);
        b.performClick();

        activity.setThisTagList(tag);
        alertDialog.show();
        b.performClick();

        activity.deleteThisTagList();
        activity.setAllTagList(tag);
        alertDialog.show();
        b.performClick();

        activity.deleteAllTagList();
        alertDialog.show();
        b.performClick();
    }
}
