package com.littleboss.smartnote;

import android.app.Activity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import static org.junit.Assert.*;


@RunWith(RobolectricTestRunner.class)
public class LBImageActivityTest {
    private NoteDatabase database;
    @Before
    public void setUp() throws Exception {
        NoteDatabase.dropDatabaseIfExist();
        database = NoteDatabase.getInstance();

        database.setTestMod(1);
    }

    @After
    public void afterTest() {
        database.closeConnection();
    }
    @Test
    public void startTest() throws Exception {
        ActivityController<LBImageActivity> controller = Robolectric.buildActivity(LBImageActivity.class).create().start().resume().visible();
        Activity activity = controller.get();
        assertNotNull(activity);
    }
}