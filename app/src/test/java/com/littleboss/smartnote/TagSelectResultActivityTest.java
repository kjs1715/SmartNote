package com.littleboss.smartnote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.FloatingActionButton;
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
public class TagSelectResultActivityTest {
    NoteDatabase database = null;
    private ActivityController<TagSelectResultActivity> controller;

    @Before
    public void setUp() throws Exception {
        NoteDatabase.dropDatabaseIfExist();
        database = NoteDatabase.getInstance();
        database.setTestMod(1);

//        database.saveNoteByTitle("", "test", "test","test");
        // for testing, inserted a data for database, inorder to return notesList
        database.saveNoteByTitle("", "test", "test", null);
        database.saveNoteByTitle("test", "", "", "test");
        database.saveNoteByTitle("test", "", "", "test1");
        controller = Robolectric.buildActivity(TagSelectResultActivity.class).create().start().resume().visible();
    }

    @After
    public void afterTest() {
        database.closeConnection();
    }

    @Test
    public void startTest() throws Exception {
        TagSelectResultActivity activity = controller.get();
        activity.adapter.getItem(0);
    }
}