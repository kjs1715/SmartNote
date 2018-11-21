package com.littleboss.smartnote;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.littleboss.smartnote.Utils.DateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TagSelectResultActivity extends AppCompatActivity {

    LinkedList<ListData> notesList;
    private List<Map<String, String>> listitem;
    private ListView listView;
    Handler handler;
    Runnable listGenerate;
    NoteDatabase noteDatabase;
    TextView textView;

    MyAdapter adapter;
    String tagString;
    boolean notsure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_select_result);
        textView=findViewById(R.id.noresultTextView);

        setTitle("标签检索结果");

        noteDatabase=NoteDatabase.getInstance();

        handler=new Handler();

        Intent intent=getIntent();
        tagString=intent.getStringExtra("tagString");
        notsure=intent.getBooleanExtra("notsure",false);
        notesList=noteDatabase.getNotesTitleListContainTags(Tag.getTagList(tagString),!notsure);

        if(notesList.size()>0)
        {
            textView.setVisibility(View.GONE);
        }
        else
        {
            textView.setVisibility(View.VISIBLE);
        }

        final Runnable runnableUi=new  Runnable(){
            @Override
            public void run() {
                listView = findViewById(R.id.mainlist);
                listView.setClickable(true);
                adapter = new MyAdapter(notesList,TagSelectResultActivity.this);
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

        new Thread(listGenerate).start();

    }

    protected void readListandFlush()
    {
        notesList=noteDatabase.getNotesTitleListContainTags(Tag.getTagList(tagString),!notsure);
        if(notesList.size()>0)
        {
            textView.setVisibility(View.GONE);
        }
        else
        {
            textView.setVisibility(View.VISIBLE);
        }
        new Thread(listGenerate).start();
    }

    @Override
    protected void onResume()
    {
        readListandFlush();
        super.onResume();
    }


}
class MyAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<ListData> list;
    Activity activity;

    public MyAdapter(List<ListData> list,Activity activity) {
        inflater = LayoutInflater.from(activity);
        this.list = list;
        this.activity=activity;
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

    class onMyClick implements View.OnClickListener {

        private int position;
        private List<ListData> list;

        public onMyClick(int position, List<ListData> list) {
            this.position = position;
            this.list = list;
        }

        @Override
        public void onClick(View view) {
            MainActivity.enterNoteDialog(position,list,activity);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new MyAdapter.ViewHolder();
            convertView = inflater.inflate(R.layout.item_main, null);
            viewHolder.tv_Name    = convertView.findViewById(R.id.notetitle);
            viewHolder.cb         = convertView.findViewById(R.id.item_check);
            viewHolder.createDate = convertView.findViewById(R.id.createDate);
            viewHolder.modifyDate = convertView.findViewById(R.id.modifyDate);
            viewHolder.tags       = convertView.findViewById(R.id.tagTextView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (MyAdapter.ViewHolder) convertView.getTag();
        }

        final String str = list.get(position).title;
        viewHolder.tv_Name.setText(str);

        String modified = DateUtils.display(list.get(position).modifyDate);
        String created = DateUtils.display(list.get(position).createDate);
        String tags = list.get(position).tagListString();
        viewHolder.modifyDate.setText("上次修改: " + modified);
        viewHolder.createDate.setText("创建时间: " + created);
        viewHolder.tags.setText(tags);

        convertView.setOnClickListener(new MyAdapter.onMyClick(position,list));

        return convertView;
    }

    class ViewHolder {
        public TextView tv_Name;
        public CheckBox cb;
        public TextView createDate;
        public TextView modifyDate;
        public TextView tags;
    }
}