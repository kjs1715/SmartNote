package com.littleboss.smartnote;

import android.app.Activity;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class NoteEditActivityTest {
    private NoteEditActivity activity;
    private ActivityController<NoteEditActivity> controller;

    @Before
    public void setUp() throws Exception {
        controller = Robolectric.buildActivity(NoteEditActivity.class);
        activity = controller.get();
    }

    @Test
    public void testLifeCycle() throws Exception {
        assertNull(activity.getLifecycle());

        controller.create();
        assertEquals("onCreate", activity.getLifecycle().toString());

        controller.start();
        assertEquals("onStart", activity.getLifecycle().toString());

        controller.resume();
        assertEquals("onResume", activity.getLifecycle().toString());

        controller.pause();
        assertEquals("onPause", activity.getLifecycle().toString());

        controller.stop();
        assertEquals("onStop", activity.getLifecycle().toString());

        controller.restart();
        assertEquals("onStart", activity.getLifecycle().toString());

        controller.destroy();
        assertEquals("onDestroy", activity.getLifecycle().toString());
    }

}
