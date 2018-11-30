package com.littleboss.smartnote;

import android.Manifest;
import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment;
import com.yalantis.contextmenu.lib.MenuObject;
import com.yalantis.contextmenu.lib.MenuParams;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemClickListener;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemLongClickListener;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NoteEditActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private ContextMenuDialogFragment mMenuDialogFragment;

    private String _title;
    private String _content;
    private String old_title;
    private String old_content;

    private boolean newCreatedFlag;
    private boolean test;

    private NoteDatabase noteDatabase;

    private LBAbstractViewGroup myViewGroup;
    public LBAbstractViewGroup getMyViewGroup() {
        return myViewGroup;
    }

    private BottomNavigationBar bottomNavigationBar;

    EditText et_title;

    private String latestCameraResultPath;

    private boolean isRecording=false;

    private Activity thisActivity;

    private long deamonRecordStartTime;
    private long recordStartTime;
    private long recordEndTime;
    private boolean isDeamonRecording;
    private int recordStartSecondsAgo;
    private boolean editable=true;

    SimpleDateFormat simpleDateFormat=new SimpleDateFormat("HH:mm:ss");

    public void show(Dialog dialog) {
        dialog.show();
    }

    private static final int photoFromGalleryCode = 0x101;
    private static final int photoFromCameraCode = 0x102;
    private static final int videoFromGalleryCode = 0x201;
    private static final int videoFromCameraCode = 0x202;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        thisActivity = this;

        getDir("databases", MODE_PRIVATE);
        noteDatabase = NoteDatabase.getInstance();

        _title = getIntent().getStringExtra("id");
        newCreatedFlag = getIntent().getBooleanExtra("newCreatedNote", true);
        editable = (getIntent().getIntExtra("justsee", 0)==0);

        fragmentManager = getSupportFragmentManager();

        test = false;

        initToolbar();
        initBottombar();
        initScrollButton();
        initEditText();
        if(editable)
        {
            startDeamonRecording();
        }
        else
        {
            this.myViewGroup.disableClick();
            et_title.setFocusable(false);
            bottomNavigationBar.hide();
        }
    }

    public void startDeamonRecording()
    {
        int __ = noteDatabase.getTestMod();
        if (__ != -1)
            return;
        if(isDeamonRecording)
            return;
        AudioFetcher.startRecording();
        isDeamonRecording=true;
        deamonRecordStartTime=System.currentTimeMillis();
    }

    public void stopDeamonRecording()
    {
        if (noteDatabase.getTestMod() != -1)
            return;
        if(!isDeamonRecording)
            return;
        AudioFetcher.stopAndDiscard();
        isDeamonRecording=false;
    }

    public void initScrollButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.goToTop);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final LBAbstractViewGroup sc = findViewById(R.id.viewgroup);
