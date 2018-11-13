package com.littleboss.smartnote;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.littleboss.smartnote.Utils.DateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    private Button button;
    private EditText editText;
    LinkedList<ListData> notesList;
    private List<Map<String, String>> listitem;
    private ListView listView;
    Handler handler;
    Runnable listGenerate;
    NoteDatabase noteDatabase;
    TextView textView;

    MyAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        setTitle("笔记搜索");

        button=findViewById(R.id.searchbutton);
        editText=findViewById(R.id.searcheditview);
        listView=findViewById(R.id.searchlistview);
        textView=findViewById(R.id.noresultTextView);

        handler=new Handler();

        noteDatabase=NoteDatabase.getInstance();

        final Runnable runnableUi=new  Runnable(){
            @Override
            public void run() {
                listView.setClickable(true);
                adapter = new MyAdapter(notesList,SearchActivity.this);
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

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notesList=NoteDatabase.getInstance().getNotesTitleListContainKeywords(editText.getText().toString());
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
        });

    }

    protected void readListandFlush()
    {
        notesList=noteDatabase.getNotesTitleListContainKeywords(editText.getText().toString());
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