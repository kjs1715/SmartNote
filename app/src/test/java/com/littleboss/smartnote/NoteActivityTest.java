package com.littleboss.smartnote;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toolbar;

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
import org.robolectric.annotation.Config;


import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;
import static org.junit.Assert.*;


@RunWith(RobolectricTestRunner.class)
public class NoteActivityTest {
    ActivityController<NoteEditActivity> controller;
    private NoteDatabase database = null;
    private static final int photoFromGalleryCode = 0x101;
    private static final int photoFromCameraCode = 0x102;
    private static final int videoFromGalleryCode = 0x201;
    private static final int videoFromCameraCode = 0x202;
    File srcFile;

    @Before
    public void setUp() {
        NoteDatabase.dropDatabaseIfExist();
        database = NoteDatabase.getInstance();
        database.setTestMod(1);
        //todo add visible
        controller = Robolectric.buildActivity(NoteEditActivity.class).create().start().resume();//.visible();

    }

    @After
    public void afterTest() {
        database.closeConnection();
    }

    @Test
    public void LBAudioViewTest() throws Exception {
        NoteEditActivity activity = controller.get();
        InputStream sourceStream = activity.getResources().openRawResource(R.raw.test);
        final int bytesPerRead = 1024;
        byte[] buffer = new byte[bytesPerRead];
        srcFile = new File("data/src.wav");
        srcFile.getParentFile().mkdirs();
        DataOutputStream destStream = new DataOutputStream(new FileOutputStream(srcFile.getAbsolutePath()));
        int size = -1, sizeCount = 0;
        while (true) {
            size = sourceStream.read(buffer,0, bytesPerRead);
            //System.out.println("size = " + String.valueOf(size));
            if (size < 0) {
                sourceStream.close();
                destStream.close();
                break;
            }
            destStream.write(buffer, 0, size);
            sizeCount += size;
        }
        LBAudioView view = new LBAudioView(
                srcFile.getAbsolutePath(),
                activity,
                null,
                false
        );
        activity.getMyViewGroup().addViewtoCursor(view);
        view.setRecognizedText("smf!");
        view.setContent("666");
        String dataString = view.toDataString();
        String filePath = view.getFilePath();
        View getView = view.getView();
        view.audioDialog();
        view.deleteDialog();
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
//    @Test
//    public void backButtonPressedTest() throws Exception {
//        Activity activity = controller.get();
//        ActionBar toolbar = activity.getActionBar();
//        toolbar.getDisplayOptions();
//    }

    @Test
    public void audioDialog() throws Exception {
        NoteEditActivity activity = controller.get();
        activity.AudioDialog();
    }

    @Test
    public void onActivityResultTest() throws Exception {
        Intent intent = new Intent();
        NoteEditActivity activity = controller.get();
        activity.onActivityResult(photoFromGalleryCode,RESULT_OK, intent);
        activity.onActivityResult(photoFromCameraCode,RESULT_OK, intent);
        activity.onActivityResult(videoFromGalleryCode,RESULT_OK, intent);
        activity.onActivityResult(videoFromCameraCode,RESULT_OK, intent);
    }

    @Test
    public void onRequestPermissionTest() throws Exception {
        int[] grant = {PackageManager.PERMISSION_GRANTED};
        int[] empty  = {};
        String[] permissions = { Manifest.permission.CAMERA};
        NoteEditActivity activity = controller.get();
        activity.onRequestPermissionsResult(1, permissions, grant);
        activity.onRequestPermissionsResult(1, permissions, empty);
        activity.onRequestPermissionsResult(0, permissions, empty);
    }
}
