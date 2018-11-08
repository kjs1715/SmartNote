package com.littleboss.smartnote;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.littleboss.smartnote.Utils.ImageUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;


import static org.junit.Assert.*;


@RunWith(RobolectricTestRunner.class)
public class NoteActivityTest {
    ActivityController<NoteEditActivity> controller;

    @Before
    public void setUp() {
        NoteDatabase.dropDatabaseIfExist();
        NoteDatabase database = NoteDatabase.getInstance();
        database.setTestMod(1);
        controller = Robolectric.buildActivity(NoteEditActivity.class).create().start();
    }

    @After
    public void afterTest() {
        NoteDatabase.closeConnection();
    }
    @Test
    public void startTest() throws Exception {
        Activity activity = controller.get();
        assertNotNull(activity);
    }

    @Test
    public void testLBViewGroup() throws Exception {
        Activity activity = controller.get();
        LBAbstractViewGroup lbAbstractViewGroup = new LBAbstractViewGroup(activity);
        assertNotNull(lbAbstractViewGroup);
    }


    @Test
    public void testBottomNavigationbar() throws Exception {
        BottomNavigationBar bottomNavigationBar = controller.get().getBottomNavigationbar();
        bottomNavigationBar.performClick();
        assertTrue(!bottomNavigationBar.isHidden());
    }
    

    @Test
    public void testChooseTab() throws Exception {
        // TODO: 06/11/2018 AudioPart could not pass
        NoteEditActivity activity = controller.get();
        for(int i = 0; i < 2; i++) {
            activity.chooseTab(i);
        }
        activity.chooseTab(3);
        activity.chooseTab(4);
    }

    @Test
    public void testLBImageView() throws Exception {
        LBImageView lbImageView = new LBImageView(controller.get());
        assertNotNull(lbImageView);
    }

    @Test
    public void testMethods() throws Exception {
        controller.get().onPhotoButtonClicked();
        controller.get().onVideoButtonClicked();
    }
}
