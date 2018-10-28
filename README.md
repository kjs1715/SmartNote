## 添加LBAbstractView的新的子类的步骤

### 1.添加类的声明，模板如下。

```Java
public class 子类名称 extends FrameLayout implements LBAbstractView {
    private Context context;
    private 内容类名 contentView;
    private View blankView;
    private LayoutInflater inflater;
    private LBClickListener clickListener;

    public 子类名称(Context context) {
        this(context, null);
    }
    public 子类名称(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.子类的layout名称,this);
        init();
    }

    public 子类名称(String content,Context context) {
        this(context);
        this.setContent(content);
    }

    public void init() {
        contentView = this.findViewById(R.id.子类的layout名称);
        blankView = findViewById(R.id.blank_view);
        contentView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(clickListener != null)
                    clickListener.onContentClick(view, 子类名称.this);
            }
        });
        contentView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(clickListener != null)
                    clickListener.onContentLongClick(view, 子类名称.this);
                return false;
            }
        });
        blankView.setOnClickListener( new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(clickListener != null)
                    clickListener.onBlankViewClick(view, 子类名称.this);
            }
        });
        blankView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(clickListener != null)
                    clickListener.onContentLongClick(view, 子类名称.this);
                return true;
            }
        });
        this.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(clickListener != null)
                    clickListener.onContentLongClick(view, 子类名称.this);
                return true;
            }
        });
    }

    public String toDataString() {
        return "<子类型名称>"+子类型内容数据字符串+"</子类型名称>";
    }

    @Override
    public void setOnClickViewListener(LBClickListener listener) {
        this.clickListener = listener;
    }

    @Override
    public ViewType getViewType() {
        return ViewType.子类Type名称;
    }

    @Override
    public String getFilePath() {
        return 内容路径名;
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setContent(String cs) {
        //根据获取的内容设置Content的内容（如根据filepath设置音频、视频、图片，如若读文件失败则显示缺省内容或者文本）
    }
}
```

### 2.添加从dataString获取的方法，在DataStringParser内

```Java
static LBAbstractView parseLabel(String label, Context context)
{
    Pattern pattern=Pattern.compile("<([^<>]+)>([\\s\\S]*?)</([^<>]+)>");
    Matcher matcher=pattern.matcher(label);
    String type="",content="";
    if(matcher.find())
    {
        type=matcher.group(1);
        content=matcher.group(2);
    }
    if(type.equals("text"))
    {
        LBTextView lbTextView=new LBTextView(content,context);
        return lbTextView;
    }
    //TODO:在此处添加else if，识别新添加的类并return 类的对象。模板如下
    else if(type.equals("子类型名称，与toDataString中相同"))
    {
        子类名称 view=new 子类名称(content,context);
        return view;
    }
    else
    {
        return new LBTextView(label,context);
    }
}
```

### 3.设置点击事件，在LBAbstractViewGroup内。

```Java
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
            switch (viewType)
            {
                case CONTENT:
                    break;
                case 你的类名，在LBAbstractView里添加:
                    点击你要干什么
                    break;
            }
        }

        @Override
        public void onContentLongClick(View v, View widget) {
            LBAbstractView.ViewType viewType=editView.getViewType();
            switch (viewType)
            {
                case CONTENT:
                    break;
                case 你的类名，在LBAbstractView里添加:
                    长按你要干什么
                    break;
            }
        }
    });
}
```