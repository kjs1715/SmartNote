package com.littleboss.smartnote.Utils;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.littleboss.smartnote.R;

import java.util.zip.Inflater;

public class LBTextView extends FrameLayout implements LBAbstractView {
    private Context context;
    private EditText editText;
    private LayoutInflater inflater;

    public LBTextView(Context context) {
        this(context, null);
    }
    public LBTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        init();
    }

    public void init() {
        this.editText = (EditText) findViewById(R.id.edittext);
        editText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        findViewById(R.id.blank_view).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public void setContent(String content) {
        this.editText.setText(content);
    }

    public String getContent() {
        String content = this.editText.getText().toString();
        return content;
    }

}
