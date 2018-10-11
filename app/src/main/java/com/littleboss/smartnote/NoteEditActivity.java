package com.littleboss.smartnote;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment;
import com.yalantis.contextmenu.lib.MenuObject;
import com.yalantis.contextmenu.lib.MenuParams;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemClickListener;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemLongClickListener;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.littleboss.smartnote.Utils.*;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class NoteEditActivity extends AppCompatActivity implements OnMenuItemClickListener, OnMenuItemLongClickListener {

    private Bitmap bitmap;

    private FragmentManager fragmentManager;
    private ContextMenuDialogFragment mMenuDialogFragment;

    private String _title;
    private String _content;
    private String old_title;
    private String old_content;

    private boolean flag;

    private NoteDatabase noteDatabase;

    public void dealString(String result) {
        ////
        EditText et_content = (EditText) findViewById(R.id.et_new_content);
        et_content.setText(et_content.getText() + result);
    }

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
        flag = getIntent().getBooleanExtra("newCreatedNote", true);

        fragmentManager = getSupportFragmentManager();
        initToolbar();
        initMenuFragment();
        initBottombar();
        initScrollButton();
        initEditText();

        // must set client-activity first
        SpeechUtility.createUtility(this, SpeechConstant.APPID +getString(R.string.APPID));
        AudioInterface.setEnvActivity(this);
    }

    private void initScrollButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.goToTop);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ScrollView sc = (ScrollView) findViewById(R.id.sc);
                sc.fullScroll(ScrollView.FOCUS_UP);
            }
        });
    }

    private void initEditText() {
        EditText et_title = (EditText) findViewById(R.id.et_new_title);
        EditText et_content = (EditText) findViewById(R.id.et_new_content);
        if(!flag) {
            _content = noteDatabase.getNotesByTitle(_title);
            et_title.setText(_title);
            Pattern p = Pattern.compile("\\<img src=\".*?\" \\/\\>");
            Matcher m = p.matcher(_content);
            SpannableString ss = new SpannableString(_content);
            while(m.find()) {
                Log.d("RGX", m.group());
                String tagPath = m.group();
                String s = m.group();
                int start = m.start();
                int end = m.end();
                String path = s.replaceAll("\\<img src=\"|\" \\/\\>","").trim();
                Bitmap ori_b = BitmapFactory.decodeFile(path);
                ImageSpan span = new ImageSpan(ori_b);
                ss.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            et_content.setText(ss);
        } else {
            _content = et_content.getText().toString();   // for judgement in noteModified(), _content will be null without this sentence
        }
        old_title = _title;
        old_content = _content;
    }

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
        final AlertDialog alertdialog = new AlertDialog.Builder(this).setTitle("Go back???").setMessage("Are you sure???").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).create();
        alertdialog.show();
    }

    public boolean noteModified() {
        EditText et_title = (EditText) findViewById(R.id.et_new_title);
        EditText et_content = (EditText) findViewById(R.id.et_new_content);
        String t = et_title.getText().toString();
        String c = et_content.getText().toString();
        if(old_title.equals(et_title.getText().toString()) && old_content.equals(et_content.getText().toString())) {
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
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mToolBarTextView.setText("NoteEditActivity");
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
                Toast.makeText(NoteEditActivity.this, "Choosed text", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Toast.makeText(NoteEditActivity.this, "Choosed image", Toast.LENGTH_SHORT).show();
                requestPermissions();
                break;
            case 2:
                Toast.makeText(NoteEditActivity.this, "Choosed voice", Toast.LENGTH_SHORT).show();
                AudioInterface.listen();
                break;
            case 3:
                Toast.makeText(NoteEditActivity.this, "Choosed video", Toast.LENGTH_SHORT).show();
                break;
            case 4:
                Toast.makeText(NoteEditActivity.this, "Choosed save", Toast.LENGTH_SHORT).show();
                saveNote();
                finish();
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
        EditText et_title = (EditText) findViewById(R.id.et_new_title);
        EditText et_content = (EditText) findViewById(R.id.et_new_content);
        String title = et_title.getText().toString();
        String content = et_content.getText().toString();

        if(flag) {
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
                switch(i) {
                    case 0:
                        // Open an image from system album

                        Intent albumIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        albumIntent.addCategory(Intent.CATEGORY_OPENABLE);
                        albumIntent.setType("image/*");
                        startActivityForResult(albumIntent, 0x111);
                        break;
                    case 1:
                        // Open the system camera
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if(cameraIntent.resolveActivity(getPackageManager()) != null) {

                        } else {
                            Toast.makeText(getApplicationContext(), "No camera", Toast.LENGTH_SHORT).show();
                        }
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
        ContentResolver resolver = getContentResolver();
        EditText et = (EditText) findViewById(R.id.et_new_content);
        if(requestCode == 0x111 && resultCode == RESULT_OK) {
            Uri originalUri = data.getData();
            Bitmap ori_bitmap = null;
            Bitmap ori_rbitmap = null;
            try {
                ori_bitmap = BitmapFactory.decodeStream(resolver.openInputStream(originalUri));
                ori_rbitmap = ImageUtils.resizeImage(ori_bitmap, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String sdStatus = Environment.getExternalStorageState();
            if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
                Log.i("Testfile", "Can`t use sdcard");
            }

            String name = Calendar.getInstance(Locale.CHINA).getTimeInMillis() + ".jpg";
            FileOutputStream fout = null;
            File rootfile = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS);
            String tempFilePath = rootfile.getPath() + File.separator + "test" + File.separator;
            File tempFile = new File(tempFilePath);
            if(!tempFile.exists()) {
                tempFile.mkdirs();
            } else {
                Log.i("FilePath", "tmpfile exists");
            }

            // TODO: 08/10/2018 Change a filepath to save the image
            String filename = rootfile.getPath() + File.separator + "test" + File.separator + name;
            try {
                fout = new FileOutputStream(filename);
                ori_rbitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fout.flush();
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            String myPath = filename;
            String tagPath = "<img src=\"" + filename + "\" " + "/>";
            Log.w(myPath, filename);
            Toast.makeText(this, myPath, Toast.LENGTH_SHORT);
            SpannableString span_str = new SpannableString(tagPath);
            Bitmap my_bitmap = BitmapFactory.decodeFile(myPath);
            Bitmap my_rbitmap = ImageUtils.resizeImage(my_bitmap, 0);
            ImageSpan span = new ImageSpan(my_rbitmap);
            span_str.setSpan(span, 0, tagPath.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            Editable ed = et.getText();
            int start = et.getSelectionStart();
            ed.insert(start, span_str);
            et.setText((CharSequence) ed);
            et.setSelection(start + span_str.length());
        }
        // TODO: 08/10/2018 Need to complete camera part 
    }

    private void insertPic(Bitmap bitmap, final int index) {

    }

    private void requestPermissions() {
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
}
