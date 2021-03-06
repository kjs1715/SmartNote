package com.littleboss.smartnote;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;

import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowDialog;
import static org.robolectric.Shadows.shadowOf;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static org.junit.Assert.*;


@RunWith(RobolectricTestRunner.class)
public class NoteActivityTest {
    private ActivityController<NoteEditActivity> controller;
    private NoteEditActivity activity;
    private NoteDatabase database = null;
    private static final int PHOTOFROMGALLERYCODE = 0x101;
    private static final int PHOTOFROMCAMERACODE = 0x102;
    private static final int VIDEOFROMGALLERYCODE = 0x201;
    private static final int VIDEOFROMCAMERACODE = 0x202;

    @Before
    public void setUp() {
        try {
            NoteDatabase.dropDatabaseIfExist();
            database = NoteDatabase.getInstance();

            database.setTestMod(1);
            //todo add visible
            controller = Robolectric.buildActivity(NoteEditActivity.class).create().start().resume();//.visible();
            activity = controller.get();
        }
        catch (Exception e) {
            Log.i("error initView() : ", e.toString());
        }

    }

    @After
    public void afterTest() {
        database.closeConnection();
    }

    @Test
    public void LBAudioViewTest() throws Exception {
        File srcFile;
        InputStream sourceStream = activity.getResources().openRawResource(R.raw.test);
        final int bytesPerRead = 1024;
        byte[] buffer = new byte[bytesPerRead];
        srcFile = new File("data/src.wav");
        DataOutputStream destStream = null;
        try {
            destStream = new DataOutputStream(new FileOutputStream(srcFile.getAbsolutePath()));

            int size = -1;
            while (true) {
                size = sourceStream.read(buffer, 0, bytesPerRead);
                if (size < 0) {
                    sourceStream.close();
                    destStream.close();
                    break;
                }
            }
            activity.getMyViewGroup().addViewtoCursor(
                    new LBAudioView(
                            srcFile.getAbsolutePath(),
                            activity,
                            null,
                            false
                    )
            );
        }
        finally {
            if (destStream!=null)
                destStream.close();
        }
        LBAudioView view = new LBAudioView(
                srcFile.getAbsolutePath(),
                activity,
                null,
                false
        );
        activity.getMyViewGroup().addViewtoCursor(view);
        view.setRecognizedText("smf!");
        view.setContent("src=(.666)content=(.777)");
        String dataString = view.toDataString();
        String filePath = view.getFilePath();
        View getView = view.getView();
        view.audioDialog();
        view.deleteDialog();
    }

    @Test
    public void startTest() throws Exception {
        //Activity activity = controller.get();
        assertNotNull(activity);
    }

    @Test
    public void testLBViewGroup() throws Exception {
        //Activity activity = controller.get();
        LBAbstractViewGroup lbAbstractViewGroup = new LBAbstractViewGroup(activity);
        LBTextView text1 = new LBTextView("666", activity);
        LBTextView text2 = new LBTextView("667", activity);
        LBTextView text3 = new LBTextView("668", activity);
        LBTextView text4 = new LBTextView("669", activity);
        lbAbstractViewGroup.addViewtoCursor(text1);
        lbAbstractViewGroup.lastFocusView = text1;
        lbAbstractViewGroup.addViewToEditText(text2);
        lbAbstractViewGroup.addViewToEditText(text3);
        lbAbstractViewGroup.deleteView(0);
        lbAbstractViewGroup.addViewToEditText(text4);
        lbAbstractViewGroup.moveViewUp(1);
        lbAbstractViewGroup.moveViewDown(0);
        lbAbstractViewGroup.swapViewPosition(0, 1);
        lbAbstractViewGroup.setContent("<text>666</text><text>667</text>");

        List<LBAbstractView> dump = lbAbstractViewGroup.buildData();
        EditText dump2 = lbAbstractViewGroup.getCurFousEditText();
        lbAbstractViewGroup.disableClick();
        assertNotNull(lbAbstractViewGroup);
    }

