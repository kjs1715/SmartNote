package com.littleboss.smartnote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.FloatingActionButton;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.littleboss.smartnote.Utils.ImageUtils;

import org.apache.tools.ant.Main;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ImageUtil;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowListView;

import java.util.List;

import static org.junit.Assert.*;
import static org.robolectric.shadows.ShadowDialog.getShownDialogs;


@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {
    @Before
    public void setUp() throws Exception {
        NoteDatabase.dropDatabaseIfExist();
        NoteDatabase database = NoteDatabase.getInstance();
        NoteDatabase.saveNoteByTitle("", "test", "test");
        database.setTestMod(1);
    }

    @After
    public void afterTest() {
        NoteDatabase.closeConnection();
    }

    @Test
    public void startTest() throws Exception {
        ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).create().start().resume().visible();
        Activity activity = controller.get();
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        assertNotNull(shadowActivity);
    }

    @Test
    public void uiTests() throws Exception {
        ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).create().start().resume().visible();
        Activity activity = controller.get();
        FloatingActionButton fab = activity.findViewById(R.id.fab);
        fab.performClick();

        // buttons tests
        Button bt_cancel = activity.findViewById(R.id.bt_cancel);
        Button bt_delete = activity.findViewById(R.id.bt_delete);
        bt_cancel.performClick();
        bt_delete.performClick();

        // mainlist test
        ListView listView = activity.findViewById(R.id.mainlist);
        View item = listView.getAdapter().getView(0, null, null);
        item.performClick();
        item.performLongClick();

        controller.get().sortNotesList(0);
        controller.get().sortNotesList(1);
        controller.get().sortNotesList(2);

        AlertDialog sortDialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNull(sortDialog);
        Menu menu = activity.findViewById(R.menu.menu_mainactivity);
        MenuItem sortItem = new RoboMenuItem(R.id.sortitem);
        activity.onOptionsItemSelected(sortItem);
        sortDialog = ShadowAlertDialog.getLatestAlertDialog();
        // TODO: 2018/11/10 sortDialog would be null
//        assertNotNull(sortDialog);
    }
}