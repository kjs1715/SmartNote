package com.littleboss.smartnote;

import android.app.Activity;
import android.util.Log;

import com.littleboss.smartnote.Utils.AudioClipper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@RunWith(RobolectricTestRunner.class)
public class AudioClipperTest {
    private ActivityController<MainActivity> controller;
    private NoteDatabase database = null;
    @Before
    public void setUp() throws Exception {
        try {
            NoteDatabase.dropDatabaseIfExist();
            database = NoteDatabase.getInstance();
            database.saveNoteByTitle("", "test", "test", "test");
            database.saveNoteByTitle("test", "test1", "test1", "test1");
            database.setTestMod(1);
            controller = Robolectric.buildActivity(MainActivity.class).create().start().resume().visible();
        }
        catch (Exception e) {
            Log.i("setup err : ", e.toString());
        }
    }

    @Test
    public void test() throws Exception {
        File srcFile, dstFile;
        Activity mainActivity = controller.get();
        InputStream sourceStream = mainActivity.getResources().openRawResource(R.raw.test);
        final int bytesPerRead = 1024;
        byte[] buffer = new byte[bytesPerRead];
        srcFile = new File("data/src.wav");
        dstFile = new File("data/dst.wav");
        DataOutputStream destStream=null;
        try {
            destStream = new DataOutputStream(new FileOutputStream(srcFile.getAbsolutePath()));
            int size = -1, sizeCount = 0;
            while (true) {
                size = sourceStream.read(buffer, 0, bytesPerRead);
                if (size < 0) {
                    sourceStream.close();
                    destStream.close();
                    break;
                }
                destStream.write(buffer, 0, size);
                sizeCount += size;
            }
            new AudioClipper().audioClip(srcFile.getAbsolutePath(), dstFile.getAbsolutePath(), 1);
        }
        finally {
            if(destStream!=null)
                destStream.close();
        }
    }

    @After
    public void after() throws Exception {
        database.closeConnection();

    }
}
