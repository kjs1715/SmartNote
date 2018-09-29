package com.littleboss.smartnote;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    private Button button;
    private EditText editText;
    private LinkedList<String> notesTitleList;
    private List<Map<String, String>> listitem;
    private ListView listView;
    private Handler handler;
    private Thread listGenerate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        setTitle("笔记搜索");

        button=findViewById(R.id.searchbutton);
        editText=findViewById(R.id.searcheditview);
        listView=findViewById(R.id.searchlistview);

        handler=new Handler();

        final Runnable runnableUi=new  Runnable(){
            @Override
            public void run() {
                final SimpleAdapter myAdapter = new SimpleAdapter(SearchActivity.super.getApplicationContext(), listitem,
                        R.layout.item_main, new String[]{"title"},new int[]{R.id.notetitle}){
                };

                listView.setClickable(true);
                listView.setAdapter(myAdapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent = new Intent(SearchActivity.this, NoteEditActivity.class);
                        HashMap hashMap=(HashMap) listView.getAdapter().getItem(i);
                        intent.putExtra("id", (String)(hashMap.get("title")));
                        startActivity(intent);
                    }
                });
            }
        };

        final Runnable listGenerateRunnable=new Runnable (){
            @Override
            public void run() {
                listitem = new ArrayList<>();
                int len=notesTitleList.size();
                for (int i = 0; i < len; i++) {
                    HashMap<String, String> showitem = new HashMap<>();
                    showitem.put("title", notesTitleList.get(i));
                    listitem.add(showitem);
                }

                handler.post(runnableUi);
            }
        };

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        String[] p=editText.getText().toString().split(" ");
                        notesTitleList=new LinkedList<>();
                        LinkedList<String> allTitles=DataAccess.getNotesTitleList();
                        for(Iterator<String> iterator=allTitles.iterator();iterator.hasNext();)
                        {
                            String title=iterator.next();
                            if(contains(p,title))
                                notesTitleList.add(title);
                        }
                        if (notesTitleList.size() > 0)
                        {
                            new Thread(listGenerateRunnable).start();
                        }
                    }
                }).start();
            }
        });

    }
    boolean contains(String [] keywords, String str)
    {
        for(String s:keywords)
            if(!str.contains(s))
                return false;
        return true;
    }
}
