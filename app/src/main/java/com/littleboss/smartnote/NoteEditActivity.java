package com.littleboss.smartnote;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

public class NoteEditActivity extends AppCompatActivity {
    private Notes content;
    private Intent intent;
    private TextView textView;
    private String title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);
        intent = getIntent();
        title=intent.getStringExtra("id");

        textView=findViewById(R.id.textView);

        textView.setText(String.format("this is the edit notes page for notes:%s",title));
    }
}
