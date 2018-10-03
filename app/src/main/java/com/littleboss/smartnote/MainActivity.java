package com.littleboss.smartnote;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.LinkedList;


public class MainActivity extends AppCompatActivity {

    private ListView notesList;
    private Button addButton, deleteButton;
    private EditText oldTitleField, contentField, titleField;
    private Activity mainActivity;
    private NoteDatabase noteDatabase;

    private void flushNotesList() {
        LinkedList<String> titleList = noteDatabase.getNotesTitleList();
        String[] titleListString = new String[titleList.size()];
        int i = 0;
        for (String _ : titleList)
            titleListString[i++] = _;
        notesList.setAdapter(
                new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_expandable_list_item_1,
                        titleListString
                )
        );
        oldTitleField.setText("");
        titleField.setText("");
        contentField.setText("");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getDir("databases", MODE_PRIVATE);

        notesList = findViewById(R.id.NotesList);
        addButton = findViewById(R.id.AddButton);
        deleteButton = findViewById(R.id.DeleteButton);
        titleField = findViewById(R.id.TitleField);
        oldTitleField = findViewById(R.id.OldTitleField);
        contentField = findViewById(R.id.ContentField);
        mainActivity = this;

        noteDatabase = NoteDatabase.getInstance();

        flushNotesList();
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    noteDatabase.saveNoteByTitle(
                            oldTitleField.getText().toString(),
                            titleField.getText().toString(),
                            contentField.getText().toString()
                    );

                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                flushNotesList();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    LinkedList<String> _ = new LinkedList<String>();
                    _.add(oldTitleField.getText().toString());
                    noteDatabase.deleteNotesTitleList(_);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                flushNotesList();
            }
        });
    }
}
