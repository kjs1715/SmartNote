package com.littleboss.smartnote;


import android.app.Activity;
import android.content.Context;
import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

import com.littleboss.smartnote.Utils.DateUtils;

class ListData {
    String title;
    Date createDate, modifyDate;
    List<Tag> tagList;
    ListData(String title, Date createDate, Date modifyDate, String tagListString) {
        this.title = title;
        this.createDate = createDate;
        this.modifyDate = modifyDate;
        this.tagList=Tag.getTagList(tagListString);

    }
    String tagListString()
    {
        StringBuilder stringBuilder=new StringBuilder("");
        for(Iterator<Tag> iterator = tagList.iterator();iterator.hasNext();)
        {
            stringBuilder.append(iterator.next().name);
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }
}

public class MainActivity extends AppCompatActivity{

    private static final int NOSELECT_STATE = -1;// 表示未选中任何CheckBox
    LinkedList<ListData> notesList;
    private List<Map<String, String>> listitem;
    private ListView listView;
    Handler handler;
    Runnable listGenerate;
    NoteDatabase noteDatabase;


    private Button bt_cancel, bt_delete;
    private TextView tv_sum;
    private LinearLayout linearLayout;
    private LinkedList<ListData> list_delete = new LinkedList();// 需要删除的数据
    private boolean isMultiSelect = false;// 是否处于多选状态
    FloatingActionButton fab;
    MyAdapter adapter;

