package com.littleboss.smartnote;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import static org.junit.Assert.*;


@RunWith(RobolectricTestRunner.class)
public class LBImageActivityTest {
    @Before
    public void setUp() throws Exception {
        NoteDatabase.dropDatabaseIfExist();
        NoteDatabase database = NoteDatabase.getInstance();
        database.setTestMod(1);
    }

    @After
    public void afterTest() {
        NoteDatabase.closeConnection();
    }
    @Test
    public void startTest() throws Exception {
        ActivityController<LBImageActivity> controller = Robolectric.buildActivity(LBImageActivity.class).create().start().resume().visible();
        Activity activity = controller.get();
        assertNotNull(activity);
    }
}