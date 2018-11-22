package com.littleboss.smartnote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityInfoCheck;
import com.littleboss.smartnote.Utils.ImageUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ImageUtil;

import java.io.InputStream;

import androidx.test.InstrumentationRegistry;

import static org.junit.Assert.*;

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

            //        database.saveNoteByTitle("", "test", "test","test");
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
        //TagSelectActivity activity = controller.get();
        View view = activity.getTestView();
        view.performClick();
        view.performClick();
    }


    @Test
    public void startTest() throws Exception {
        //TagSelectActivity activity = controller.get();
        Button button = activity.findViewById(R.id.sbutton);
        activity.setTitlE(" ");
        button.performClick();
    }
}
