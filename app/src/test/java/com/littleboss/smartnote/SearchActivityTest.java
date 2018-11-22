package com.littleboss.smartnote;

import android.app.Activity;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowListView;

import static org.junit.Assert.*;


@RunWith(RobolectricTestRunner.class)
public class SearchActivityTest {
    private NoteDatabase database = null;
    @Before
    public void setUp() throws Exception {
        try {
            NoteDatabase.dropDatabaseIfExist();
            database = NoteDatabase.getInstance();
            database.saveNoteByTitle("", "test", "test", "test");
            database.setTestMod(1);

            // for testing, inserted a data for database, inorder to return notesList
            database.saveNoteByTitle("", "test", "test", "test");
        }
        catch (Exception e) {
            Log.i("setup err : ", e.toString());
        }
    }

    @Test
    public void startTest() throws Exception {
        ActivityController<SearchActivity> controller = Robolectric.buildActivity(SearchActivity.class).create().start().resume().visible();
        Activity activity = controller.get();
        assertNotNull(activity);

        Button button = activity.findViewById(R.id.searchbutton);
        button.performClick();

        ListView listView = activity.findViewById(R.id.searchlistview);
        ShadowListView shadowListView = Shadows.shadowOf(listView);
        shadowListView.performItemClick(0);
    }

    @After
    public void afterTest() {
        database.closeConnection();
    }
}
