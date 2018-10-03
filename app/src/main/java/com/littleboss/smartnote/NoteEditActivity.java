package com.littleboss.smartnote;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.util.ArrayList;
import java.util.List;

import com.littleboss.smartnote.NoteDatabase;

public class NoteEditActivity extends AppCompatActivity implements OnMenuItemClickListener, OnMenuItemLongClickListener {

    private FragmentManager fragmentManager;
    private ContextMenuDialogFragment mMenuDialogFragment;

    private String _title;
    private String _content;
    private boolean flag;

    private NoteDatabase noteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        getDir("databases", MODE_PRIVATE);
        noteDatabase = NoteDatabase.getInstance();

        _title = getIntent().getStringExtra("id");
        flag = getIntent().getBooleanExtra("newCreatedNote", true);

//         testing Database
//        _title = "abcde";
//        flag = true;

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
                final ScrollView sc = (ScrollView) findViewById(R.id.sc);
                sc.fullScroll(ScrollView.FOCUS_UP);
            }
        });
    }

    private void initEditText() {
        EditText et_title = (EditText) findViewById(R.id.et_new_title);
        EditText et_content = (EditText) findViewById(R.id.et_new_content);
        if(flag) {
            return ;
        } else {
            _content = noteDatabase.getNotesByTitle(_title);
            et_title.setText(_title);
            et_content.setText(_content);
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
        BottomNavigationBar bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.navigation_view);
        bottomNavigationBar.addItem(new BottomNavigationItem(R.drawable.text_icon, "Text"))
                           .addItem(new BottomNavigationItem(R.drawable.camera_icon, "Image"))
                           .addItem(new BottomNavigationItem(R.drawable.mic_icon, "Voice"))
                           .addItem(new BottomNavigationItem(R.drawable.video_icon,"Video"))
                           .addItem(new BottomNavigationItem(R.drawable.save_icon, "Save")).initialise();
        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED_NO_TITLE)
                           .setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_DEFAULT);
        bottomNavigationBar.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position) {
                switch(position) {
                    case 0:
                        Toast.makeText(NoteEditActivity.this, "Choosed text", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Toast.makeText(NoteEditActivity.this, "Choosed image", Toast.LENGTH_SHORT).show();
                        getImage();
                        break;
                    case 2:
                        Toast.makeText(NoteEditActivity.this, "Choosed voice", Toast.LENGTH_SHORT).show();
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

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {

            }
        });
    }

    private void saveNote() {
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

    private void getImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, 0);
    }
}
