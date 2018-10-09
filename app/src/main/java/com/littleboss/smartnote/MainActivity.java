package com.littleboss.smartnote;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

import java.util.LinkedList;


public class MainActivity extends AppCompatActivity{

    private static final int NOSELECT_STATE = -1;// 表示未选中任何CheckBox
    LinkedList<String> notesList;
    private List<Map<String, String>> listitem;
    private ListView listView;
    Handler handler;
    Runnable listGenerate;
    NoteDatabase noteDatabase;


    private Button bt_cancel, bt_delete;
    private TextView tv_sum;
    private LinearLayout linearLayout;
    private LinkedList<String> list_delete = new LinkedList<String>();// 需要删除的数据
    private boolean isMultiSelect = false;// 是否处于多选状态
    FloatingActionButton fab;
    MyAdapter adapter;


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

        handler=new Handler();

        final Runnable runnableUi=new  Runnable(){
            @Override
            public void run() {
//                final SimpleAdapter myAdapter = new SimpleAdapter(MainActivity.super.getApplicationContext(), listitem,
//                        R.layout.item_main, new String[]{"title"},new int[]{R.id.notetitle}){
//                };

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
                    showitem.put("title", notesList.get(i));
                    listitem.add(showitem);
                }
//                list=new ArrayList<>(notesList);
                handler.post(runnableUi);
            }
        };

        new Thread(new Runnable(){
            @Override
            public void run()
            {
                notesList=noteDatabase.getNotesTitleList();
                if(notesList!=null&&notesList.size()>0)
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
                flushList();
                isMultiSelect = false;
            }
        });
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

    protected void flushList()
    {
        new Thread(new Runnable(){
            @Override
            public void run()
            {
                notesList=noteDatabase.getNotesTitleList();
                if(notesList!=null&&notesList.size()>0)
                    new Thread(listGenerate).start();
            }
        }).start();
    }

    @Override
    protected void onResume()
    {
        flushList();
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
        if (id == R.id.searchitem) {
            if(isMultiSelect)
            {
                Toast.makeText(MainActivity.this.getApplicationContext(),"请先确定是否删除笔记",Toast.LENGTH_SHORT).show();
            }
            else
            {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        }

        return super.onOptionsItemSelected(item);

    }

    class MyAdapter extends BaseAdapter{
        public HashMap<Integer, Integer> isCheckBoxVisible;// 用来记录是否显示checkBox
        public HashMap<Integer, Boolean> isChecked;// 用来记录是否被选中
        private LayoutInflater inflater;
        private List<String> list;


        public MyAdapter(Context context, List<String> list, int position) {
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
            private List<String> list;

            // 获取数据，与长按Item的position
            public onMyLongClick(int position, List<String> list) {
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
            private List<String> list;

            // 获取数据，与长按Item的position
            public onMyClick(int position, List<String> list) {
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
                    Intent intent = new Intent(MainActivity.this, NoteEditActivity.class);
                    intent.putExtra("id", (String)(notesList.get(position)));
                    intent.putExtra("newCreatedNote", false);
                    startActivity(intent);
                }
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_main, null);
                viewHolder.tv_Name = (TextView) convertView.findViewById(R.id.notetitle);
                viewHolder.cb = (CheckBox) convertView.findViewById(R.id.item_check);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final String str = list.get(position);
            viewHolder.tv_Name.setText(str);
            // 根据position设置CheckBox是否可见，是否选中
            viewHolder.cb.setChecked(isChecked.get(position));
            viewHolder.cb.setVisibility(isCheckBoxVisible.get(position));
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

        public String getNotesTitle(int index)
        {
            return list.get(index);
        }

        class ViewHolder {
            public TextView tv_Name;
            public CheckBox cb;
        }
    }

}
