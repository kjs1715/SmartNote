package com.littleboss.smartnote;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.inputmethodservice.KeyboardView;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

public class NoteEditActivity extends AppCompatActivity implements OnMenuItemClickListener, OnMenuItemLongClickListener {

    private Bitmap bitmap;

    private FragmentManager fragmentManager;
    private ContextMenuDialogFragment mMenuDialogFragment;

    private String _title;
    private String _content;
    private String old_title;
    private String old_content;

    private boolean newCreatedFlag;

    private NoteDatabase noteDatabase;

    private LBAbstractViewGroup myViewGroup;

    private Uri imageTakeUri;
    private boolean isRecording=false;


    public void show(Dialog dialog) {
        dialog.show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        getDir("databases", MODE_PRIVATE);
        noteDatabase = NoteDatabase.getInstance();

        _title = getIntent().getStringExtra("id");
        newCreatedFlag = getIntent().getBooleanExtra("newCreatedNote", true);

        fragmentManager = getSupportFragmentManager();

        initToolbar();
        initMenuFragment();
        initBottombar();
        initScrollButton();
        initEditText();

    }

    private void initScrollButton() {
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

    private void initEditText() {
        myViewGroup=findViewById(R.id.viewgroup);
        EditText et_title = (EditText) findViewById(R.id.et_new_title);
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
    private void initMenuFragment() {
        MenuParams menuParams = new MenuParams();
        menuParams.setActionBarSize((int) getResources().getDimension(R.dimen.tool_bar_height));
        menuParams.setMenuObjects(getMenuObjects());
        menuParams.setClosableOutside(false);
        mMenuDialogFragment = ContextMenuDialogFragment.newInstance(menuParams);
        mMenuDialogFragment.setItemClickListener(this);
        mMenuDialogFragment.setItemLongClickListener(this);
    }

    private List<MenuObject> getMenuObjects() {
        // You can use any [resource, bitmap, drawable, color] as image:
        // item.setResource(...)
        // item.setBitmap(...)
        // item.setDrawable(...)
        // item.setColor(...)
        // You can set image ScaleType:
        // item.setScaleType(ScaleType.FIT_XY)
        // You can use any [resource, drawable, color] as background:
        // item.setBgResource(...)
        // item.setBgDrawable(...)
        // item.setBgColor(...)
        // You can use any [color] as text color:
        // item.setTextColor(...)
        // You can set any [color] as divider color:
        // item.setDividerColor(...)

        List<MenuObject> menuObjects = new ArrayList<>();

        MenuObject close = new MenuObject();
        close.setResource(R.drawable.icn_close);

        MenuObject send = new MenuObject("Share to");
        send.setResource(R.drawable.icn_1);

        MenuObject like = new MenuObject("Like profile");
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.icn_2);
        like.setBitmap(b);

        MenuObject addFr = new MenuObject("Add to friends");
        BitmapDrawable bd = new BitmapDrawable(getResources(),
                BitmapFactory.decodeResource(getResources(), R.drawable.icn_3));
        addFr.setDrawable(bd);

        MenuObject addFav = new MenuObject("Add to favorites");
        addFav.setResource(R.drawable.icn_4);


        menuObjects.add(close);
        menuObjects.add(send);
        menuObjects.add(like);
        menuObjects.add(addFr);
        menuObjects.add(addFav);

        return menuObjects;
    }

    protected void addFragment(Fragment fragment, boolean addToBackStack, int containerId) {
        invalidateOptionsMenu();
        String backStackName = fragment.getClass().getName();
        boolean fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0);
        if (!fragmentPopped) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(containerId, fragment, backStackName)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            if (addToBackStack)
                transaction.addToBackStack(backStackName);
            transaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.context_menu:
                if (fragmentManager.findFragmentByTag(ContextMenuDialogFragment.TAG) == null) {
                    mMenuDialogFragment.show(fragmentManager, ContextMenuDialogFragment.TAG);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(!noteModified()) {
            finish();
            return ;
        }
        final AlertDialog alertdialog = new AlertDialog.Builder(this)
                .setTitle("警告")
                .setMessage("是否保存修改的内容？")
        .setPositiveButton("保存",
        new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                saveNote();
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
                finish();
            }
        }).create();
        alertdialog.show();
    }

    public boolean noteModified() {
        EditText et_title = (EditText) findViewById(R.id.et_new_title);
//        EditText et_content = (EditText) findViewById(R.id.et_new_content);
        String t = et_title.getText().toString();
//        String c = et_content.getText().toString();
        String c = this.myViewGroup.toDataString();
        if(old_title.equals(t) && old_content.equals(c)) {
            return false;
        }
        return true;
    }

    @Override
    public void onMenuItemClick(View clickedView, int position) {
        Toast.makeText(this, "Clicked on position: " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMenuItemLongClick(View clickedView, int position) {
        Toast.makeText(this, "Long clicked on position: " + position, Toast.LENGTH_SHORT).show();
    }


    private void initToolbar() {
        /**
         * @Author: Buzz Kim
         * @Date: 03/10/2018 5:51 PM
         * @param
         * @Description: Initialize toolbar
         *
         */
        Toolbar mToolbar = findViewById(R.id.toolbar);
        TextView mToolBarTextView = findViewById(R.id.text_view_toolbar_title);
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
        mToolBarTextView.setText("编辑笔记");
        mToolBarTextView.setTextColor(getResources().getColor(R.color.white));

    }

    private void initBottombar() {
        /**
         * @Author: Buzz Kim
         * @Date: 03/10/2018 5:52 PM
         * @param
         * @Description: Initialize bottom bar, for choices of multimedia
         *
         */
        final BottomNavigationBar bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.navigation_view);
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
        bottomNavigationBar.addItem(new BottomNavigationItem(R.drawable.camera_icon, "Image"))
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

    private void chooseTab(int pos) {
        /**
         * @Author: Buzz Kim
         * @Date: 09/10/2018 1:53 PM
         * @param pos
         * @Description: function for tabSelected and tabUnselected
         *
         */
        switch(pos) {
            case 0:
                Toast.makeText(NoteEditActivity.this, "Choosed image", Toast.LENGTH_SHORT).show();
                requestPermissionsForPhoto();
                break;
            case 1:
                Toast.makeText(NoteEditActivity.this, "Choosed voice", Toast.LENGTH_SHORT).show();
                if(!isRecording)
                {
                    AudioFetcher.startRecording();
                    isRecording=true;
                }
                else
                {
                    String latestAudioLocation=AudioFetcher.stopRecording();
                    isRecording=false;
                    myViewGroup.addViewtoCursor(new LBAudioView(
                            latestAudioLocation,
                            NoteEditActivity.this,
                            null
                    ));
                }
                break;
            case 2:
                Toast.makeText(NoteEditActivity.this, "Choosed video", Toast.LENGTH_SHORT).show();
                requestPermissionsForVideo();
                break;
            case 3:
                Toast.makeText(NoteEditActivity.this, "Choosed save", Toast.LENGTH_SHORT).show();
                saveNote();
                finish();
                break;
            default:
                break;
        }
    }

    private void onPhotoButtonClicked() {
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
                        // Open an image from system album

                        Intent albumIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        albumIntent.addCategory(Intent.CATEGORY_OPENABLE);
                        albumIntent.setType("image/*");
//                        albumIntent.setType("video/*");
                        startActivityForResult(albumIntent, 0x101);
                        break;
                    case 1:
//                        // Open the system camera
//                        Intent intent=new Intent();
//                        // 指定开启系统相机的Action
//                        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
//                        intent.addCategory(Intent.CATEGORY_DEFAULT);
//                        String filepath="/storage/emulated/0/DCIM/Camera/IMG_"+new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())+".jpg";
//                        File file=new File(filepath);
//                        // 把文件地址转换成Uri格式
//                        imageTakeUri=Uri.fromFile(file);
//                        // 设置系统相机拍摄照片完成后图片文件的存放地址
//                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageTakeUri);
//                        startActivityForResult(intent,0x102);

//                        if(checkSelfPermission(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
//                            requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},200);
//                            return;
//                        }
//                        Intent intent = new Intent();
//                        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
//                        File imagePath = new File(Context.getFilesDir(), "images");
//                        File newFile = new File(imagePath, "test.jpg");
//                        Uri contentUri = FileProvider.getUriForFile(NoteEditActivity.this, "com.littleboss.smartnote", newFile);
//                        intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
//                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                        startActivityForResult(intent,200);
                        break;
                    default:
                        break;
                }
            }
        });
        alb.show();
    }

    private void onVideoButtonClicked() {
        AlertDialog.Builder alb = new AlertDialog.Builder(NoteEditActivity.this);
        alb.setTitle("获取录像方式");
        final String[] methods = { "从图库导入", "打开摄像机" };
        alb.setItems(methods, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent;
                switch(i) {
                    case 0:
                        // Open an image from system album

                        intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
//                        albumIntent.setType("image/*");
                        intent.setType("video/*");
                        startActivityForResult(intent, 0x201);
                        break;
                    case 1:
//                         TODO Open the system camera
                        intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        String filepath="/storage/emulated/0/DCIM/Camera/VID_"+new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())+".mp4";
                        File file=new File(filepath);
                        // 把文件地址转换成Uri格式
                        Uri uri=Uri.fromFile(file);
                        // 设置系统相机拍摄照片完成后图片文件的存放地址
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        startActivityForResult(intent,0x202);
                        break;
                    default:
                        break;
                }
            }
        });
        alb.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println(data==null);
        if(resultCode==RESULT_OK)
        {
            if(requestCode == 0x101) {
                Uri originalUri = data.getData();
                String img_path = UriParser.getPath(
                        NoteEditActivity.this,
                        originalUri
                );
                this.myViewGroup.addViewtoCursor(new LBImageView(img_path, NoteEditActivity.this));
            }
            else if(requestCode == 0x102) {
                this.myViewGroup.addViewtoCursor(new LBImageView(UriParser.getPath(NoteEditActivity.this,imageTakeUri),NoteEditActivity.this));
            }
            else if(requestCode == 0x201) {
                Uri originalUri = data.getData();
                this.myViewGroup.addViewtoCursor(new LBVideoView(UriParser.getPath(NoteEditActivity.this,originalUri),NoteEditActivity.this));
            }
            else if(requestCode == 0x202) {
                Uri originalUri = data.getData();
                this.myViewGroup.addViewtoCursor(new LBVideoView(UriParser.getPath(NoteEditActivity.this,originalUri),NoteEditActivity.this));
            }
        }
    }

    private void insertPic(Bitmap bitmap, final int index) {

    }

    private void requestPermissionsForPhoto() {
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

    private void requestPermissionsForVideo() {
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

    private void saveNote() {
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
                        noteDatabase.saveNoteByTitle("", title, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        noteDatabase.saveNoteByTitle(_title, title, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    public void initKeyboardListener() {

    }
}
