package com.littleboss.smartnote;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.littleboss.smartnote.Utils.ImageUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ImageUtil;
import org.robolectric.shadows.ShadowListView;

import static org.junit.Assert.*;


@RunWith(RobolectricTestRunner.class)
public class SearchActivityTest {
    @Before
    public void setUp() throws Exception {
        NoteDatabase.dropDatabaseIfExist();
        NoteDatabase database = NoteDatabase.getInstance();
        database.setTestMod(1);
        // for testing, inserted a data for database, inorder to return notesList
        NoteDatabase.saveNoteByTitle("", "test", "test");
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
        NoteDatabase.closeConnection();
    }
}