    @Test
    public void testBottomNavigationbar() throws Exception {
        BottomNavigationBar bottomNavigationBar = activity.getBottomNavigationbar();
        bottomNavigationBar.performClick();
        assertTrue(!bottomNavigationBar.isHidden());
    }

    @Test
    public void testChooseTab() throws Exception {
        // TODO: 06/11/2018 AudioPart could not pass
        //NoteEditActivity activity = controller.get();
        for (int i = 0; i < 5; i++) {
            activity.chooseTab(i);
        }
    }

    @Test
    public void testLBImageView() throws Exception {
        LBImageView lbImageView = new LBImageView(controller.get());
        assertNotNull(lbImageView);
    }

    @Test
    public void testMethods() throws Exception {
        activity.onPhotoButtonClicked();
        activity.onVideoButtonClicked();
    }

    @Test
    public void backButtonPressedTest() throws Exception {
        //NoteEditActivity activity = controller.get();
        activity.setOldTitle("");
        activity.performbackbuttonclick();
        AlertDialog dialog = activity.backPressedDialog();
        int[] buttonlist = {-1, -2, -3};
        for (int i = 0; i < 3; i++) {
            dialog.show();
            Button b = dialog.getButton(buttonlist[i]);
            b.performClick();
        }
//        dialog.getButton(1);
//        dialog.getButton(2);
    }


    @Test
    public void audioDialog() throws Exception {
        //NoteEditActivity activity = controller.get();
        activity.AudioDialog().show();
        activity.setTest(true);
        activity.AudioDialogChoosed();
        Field field = android.support.v7.app.AlertDialog.class.getDeclaredField("mAlert");
        field.setAccessible(true);
        Object alertController = field.get(activity.AudioDialog());
        field = alertController.getClass().getDeclaredField("mAdapter");
        field.setAccessible(true);
        Adapter alertAdapter = (Adapter) field.get(alertController);
        ShadowDialog shadowAlertDialog = shadowOf(activity.AudioDialog());
//        for(int i = 0; i < 4; i++) {
//            shadowAlertDialog.clickOn(i);
//            shadowAlertDialog.clickOnText("from");
//        }
//        ShadowAdapterView adapterView = ShadowBaseAdapter.class.

    }

    @Test
    public void onActivityResultTest() throws Exception {
        Intent intent = new Intent();
        activity.onActivityResult(PHOTOFROMGALLERYCODE, RESULT_OK, intent);
        activity.onActivityResult(PHOTOFROMCAMERACODE, RESULT_OK, intent);
        activity.onActivityResult(VIDEOFROMGALLERYCODE, RESULT_OK, intent);
        activity.onActivityResult(VIDEOFROMCAMERACODE, RESULT_OK, intent);

    }

    @Test
    public void onRequestPermissionTest() throws Exception {
        int[] grant = {PackageManager.PERMISSION_GRANTED};
        int[] empty = {};
        String[] permissions = {Manifest.permission.CAMERA};
        activity.onRequestPermissionsResult(1, permissions, grant);
        activity.onRequestPermissionsResult(1, permissions, empty);
        activity.onRequestPermissionsResult(0, permissions, empty);
    }

    @Test
    public void saveNoteTest() throws Exception {
        activity.setNewCreatedFlag(false);
        activity.saveNote();
    }

    @Test
    public void takeVideoTest() throws Exception {
        activity.takeVideo();
    }

    @Test
    public void fabButtonTest() throws Exception {
        FloatingActionButton fab = activity.findViewById(R.id.goToTop);
        fab.performClick();
    }

    @Test
    public void keyboarHideAndShowTest() throws Exception {
        InputMethodManager imm1 = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        EditText editText = activity.findViewById(R.id.et_new_title);
        imm1.hideSoftInputFromWindow(editText.getWindowToken(), 0);

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }
}