//                final ScrollView sc = findViewById(R.id.sc);
                sc.fullScroll(ScrollView.FOCUS_UP);
            }
        });
    }

    public void initEditText() {
        myViewGroup=findViewById(R.id.viewgroup);
        et_title = (EditText) findViewById(R.id.et_new_title);
        if(!newCreatedFlag) {
            _content = noteDatabase.getNotesByTitle(_title);
            et_title.setText(_title);
        } else {
            _content = "";
        }
        myViewGroup.setContent(_content);
        old_title = _title;
        old_content = _content;
        myViewGroup.setLastEditTextFocus();
        if(et_title.getText().toString().equals("")) {
            et_title.requestFocus();
        }
    }

    /**
     * Refered from library of Toolbar
     *
     * **/



    @Override
    public void finish()
    {
        stopDeamonRecording();
        super.finish();
    }

    @Override
    public void onBackPressed() {
        if(!editable)
        {
            finish();
            return;
        }
        if(!noteModified()) {
            if(!isRecording)
                stopDeamonRecording();
            else
                Toast.makeText(this, "后台持续录音", Toast.LENGTH_SHORT).show();
            finish();
            return ;
        }
        AlertDialog dialog = backPressedDialog();
        dialog.show();
    }

    public boolean noteModified() {
        EditText et_title = (EditText) findViewById(R.id.et_new_title);
        String t = et_title.getText().toString();
//        String c = et_content.getText().toString();
        String c = this.myViewGroup.toDataString();
        if(old_title.equals(t) && old_content.equals(c)) {
            return false;
        }
        return true;
    }


    public BottomNavigationBar getBottomNavigationbar() {
        return this.bottomNavigationBar;
        // fix for unittest
    }

    public void initToolbar() {
        /**
         * @Author: Buzz Kim
         * @Date: 03/10/2018 5:51 PM
         * @param
         * @Description: Initialize toolbar
         *
         */
        Toolbar mToolbar = findViewById(R.id.toolbar);
        TextView mToolBarTextView = findViewById(R.id.text_view_toolbar_title);
        mToolBarTextView.setGravity(Gravity.START);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        mToolbar.setNavigationIcon(R.drawable.btn_back);
        mToolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        if (editable) {
            mToolBarTextView.setText("编辑笔记");
        }
        else{
            mToolBarTextView.setText("预览笔记");
        }
        mToolBarTextView.setTextColor(getResources().getColor(R.color.white));

    }

    public void initBottombar() {
        /**
         * @Author: Buzz Kim
         * @Date: 03/10/2018 5:52 PM
         * @param
         * @Description: Initialize bottom bar, for choices of multimedia
         *
         */
        bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.navigation_view);
        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED_NO_TITLE)
                .setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_DEFAULT);
        bottomNavigationBar.setBarBackgroundColor(R.color.colorPrimaryDark);
        bottomNavigationBar.setInActiveColor(R.color.colorPrimary);
        bottomNavigationBar.setActiveColor(R.color.white);
        bottomNavigationBar.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position) {
                chooseTab(position);
            }

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {
                chooseTab(position);
            }
        });
        bottomNavigationBar.addItem(new BottomNavigationItem(R.drawable.text_icon, "Text"))
                .addItem(new BottomNavigationItem(R.drawable.camera_icon, "Image"))
                .addItem(new BottomNavigationItem(R.drawable.mic_icon, "Voice"))
                .addItem(new BottomNavigationItem(R.drawable.video_icon,"Video"))
                .addItem(new BottomNavigationItem(R.drawable.save_icon, "Save")).initialise();

