package com.littleboss.smartnote;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.Iterator;
import java.util.List;

public class TagEditActivity extends AppCompatActivity {

    String title;
    String tagsListString;
    NoteDatabase noteDatabase;
    List<Tag> thisTagList;
    List<Tag> allTagList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        title = getIntent().getStringExtra("id");
        noteDatabase = NoteDatabase.getInstance();
        List<ListData> listData = noteDatabase.getNotesTitleList();
        for(Iterator<ListData> iterator=listData.iterator();iterator.hasNext();)
        {
            ListData listData1=iterator.next();
            if(listData1.title.equals(title))
            {
                tagsListString=listData1.tagListString();
                return;
            }
        }
        thisTagList=Tag.getTagList(tagsListString);
        allTagList=noteDatabase.
    }

}
