package com.littleboss.smartnote;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import java.util.LinkedList;
import java.util.List;

public class TagSelectActivity extends AppCompatActivity {

    String title;
    NoteDatabase noteDatabase;
    List<Tag> allTagList;
    FlowLayout upperFlowLayout;
    FlowLayout lowerFlowLayout;
    CheckBox notsure;
    private Button button;

    View testView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_select);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("标签检索");

        upperFlowLayout=findViewById(R.id.upperGrid);
        lowerFlowLayout=findViewById(R.id.lowerGrid);
        button=findViewById(R.id.sbutton);
        notsure=findViewById(R.id.notsure);
        noteDatabase = NoteDatabase.getInstance();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinkedList<Tag> linkedList=new LinkedList<>();
                int count=upperFlowLayout.getChildCount();
                for(int i=0;i<count;i++)
                {
                    linkedList.add(new Tag(((TextView)((ConstraintLayout)((ConstraintLayout)upperFlowLayout.getChildAt(i)).getChildAt(0)).getChildAt(0)).getText().toString()));
                }
                String tagString=Tag.getTagListString(linkedList);
                Intent intent = new Intent(TagSelectActivity.this, TagSelectResultActivity.class);
                intent.putExtra("tagString", tagString);
                intent.putExtra("notsure", notsure.isChecked());
                startActivity(intent);
            }
        });

        allTagList=noteDatabase.getAllTagsList();
        for(Tag tag:allTagList)
        {
            View view=Tag.getTextView(TagSelectActivity.this,tag);
            addViewToLower(upperFlowLayout,lowerFlowLayout,view);
            testView = view;
        }
    }

    public void addViewToUpper(ViewGroup upper, ViewGroup lower , View view)
    {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                upper.removeView(view);
                addViewToLower(upper,lower,view);
            }
        });
        upper.addView(view);
    }
    public void addViewToLower(ViewGroup upper, ViewGroup lower , View view)
    {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lower.removeView(view);
                addViewToUpper(upper,lower,view);
            }
        });
        lower.addView(view);
    }

    public void setTitlE(String title) {
        this.title = title;
    }

    public View getTestView() {
        return this.testView;
    }
}