//         Hide bottombar when keyboard is hiding
        KeyboardChangeListener softKeyboardStateHelper = new KeyboardChangeListener(this);
        softKeyboardStateHelper.setKeyBoardListener(new KeyboardChangeListener.KeyBoardListener() {
            @Override
            public void onKeyboardChange(boolean isShow, int keyboardHeight) {
                if (isShow) {
                    bottomNavigationBar.hide();
                } else {
                    //键盘的收起
                    bottomNavigationBar.show();
                }
            }

        });
    }

    public void chooseTab(int pos) {
        /**
         * @Author: Buzz Kim
         * @Date: 09/10/2018 1:53 PM
         * @param pos
         * @Description: function for tabSelected and tabUnselected
         *
         */
        if(!editable)
            return;
        switch(pos) {
            case 0:
                myViewGroup.onNewTextEvent();
                myViewGroup.setLastEditTextFocus();
                break;
            case 1:
                requestPermissionsForPhoto();
                break;
            case 2:
                onAudioButtonClicked();
                break;
            case 3:
                requestPermissionsForVideo();
                break;
            case 4:
                saveNote();
                finish();
                break;
            default:
                break;
        }
    }

    public void onAudioButtonClicked()
    {
        if(!isRecording)
        {
            tryStartRecording();
        }
        else
        {
            try {
                stopRecording();
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("error initView() : ", e.toString());
            }
        }
    }

    public void tryStartRecording()
    {
        AudioDialog().show();
    }

    public void stopRecording()
    {
        recordEndTime=System.currentTimeMillis();
        String latestAudioLocation=AudioFetcher.stopRecording(recordEndTime-recordStartTime);
        Toast.makeText(NoteEditActivity.this, String.format("已保存录音，时长%d秒，开始转换为文字，请耐心等待",(recordEndTime-recordStartTime)/1000), Toast.LENGTH_SHORT).show();
        isRecording=false;
        isDeamonRecording=false;
        myViewGroup.addViewtoCursor(new LBAudioView(
                latestAudioLocation,
                NoteEditActivity.this,
                null,
                true
        ));
        startDeamonRecording();
    }

    public AlertDialog AudioDialog() {
        final String[] dialogItems = {"现在", "15秒之前","30秒之前","60秒之前"};
        AlertDialog dialog = new AlertDialog.Builder(this)
        .setTitle("开始录音时间")
        .setItems(dialogItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: recordStartSecondsAgo=0; break; case 1: recordStartSecondsAgo=15;break;case 2: recordStartSecondsAgo=30;break;case 3: recordStartSecondsAgo=60;break;default: recordStartSecondsAgo=-1;break; }
                AudioDialogChoosed();
            }
        }).create();
        return dialog;
    }

    public void AudioDialogChoosed()
    {
        if(recordStartSecondsAgo==-1)
            return;
        recordStartTime=System.currentTimeMillis()-recordStartSecondsAgo*1000;
        if(recordStartTime<deamonRecordStartTime)
        {
            recordStartTime=deamonRecordStartTime;
            recordStartSecondsAgo=(int)((System.currentTimeMillis()-recordStartTime)/1000);
        }
        switch(recordStartSecondsAgo)
        {
            case 0:
                Toast.makeText(NoteEditActivity.this, "已开始录音", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(NoteEditActivity.this, String.format("已从%d秒前开始录音",recordStartSecondsAgo), Toast.LENGTH_SHORT).show();
        }
        if(!test) {
            AudioFetcher.startRecording();
        }
        isRecording=true;
    }

    public void onPhotoButtonClicked() {
        /**
         * @Author: Buzz Kim
         * @Date: 08/10/2018 1:49 PM
         * @param
         * @Description: Open the album or camera of user`s phone
         *
         */
        AlertDialog.Builder alb = new AlertDialog.Builder(NoteEditActivity.this);
        alb.setTitle("获取照片方式");
        final String[] methods = { "从图库导入", "打开照相机" };
        alb.setItems(methods, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                /**
                 * i == 0: from system album;
                 * i == 1: from system camera;
                 * */
                switch(i) {
                    case 0:
                        Intent albumIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        albumIntent.addCategory(Intent.CATEGORY_OPENABLE);
                        albumIntent.setType("image/*");
                        startActivityForResult(albumIntent, photoFromGalleryCode);
                        break;
                    case 1:
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        File imageFile = new File("data/data/com.littleboss.smartnote/resources/images/" + timeStamp + ".jpg");
                        imageFile.getParentFile().mkdirs();
                        latestCameraResultPath = imageFile.getAbsolutePath();
                        if (imageFile != null) {
                            takePictureIntent.putExtra(
                                    MediaStore.EXTRA_OUTPUT,
                                    FileProvider.getUriForFile(
                                            thisActivity,
                                            "com.littleboss.smartnote.fileprovider",
                                            imageFile
                                    )
                            );
                            startActivityForResult(takePictureIntent, photoFromCameraCode);
                        }

                        break;
                    default:
                        break;
                }
            }
        });
        alb.show();
    }

    public void onVideoButtonClicked() {
        AlertDialog.Builder alb = new AlertDialog.Builder(NoteEditActivity.this);
        alb.setTitle("获取录像方式");
        final String[] methods = { "从图库导入", "打开摄像机" };
        alb.setItems(methods, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch(i) {
                    case 0:
                        // Open an image from system album
                        Intent intent;
                        intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("video/*");
                        startActivityForResult(intent, videoFromGalleryCode);
                        break;
                    case 1:
                        takeVideo();
                        break;
                    default:
                        break;
                }
            }
        });
        alb.show();
    }

    public void takeVideo()
    {
        if(!isRecording)
            stopDeamonRecording();
        else{
            stopRecording();
            stopDeamonRecording();
        }
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        File videoFile = null;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        videoFile = new File("data/data/com.littleboss.smartnote/resources/videos/" + timeStamp + ".avi");
        videoFile.getParentFile().mkdirs();
        latestCameraResultPath = videoFile.getAbsolutePath();
        if (videoFile != null) {
            takeVideoIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    FileProvider.getUriForFile(
                            thisActivity,
                            "com.littleboss.smartnote.fileprovider",
                            videoFile
                    )
            );
            startActivityForResult(takeVideoIntent, videoFromCameraCode);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if(!isDeamonRecording)
        {
            startDeamonRecording();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK)
        {
            if(requestCode == photoFromGalleryCode) {
                Uri originalUri = data.getData();
                // FIXME: 2018/11/13 Added if condition for testing, I m not sure it would or not effect on App
                if(originalUri != null) {
                    String img_path = UriParser.getPath(
                            NoteEditActivity.this,
                            originalUri
                    );
                    LBImageView added = new LBImageView(img_path, NoteEditActivity.this);
                    this.myViewGroup.addViewtoCursor(added);
                }
            }
            else if(requestCode == photoFromCameraCode) {
                this.myViewGroup.addViewtoCursor(new LBImageView(latestCameraResultPath ,NoteEditActivity.this));
            }
            else if(requestCode == videoFromGalleryCode){
                Uri originalUri = data.getData();
                this.myViewGroup.addViewtoCursor(new LBVideoView(UriParser.getPath(NoteEditActivity.this,originalUri),NoteEditActivity.this));
            }
            else if(requestCode == videoFromCameraCode) {
                this.myViewGroup.addViewtoCursor(new LBVideoView(latestCameraResultPath,NoteEditActivity.this));
            }
        }
    }

    public void requestPermissionsForPhoto() {
        /**
         * @Author: Buzz Kim
         * @Date: 08/10/2018 8:01 PM
         * @param
         * @Description: Request for SDcard permissions
         */
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        } else {
            onPhotoButtonClicked();
        }
    }

    public void requestPermissionsForVideo() {
        /**
         * @Author: Buzz Kim
         * @Date: 08/10/2018 8:01 PM
         * @param
         * @Description: Request for SDcard permissions
         */
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        } else {
            onVideoButtonClicked();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onPhotoButtonClicked();
                } else {
                    Toast.makeText(this, "权限被拒绝了。。。", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    public void saveNote() {
        /**
         * @Author: Buzz Kim
         * @Date: 03/10/2018 8:57 PM
         * @param
         * @Description: Save note into database
         *
         */

        new Thread(new Runnable() {
            @Override
            public void run() {
                EditText et_title = findViewById(R.id.et_new_title);
                String title = et_title.getText().toString();
                String content = myViewGroup.toDataString();

                if(newCreatedFlag) {
                    try {
                        noteDatabase.saveNoteByTitle("", title, content,null);
                    } catch (Exception e) {
                        //e.printStackTrace();
                        Log.i("err saveNote() : ", e.toString());
                    }
                } else {
                    try {
                        noteDatabase.saveNoteByTitle(_title, title, content,null);
                    } catch (Exception e) {
                        //e.printStackTrace();
                        Log.i("err saveNote() : ", e.toString());
                    }
                }
            }
        }).start();
    }

    public void performbackbuttonclick() {
        onBackPressed();
    }

    public void setOldTitle(String title) {
        this.old_title = title;
        this._title = title;
    }

    public AlertDialog backPressedDialog() {
        final AlertDialog alertdialog = new AlertDialog.Builder(this)
                .setTitle("警告")
                .setMessage("是否保存修改的内容？")
                .setPositiveButton("保存",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                saveNote();
                                if (!isRecording)
                                    stopDeamonRecording();
                                else
                                    Toast.makeText(NoteEditActivity.this, "后台持续录音", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                .setNeutralButton("取消",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                .setNegativeButton("放弃",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (!isRecording)
                                    stopDeamonRecording();
                                else
                                    Toast.makeText(NoteEditActivity.this, "后台持续录音", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }).create();
        return alertdialog;
    }

    public void setNewCreatedFlag(boolean set) {
        this.newCreatedFlag = set;
    }

    public void setIsRecording(boolean set) {
        this.isRecording = set;
    }

    public void setTest(boolean set) {
        this.test = set;
    }
}
