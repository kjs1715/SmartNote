package com.littleboss.smartnote;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

public class LBTextView extends FrameLayout implements LBAbstractView {
    private EditText editText;
    private LayoutInflater inflater;
    private LBClickListener clickListener;

    public LBTextView(Context context) {
        this(context, null);
    }
    public LBTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
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
                    clickListener.onContentClick(view, LBTextView.this);
            }
        });
        findViewById(R.id.blank_view).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(clickListener != null)
                    clickListener.onBlankViewClick(view, LBTextView.this);
            }
        });
        findViewById(R.id.blank_view).setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(clickListener != null)
                    clickListener.onContentLongClick(view, LBTextView.this);
                return true;
            }
        });
        this.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(clickListener != null)
                    clickListener.onContentLongClick(view, LBTextView.this);
                return true;
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

    public void setFocusSelection(int length) { editText.setSelection(length); }

    public void reqFocus(){
        editText.requestFocus();
    }

    @Override
    public void setOnClickViewListener(LBClickListener listener) {
        this.clickListener = listener;
    }

    @Override
    public ViewType getViewType() {
        return ViewType.CONTENT;
    }

    @Override
    public String getFilePath() {
        return null;
    }

    @Override
    public View getView() {
        return this;
    }

    public String getContent() {
        return editText.getText().toString();
    }

    public String getText() {
        return editText.getText().toString();
    }

    @Override
    public void setContent(String cs) {
        editText.setText(cs);
    }
}
