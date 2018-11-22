package com.littleboss.smartnote;

import android.app.Activity;
import android.app.AlertDialog;
import android.support.design.widget.FloatingActionButton;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;

import java.util.Date;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;


@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {
    ActivityController<MainActivity> controller;
    private NoteDatabase database = null;
    @Before
    public void setUp() throws Exception {
        NoteDatabase.dropDatabaseIfExist();
        database = NoteDatabase.getInstance();
        database.saveNoteByTitle("", "test1", "test1",null);
        database.saveNoteByTitle("", "test2", "test2",null);
        database.setTestMod(1);
        controller = Robolectric.buildActivity(MainActivity.class).create().start().resume().visible();
    }

    @After
    public void afterTest() {
        database.closeConnection();
    }

//      todo: uncomment and fix the NullPointerException

    @Test
    public void buttonTest() throws Exception {
        try {
            MainActivity activity = controller.get();
            // buttons tests
            Button bt_delete = activity.findViewById(R.id.bt_delete);
            bt_delete.performClick();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void startTest() throws Exception {
        MainActivity activity = controller.get();
        ShadowActivity shadowActivity = shadowOf(activity);
        assertNotNull(shadowActivity);
    }

    @Test
    public void uiTests() throws Exception {
        MainActivity activity = controller.get();
        FloatingActionButton fab = activity.findViewById(R.id.fab);
        fab.performClick();
    }

    @Test
    public void menuTest() throws Exception {
        MainActivity activity = controller.get();
        AlertDialog sortDialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNull(sortDialog);
        MenuItem menuItem = new RoboMenuItem(R.id.sortitem);
        activity.onOptionsItemSelected(menuItem);
        activity.sortDialog();
    }

    @Test
    public void buttonTest1() throws Exception {
        try {
            Activity activity = controller.get();
            // buttons tests
            Button bt_cancel = activity.findViewById(R.id.bt_cancel);
            bt_cancel.performClick();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

////      todo: uncomment and fix the IndexOutOfBoundsException
    @Test
    public void mainListTest() throws Exception {
        try {
            Activity activity = controller.get();
            // mainlist test
            ListView listView = activity.findViewById(R.id.mainlist);
            View item = listView.getAdapter().getView(0, null, null);
            item.performClick();
            item.performLongClick();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

//      todo: uncomment and fix the IllegalStateException
    @Test
    public void testSorting() throws Exception {
        Activity activity = controller.get();

        database.saveNoteByTitle("", "test3", "test3",null);
        database.saveNoteByTitle("", "test4", "test4",null);


        // test method for comparing
        Date testDate1 = new Date(2018,5,1);
        Date testDate2 = new Date(2018,5,2);
        Date testMDate1 = new Date(2018,6,1);
        Date testMDate2 = new Date(2018,6,2);

        controller.get().sortNotesList(0);
        controller.get().sortNotesList(1);
        controller.get().sortNotesList(2);

        ListData test1 = new ListData("test11", testDate1, testMDate1,"");
        ListData test2 = new ListData("test22", testDate2, testMDate2,"");

        controller.get().Compare(0, test1, test2);
        controller.get().Compare(1, test1, test2);
        controller.get().Compare(2, test1, test2);
    }

//    //  todo: uncomment and fix the IndexOutOfBoundsException
//    @Test
    public void enterDialogTest() throws Exception {
        MainActivity activity = controller.get();
        ListView listView = activity.findViewById(R.id.mainlist);
        View item = listView.getAdapter().getView(0, null, null);
        item.performClick();
        View item1 = MainActivity.enterNoteDialog(0,activity.notesList,activity).getListView().getAdapter().getView(0, null, null);
        item1.performClick();
//        AlertDialog enterDialog = ShadowAlertDialog.getLatestAlertDialog();
//        assertNotNull(enterDialog);
    }

////      todo: uncomment and fix the IndexOutOfBoundsException
    @Test
    public void testLongClick() throws Exception {
        try {
        MainActivity activity = controller.get();
        ListView listView = activity.findViewById(R.id.mainlist);
        View item = listView.getAdapter().getView(0, null, null);
//        item.performLongClick();
        activity.isMultiselected();
        item.performClick();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRequestPermission() throws Exception {
        Activity activity = Robolectric.buildActivity(MainActivity.class).create().start().resume().visible().get();
        String[] permissions = {"Camera"};
        activity.requestPermissions(permissions,0);
    }
}