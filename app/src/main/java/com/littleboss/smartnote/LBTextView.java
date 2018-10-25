package com.littleboss.smartnote;

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
    private LBClickListener clickListener;

    public LBTextView(Context context) {
        this(context, null);
    }
    public LBTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.item_edittext,this);

        init();
    }

    public LBTextView(String content,Context context) {
        this(context, null);
        this.setContent(content);
    }

    public void init() {
        this.editText = this.findViewById(R.id.edittext);
        editText.setSingleLine(false);
        editText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(clickListener != null)
                    clickListener.onBlankViewClick(view, LBTextView.this);
            }
        });
        findViewById(R.id.blank_view).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(clickListener != null)
                    clickListener.onContentClick(view, LBTextView.this);
            }
        });
    }

    public String toDataString() {
        return "<text>"+this.getContent()+"</text>";
    }

    public EditText getEditText() {
        return editText;
    }

    public int getSelectionStart(){
        return editText.getSelectionStart();
    }

    public void setText(String text){
        editText.setText(text);
    }

    public void setSelection(int start,int stop){
        editText.setSelection(start,stop);
    }

    public void reqFocus(){
        editText.requestFocus();
    }

    @Override
    public void setOnClickViewListener(LBClickListener listener) {
        this.clickListener = listener;
    }

    @Override
    public Enum getViewType() {
        return Type.CONTENT;
    }

    @Override
    public String getFilePath() {
        return null;
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public String getContent() {
        String s = editText.getText().toString();
        return s;
    }

    public String getText() {
        String s = editText.getText().toString();
        return s;
    }

    @Override
    public void setContent(String cs) {
        editText.setText(cs);
    }
}
