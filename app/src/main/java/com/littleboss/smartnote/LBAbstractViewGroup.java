package com.littleboss.smartnote;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;

public class LBAbstractViewGroup extends ScrollView {
    public static final String KEY_TITLE = "title";
    public static final String KEY_CONTENT = "content";

    private LinearLayout allLayout; // 这个是所有子view的容器，scrollView内部的唯一一个ViewGroup
    private OnKeyListener keyListener; // 所有EditText的软键盘监听器
    private OnFocusChangeListener focusListener; // 所有EditText的焦点监听listener
    public LBTextView lastFocusView; // 最近被聚焦的view
    private LayoutTransition mTransitioner; // 只在图片View添加或remove时，触发transition动画
    private Context mContext;

    public LBAbstractViewGroup(Context context) {
        this(context, null);
    }

    public LBAbstractViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LBAbstractViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;

        // 初始化allLayout，用来存放所有富文本组件
        allLayout = new LinearLayout(context);
        allLayout.setOrientation(LinearLayout.VERTICAL);
        setupLayoutTransitions();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        addView(allLayout, layoutParams);

        // 键盘退格监听
        // 主要用来处理点击回删按钮时，view的一些列合并操作
        keyListener = new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                    LBTextView LBTextView = (LBTextView) v.getParent().getParent();
                    onBackspacePress(LBTextView);
                }
                return false;
            }
        };

        //定一个焦点改变监听器，用来知道最后的焦点在哪个组件，这样插入新组件的话就会插入到那个组件的后面
        focusListener = new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    lastFocusView = (LBTextView) v.getParent().getParent();
                }
            }
        };
    }

    public void removeAll() {
        if (allLayout != null) {
            allLayout.removeAllViews();
        }
    }

    public void setContent(String dataString)
    {
        List<String> labels=DataStringParser.parse(dataString);
        this.removeAll();
        for(String label:labels)
        {
            View view=(View)DataStringParser.parseLabel(label,this.mContext);
            if(view instanceof LBTextView)
            {
                ((LBTextView) view).getEditText().setOnKeyListener(keyListener);
                ((LBTextView) view).getEditText().setOnFocusChangeListener(focusListener);
            }
            setEditViewListener((LBAbstractView)view);
            this.addViewToLinear(view);
        }
        if(this.allLayout.getChildCount()==0)
        {
            LBTextView lbTextView=createEditText();
            this.addViewtoCursor(lbTextView);
        }
    }

    /**
     * 处理软键盘backSpace回退事件
     * 回退时是否在文本上回退，在文本上时是否还有数据，有就删除数据，没有就上次上一个组件，当前焦点还是在这个文本框，这样才有一种富文本编辑器的感觉
     *
     * @param
     */
    private void onBackspacePress(LBTextView curView) {
        int startSelection = curView.getEditText().getSelectionStart();
        // 只有在光标已经顶到文本输入框的最前方，在判定是否删除之前的组件，或两个View合并
        if (startSelection == 0) {
            //表示一个文本框，这种情况回退不能删除组件
            if (allLayout.getChildCount() <= 1) {
                return;
            }
            int editIndex = allLayout.indexOfChild(curView);
            View preView = allLayout.getChildAt(editIndex - 1);
            // 则返回的是null
            if (null != preView) {
                if (preView instanceof LBTextView) {
                    // 光标EditText的上一个view对应的还是文本框EditText
                    String str1 = curView.getEditText().getText().toString();
                    EditText preEdit = ((LBTextView) preView).getEditText();
                    String str2 = preEdit.getText().toString();

                    // 合并文本view时，不需要transition动画
                    allLayout.setLayoutTransition(null);
                    allLayout.removeView(curView);
                    allLayout.setLayoutTransition(mTransitioner); // 恢复transition动画

                    // 文本合并
                    preEdit.setText(str2 + str1);
                    preEdit.requestFocus();
                    preEdit.setSelection(str2.length(), str2.length());
                    lastFocusView = (LBTextView) preView;
                } else if (preView instanceof LBAbstractView) {
                    // 光标EditText的上一个view对应的是组件
                    onEditViewCloseClick(preView);
                }

            }
        }
    }

    /**
     * 处理组件关闭图标的点击事件
     *
     * @param view 整个image对应的relativeLayout view
     */
    private void onEditViewCloseClick(View view) {
        if (!mTransitioner.isRunning()) {
            allLayout.removeView(view);
        }
    }

    /**
     * 生成文本输入框
     */
    private LBTextView createEditText() {
        LBTextView LBTextView = new LBTextView(mContext);
        LBTextView.getEditText().setOnKeyListener(keyListener);
        LBTextView.getEditText().setOnFocusChangeListener(focusListener);
        setEditViewListener(LBTextView);
        return LBTextView;
    }

    private void setEditViewListener(final LBAbstractView editView) {
        //删除按钮设置监听器
        editView.setOnClickViewListener(new LBClickListener() {
            @Override
            public void onBlankViewClick(View v, View widget) {
                //点击组件下面的空白，如果当前组件和上下组件都不是文本框，则创建一个文本框
                int i=allLayout.indexOfChild(widget);
                if(i<0)
                    return;
                View curView = allLayout.getChildAt(i);
                View nextView = allLayout.getChildAt(i + 1);
                if (!(curView instanceof LBTextView) && (nextView == null || !(nextView instanceof LBTextView))) {
                    addEditTextAtIndex(i + 1, "");
                }
            }

            @Override
            public void onContentClick(View v, View widget) {
                LBAbstractView.ViewType viewType=editView.getViewType();
                Intent intent;
                switch (viewType)
                {
                    case IMAGE:
                        intent = new Intent(getContext(), LBImageActivity.class);
                        intent.putExtra("filepath",editView.getFilePath());
                        getContext().startActivity(intent);
                        break;
                    case VIDEO:
                        intent = new Intent(getContext(), LBVideoActivity.class);
                        intent.putExtra("filepath",editView.getFilePath());
                        getContext().startActivity(intent);
                        break;
                }
            }

            @Override
            public void onContentLongClick(View v, View widget) {
                LBAbstractView.ViewType viewType=editView.getViewType();
                switch (viewType)
                {
                    case IMAGE:
                        ((LBImageView)editView).imageDialog();
                        break;
                    case VIDEO:
                        ((LBVideoView)editView).imageDialog();
                        break;
                    case AUDIO:
                        ((LBAudioView)editView).audioDialog();
                        break;
                }
            }

            @Override
            public void moveUp(View widget) {
                int i=allLayout.indexOfChild(widget);
                moveViewUp(i);
            }

            @Override
            public void moveDown(View widget) {
                int i=allLayout.indexOfChild(widget);
                moveViewDown(i);
            }
        });
    }

    /**
     * 在特定位置插入EditText
     *
     * @param index   位置
     * @param editStr EditText显示的文字
     */
    private void addEditTextAtIndex(final int index, String editStr) {
        LBTextView view = createEditText();
        EditText editText2 = (EditText) view.findViewById(R.id.edittext);
        editText2.setText(editStr);
        lastFocusView = view;
        view.reqFocus();
        // 请注意此处，EditText添加、或删除不触动Transition动画
        allLayout.setLayoutTransition(null);
        allLayout.addView(view, index);
        allLayout.setLayoutTransition(mTransitioner); // remove之后恢复transition动画
    }

    /**
     * 在特定位置添加一个编辑组件
     */
    private void addEditViewAtIndexAnimation(final int index, final LBAbstractView editView) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                allLayout.addView(editView.getView(), index);
            }
        }, 200);


    }

    private void srollToBottom() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (lastFocusView != null)
                    lastFocusView.reqFocus();
                fullScroll(ScrollView.FOCUS_DOWN);
            }
        }, 1000);
    }

    /**
     * 立即插入一个编辑组件，适用于编辑话题，有延时会导致顺序错乱
     * 代价是没有动画
     *
     * @param index    显示位置
     * @param editView 组件
     */
    private void addEditViewAtIndexImmediate(final int index, final LBAbstractView editView) {

        allLayout.addView(editView.getView(), index);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (lastFocusView != null)
                    lastFocusView.reqFocus();
                fullScroll(ScrollView.FOCUS_DOWN);
            }
        }, 1000);

    }

    /**
     * 初始化transition动画
     */
    private void setupLayoutTransitions() {
        mTransitioner = new LayoutTransition();
        allLayout.setLayoutTransition(mTransitioner);
        mTransitioner.setDuration(300);
    }

    /**
     * 获取当前焦点的Edittext
     *
     * @return
     */
    public EditText getCurFousEditText() {
        if (lastFocusView != null)
            return lastFocusView.getEditText();
        return null;
    }

    public void setLastEditTextFocus() {
        int childCount = allLayout.getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            View childAt = allLayout.getChildAt(i);
            if (childAt instanceof LBTextView) {
//                String content = ((LBTextView) childAt).getContent();
//                ((LBTextView) childAt).setFocusSelection(content.length());
                ((LBTextView) childAt).reqFocus();
                showKeyBoard(((LBTextView) childAt).getEditText());
                return;
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (ev.getY() > allLayout.getBottom()) {
            setLastEditTextFocus();
            return true;
        }


        return super.dispatchTouchEvent(ev);
    }

    /**
     * 隐藏小键盘
     */
    public void hideKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(lastFocusView.getWindowToken(), 0);
    }

    public void showKeyBoard(EditText view) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        int clength = view.getText().toString().length();
        view.setSelection(clength);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