    private final int WRITE_EXTERNAL_STORAGE_ID = 0;
    private final int RECORD_AUDIO_ID = 1;
    private final int CAMERA_ID = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NoteEditActivity.class);
                intent.putExtra("id", "");
                intent.putExtra("newCreatedNote", true);
                startActivity(intent);
            }
        });

        isMultiSelect=false;

        setTitle("会议速记助手");

        noteDatabase=NoteDatabase.getInstance();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_ID
            );
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_ID
            );
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.CAMERA},
                    CAMERA_ID
            );
        }


        handler=new Handler();

        final Runnable runnableUi=new  Runnable(){
            @Override
            public void run() {

                listView = findViewById(R.id.mainlist);
                listView.setClickable(true);
                adapter = new MyAdapter(MainActivity.this, notesList, NOSELECT_STATE);
                listView.setAdapter(adapter);
            }
        };

        listGenerate=new Runnable(){
            @Override
            public void run () {
                listitem = new ArrayList<>();
                int len = notesList.size();
                for (int i = 0; i < len; i++) {
                    HashMap<String, String> showitem = new HashMap<>();
                    showitem.put("title", notesList.get(i).title);
                    listitem.add(showitem);
                }
                handler.post(runnableUi);
            }
        };

        new Thread(new Runnable(){
            @Override
            public void run()
            {
                notesList=noteDatabase.getNotesTitleList();
                if(notesList!=null)
                    new Thread(listGenerate).start();
            }
        }).start();

        bt_cancel = (Button) findViewById(R.id.bt_cancel);
        bt_delete = (Button) findViewById(R.id.bt_delete);
        tv_sum = (TextView) findViewById(R.id.tv_sum);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

        bt_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("bt_cancel clicked");
                hideLinearLayout();
                isMultiSelect = false;
            }
        });
        bt_delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("bt_delete clicked");
                try {
                    noteDatabase.deleteNotesTitleList(list_delete);
                } catch (NoteNotExistException e) {
                    e.printStackTrace();
                }
                hideLinearLayout();
                readListandFlush();
                isMultiSelect = false;
            }
        });
    }

    public void sortNotesList(int type) {
        switch(type) {
            case 0:                         // by time
                Collections.sort(notesList, new Comparator<ListData>() {
                    @Override
                    public int compare(ListData o1, ListData o2) {
                        return Compare(0, o1, o2);
                    }
                });
                break ;
            case 1:
                Collections.sort(notesList, new Comparator<ListData>() {
                    @Override
                    public int compare(ListData o1, ListData o2) {
                        return Compare(1, o1, o2);
                    }
                });
                break ;
            case 2:
                Collections.sort(notesList, new Comparator<ListData>() {
                    @Override
                    public int compare(ListData o1, ListData o2) {
                        return Compare(2, o1, o2);
                    }
                });
                break;
            default :
                break;
        }
        flushList();
    }

    public AlertDialog sortDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] dialogItems = { "按创建时间排序","按修改时间排序", "按名称排序" };
        builder.setItems(dialogItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sortNotesList(which);
            }
        });
        return builder.show();
    }

    public static AlertDialog enterNoteDialog(int position, List<ListData> notesList, Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final String[] dialogItems = { "编辑","预览","修改标签"};
        builder.setItems(dialogItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0 || which==1)
                {
                    Intent intent = new Intent(activity, NoteEditActivity.class);
                    intent.putExtra("id", (notesList.get(position)).title);
                    intent.putExtra("newCreatedNote", false);
                    intent.putExtra("justsee", which);
                    activity.startActivity(intent);
                }
                else
                {
                    Intent intent = new Intent(activity, TagEditActivity.class);
                    intent.putExtra("id", (notesList.get(position)).title);
                    activity.startActivity(intent);
                }
            }
        });
        return builder.show();
    }

    protected void showLinearLayout()
    {
        linearLayout.setVisibility(View.VISIBLE);
        tv_sum.setText("共选择了" + list_delete.size() + "项");
        adapter.showAllItems();
        adapter.notifyDataSetChanged();
        fab.setVisibility(View.GONE);
    }

    protected void hideLinearLayout()
    {
        adapter.hideAllItems();
        adapter.notifyDataSetChanged();
        linearLayout.setVisibility(View.GONE);
        fab.setVisibility(View.VISIBLE);

    }

    protected void readListandFlush()
    {
        new Thread(new Runnable(){
            @Override
            public void run()
            {
                notesList=noteDatabase.getNotesTitleList();
                if(notesList!=null)
                    new Thread(listGenerate).start();
            }
        }).start();
    }

    protected void flushList()
    {
        new Thread(new Runnable(){
            @Override
            public void run()
            {
                if(notesList!=null)
                    new Thread(listGenerate).start();
            }
        }).start();
    }

    @Override
    protected void onResume()
    {
        readListandFlush();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mainactivity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
            if(isMultiSelect)
            {
                Toast.makeText(MainActivity.this.getApplicationContext(),"请先退出多选模式",Toast.LENGTH_SHORT).show();
            }
            else
            {
                if (id == R.id.searchitem) {
                    Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                    startActivity(intent);
                }
                else if(id == R.id.sortitem) {
                    sortDialog();
                }
                else if(id == R.id.tagselect) {
                    Intent intent = new Intent(MainActivity.this, TagSelectActivity.class);
                    startActivity(intent);
                }
            }
        return super.onOptionsItemSelected(item);
    }

    class MyAdapter extends BaseAdapter{
        public HashMap<Integer, Integer> isCheckBoxVisible;// 用来记录是否显示checkBox
        public HashMap<Integer, Boolean> isChecked;// 用来记录是否被选中
        private LayoutInflater inflater;
        private List<ListData> list;

        public MyAdapter(Context context, List<ListData> list, int position) {
            inflater = LayoutInflater.from(context);
            this.list = notesList;
            isCheckBoxVisible = new HashMap<Integer, Integer>();
            isChecked = new HashMap<Integer, Boolean>();
            // 如果处于多选状态，则显示CheckBox，否则不显示
            if (isMultiSelect) {
                for (int i = 0; i < list.size(); i++) {
                    isCheckBoxVisible.put(i, CheckBox.VISIBLE);
                    isChecked.put(i, false);
                }
            } else {
                for (int i = 0; i < list.size(); i++) {
                    isCheckBoxVisible.put(i, CheckBox.GONE);
                    isChecked.put(i, false);
                }
            }

            // 如果长按Item，则设置长按的Item中的CheckBox为选中状态
            if (isMultiSelect && position >= 0) {
                isChecked.put(position, true);
            }
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class onMyLongClick implements OnLongClickListener {

            private int position;
            private List<ListData> list;

            // 获取数据，与长按Item的position
            public onMyLongClick(int position, List<ListData> list) {
                this.position = position;
                this.list = list;
            }

            // 在长按监听时候，切记将监听事件返回ture
            @Override
            public boolean onLongClick(View v) {
                if(isMultiSelect)
                {
                    return false;
                }
                else {
                    isMultiSelect = true;
                    list_delete.clear();
                    list_delete.add(notesList.get(position));
                    adapter.setOneHot(position);
                    adapter.showAllItems();
                    adapter.notifyDataSetChanged();
                    showLinearLayout();
                    return true;
                }
            }
        }

        class onMyClick implements OnClickListener {

            private int position;
            private List<ListData> list;

            // 获取数据，与长按Item的position
            public onMyClick(int position, List<ListData> list) {
                this.position = position;
                this.list = list;
            }

            @Override
            public void onClick(View view) {
                if(isMultiSelect)
                {
                    if(adapter.isChecked(position))
                    {
                        list_delete.remove(adapter.getNotesTitle(position));
                    }
                    else
                    {
                        list_delete.add(adapter.getNotesTitle(position));
                    }
                    adapter.switchIsChecked(position);
                    adapter.notifyDataSetChanged();
                    tv_sum.setText("共选择了" + list_delete.size() + "项");
                }
                else
                {
                    enterNoteDialog(position,notesList,MainActivity.this);
                }
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;

            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_main, null);
                viewHolder.tv_Name    = convertView.findViewById(R.id.notetitle);
                viewHolder.cb         = convertView.findViewById(R.id.item_check);
                viewHolder.createDate = convertView.findViewById(R.id.createDate);
                viewHolder.modifyDate = convertView.findViewById(R.id.modifyDate);
                viewHolder.tags       = convertView.findViewById(R.id.tagTextView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final String str = list.get(position).title;
            viewHolder.tv_Name.setText(str);

            // 根据position设置CheckBox是否可见，是否选中
            viewHolder.cb.setChecked(isChecked.get(position));
            viewHolder.cb.setVisibility(isCheckBoxVisible.get(position));

            String modified = DateUtils.display(list.get(position).modifyDate);
            String created = DateUtils.display(list.get(position).createDate);
            String tags = list.get(position).tagListString();
            viewHolder.modifyDate.setText("上次修改: " + modified);
            viewHolder.createDate.setText("创建时间: " + created);
            viewHolder.tags.setText(tags);

            convertView.setOnLongClickListener(new onMyLongClick(position,list));
            convertView.setOnClickListener(new onMyClick(position,list));

            return convertView;
        }

        public void hideAllItems()
        {
            int len=list.size();
            for(int i=0;i<len;i++)
            {
                isCheckBoxVisible.put(i,View.GONE);
            }
        }

        public void showAllItems()
        {
            int len=list.size();
            for(int i=0;i<len;i++)
            {
                isCheckBoxVisible.put(i,View.VISIBLE);
            }
        }

        public void setOneHot(int position)
        {
            int len=list.size();
            for(int i=0;i<len;i++)
            {
                if(i==position)
                    isChecked.put(i,true);
                else
                    isChecked.put(i,false);
            }
        }

        public void switchIsChecked(int position)
        {
            isChecked.put(position,!isChecked.get(position));
        }

        public boolean isChecked(int position)
        {
            return isChecked.get(position);
        }

        public ListData getNotesTitle(int index)
        {
            return list.get(index);
        }

        class ViewHolder {
            public TextView tv_Name;
            public CheckBox cb;
            public TextView createDate;
            public TextView modifyDate;
            public TextView tags;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            System.exit(0);

        }
    }

    public int Compare(int type, ListData o1, ListData o2) {
        if(type == 0) {
            if(o1.createDate.before(o2.createDate)) {
                return 1;
            } else if (o2.createDate.before(o1.createDate)) {
                return -1;
            } else {
                return 0;
            }
        } else if(type == 1) {
            if(o1.modifyDate.before(o2.modifyDate)) {
                return 1;
            } else if (o2.modifyDate.before(o1.modifyDate)) {
                return -1;
            } else {
                return 0;
            }
        } else {
            return o1.title.compareTo(o2.title);
        }
    }

    public void isMultiselected() {
        this.isMultiSelect = true;
    }
}
