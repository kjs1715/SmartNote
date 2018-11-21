package com.littleboss.smartnote;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

public class TagEditActivity extends AppCompatActivity {

    String title;
    String tagsListString;
    NoteDatabase noteDatabase;
    List<Tag> thisTagList;
    List<Tag> allTagList;
    FlowLayout upperFlowLayout;
    FlowLayout lowerFlowLayout;
    private Button button;

    View testView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("标签编辑");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addTagFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTagDialog().show();
            }
        });

        upperFlowLayout=findViewById(R.id.upperGrid);
        lowerFlowLayout=findViewById(R.id.lowerGrid);
        button=findViewById(R.id.sbutton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinkedList<Tag> linkedList=new LinkedList<>();
                int count=upperFlowLayout.getChildCount();
                for(int i=0;i<count;i++)
                {
                    linkedList.add(new Tag(((TextView)((ConstraintLayout)((ConstraintLayout)upperFlowLayout.getChildAt(i)).getChildAt(0)).getChildAt(0)).getText().toString()));
                }
                try {
                    noteDatabase.saveNoteByTitle(title,null,null,Tag.getTagListString(linkedList));
                } catch (Exception e) {
                    //e.printStackTrace();
                    Log.i("err button.onClick() : ", e.toString());
                }
                Toast.makeText(TagEditActivity.this,"标签保存成功",Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        title = getIntent().getStringExtra("id");
        noteDatabase = NoteDatabase.getInstance();
        List<ListData> listData = noteDatabase.getNotesTitleList();
        for(ListData listData1:listData)
        {
            if(listData1.title.equals(title))
            {
                tagsListString=listData1.tagListString();
                break;
            }
        }
        thisTagList=Tag.getTagList(tagsListString);
        thisTagList=Tag.removeDuplicate(thisTagList);
        for(Tag tag:thisTagList)
        {
            View view=Tag.getTextView(TagEditActivity.this,tag);
            addViewToUpper(upperFlowLayout,lowerFlowLayout,view);
        }
        allTagList=noteDatabase.getAllTagsList();
        for(Tag tag:allTagList)
        {
            if(thisTagList.contains(tag))
            {
                continue;
            }
            View view=Tag.getTextView(TagEditActivity.this,tag);
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

    public AlertDialog addTagDialog() {
        AddTagDialog addtag_dialog = new AddTagDialog(TagEditActivity.this);
        AlertDialog dialog = new AlertDialog.Builder(TagEditActivity.this)
                .setTitle("添加新标签")
                .setView(addtag_dialog)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                String string=addtag_dialog.getTagname().getText().toString().trim().replace(" ","_");
                if(noteDatabase.getTestMod() == 1) {
                    string = "test1"; // Don`t mind, just for testing ^^7
                }
                if(string.length()==0) {
                    return;
                }
                if(thisTagList.contains(new Tag(string)))
                {
                    Toast.makeText(TagEditActivity.this,"该笔记已有同名标签",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(allTagList.contains(new Tag(string)))
                {
                    Toast.makeText(TagEditActivity.this,"所有标签中已有同名标签",Toast.LENGTH_SHORT).show();
                    addViewToUpper(upperFlowLayout,lowerFlowLayout,Tag.getTextView(TagEditActivity.this, new Tag(string)));
                    return;
                }
                Tag tag=new Tag(string);
                addViewToUpper(upperFlowLayout,lowerFlowLayout,Tag.getTextView(TagEditActivity.this, tag));
            }
        }).create();
        return dialog;
    }

    public void setThisTagList(Tag str) {
        this.thisTagList.add(str);
    }

    public void setAllTagList(Tag str) {
        this.allTagList.add(str);
    }

    public void deleteThisTagList() {
        this.thisTagList.clear();
    }

    public void deleteAllTagList() {
        this.allTagList.clear();
    }
}


class AddTagDialog extends FrameLayout {

    private EditText tagname;
    public AddTagDialog(@NonNull Context context) {
        super(context);

        // 渲染xml
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.addtag_dialog, this);

        // 绑定View对象
        tagname = findViewById(R.id.tagname);
    }

    public EditText getTagname() {
        return this.tagname;
    }
}