package com.littleboss.smartnote;

import android.app.Activity;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import static org.junit.Assert.*;


@RunWith(RobolectricTestRunner.class)
public class LBVideoViewActivityTest {
    private NoteDatabase database;
    @Before
    public void setUp() throws Exception {
        try {
            NoteDatabase.dropDatabaseIfExist();
            database = NoteDatabase.getInstance();
            database.setTestMod(1);
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
    public void startTest() {
            ActivityController<LBVideoActivity> controller =
                    Robolectric.buildActivity(LBVideoActivity.class)
                            .create()
                            .start()
                            .resume()
                            .visible();
            Activity activity = controller.get();
            assertNotNull(activity);
    }
}