//        view.requestFocus();
        imm.showSoftInput(view, 0);
    }



    /**
     * 获取全部数据集合
     */
    public List<LBAbstractView> buildData() {
        List<LBAbstractView> dataList = new ArrayList<LBAbstractView>();
        int num = allLayout.getChildCount();
        for (int index = 0; index < num; index++) {
            LBAbstractView itemView = (LBAbstractView) allLayout.getChildAt(index);
            dataList.add(itemView);
        }
        return dataList;
    }

    private void addViewToLinear(View view)
    {
        this.allLayout.addView(view);
    }

    public String toDataString()
    {
        StringBuffer stringBuffer=new StringBuffer("");
        for(int i=0;i<this.allLayout.getChildCount();i++)
        {
            stringBuffer.append(((LBAbstractView)allLayout.getChildAt(i)).toDataString());
        }
        return stringBuffer.toString();
    }

    int getSize()
    {
        return this.allLayout.getChildCount();
    }

    /**
     * 插入一个编辑组件,根据焦点的不同而位置不同
     */
    public void addViewtoCursor(LBAbstractView editView) {
        setEditViewListener(editView);

        if (lastFocusView != null)
        {
            String lastEditStr = lastFocusView.getContent();
            lastFocusView.reqFocus();
            int cursorIndex = lastFocusView.getSelectionStart();
            int lastEditIndex = allLayout.indexOfChild(lastFocusView);
            if (cursorIndex >= 0) {
                String editStr1 = lastEditStr.substring(0, cursorIndex).trim();

                if (lastEditStr.length() == 0 || editStr1.length() == 0) {
                    // 如果EditText为空，或者光标已经顶在了editText的最前面，则直接插入组件，并且EditText下移即可
                    addEditViewAtIndexAnimation(lastEditIndex, editView);
                } else {
                    // 如果EditText非空且光标不在最顶端，则需要添加新的imageView和EditText
                    lastFocusView.setText(editStr1);
                    String editStr2 = lastEditStr.substring(cursorIndex).trim();
                    if (allLayout.getChildCount() - 1 == lastEditIndex
                            || editStr2.length() > 0) {
                        addEditTextAtIndex(lastEditIndex + 1, editStr2);
                    }

                    addEditViewAtIndexAnimation(lastEditIndex + 1, editView);
                    lastFocusView.reqFocus();
                    lastFocusView.setSelection(lastFocusView.getContent().length(), lastFocusView.getContent().length());
                }
                if (allLayout.indexOfChild(lastFocusView) >= allLayout.getChildCount() - 1) {
                    srollToBottom();
                }
            } else {
                //出现失去焦点的情况，默认添加到最后面
                addEditViewAtIndexAnimation(allLayout.getChildCount() - 1, editView);
                srollToBottom();
            }
            hideKeyBoard();
        }
        else
        {
            addEditViewAtIndexAnimation(allLayout.getChildCount() - 1, editView);
            srollToBottom();
        }
    }

    void deleteView(int position)
    {
        allLayout.removeViewAt(position);
    }

    void moveViewUp(int position)
    {
        swapViewPosition(position,position-1);
    }
    void moveViewDown(int position)
    {
        swapViewPosition(position,position+1);
    }
    void swapViewPosition(int position1,int position2)
    {
        if(position1==position2)
            return;
        if(position1>=allLayout.getChildCount()||position2>=allLayout.getChildCount())
            return;
        if(position1>position2) {
            int mid;
            mid = position1;
            position1=position2;
            position2 = mid;
        }
        View midview1=allLayout.getChildAt(position1);
        View midview2=allLayout.getChildAt(position2);
        allLayout.removeViewAt(position2);
        allLayout.removeViewAt(position1);
        allLayout.addView(midview2,position1);
        allLayout.addView(midview1,position2);
    }

    public void onNewTextEvent() {
        int lastEditIndex = allLayout.indexOfChild(lastFocusView);
        View curView = allLayout.getChildAt(lastEditIndex);
        View nextView = allLayout.getChildAt(lastEditIndex + 1);
        if (!(curView instanceof LBTextView) && (nextView == null || !(nextView instanceof LBTextView))) {
            addEditTextAtIndex(lastEditIndex + 1, "");
        }
    }
}
