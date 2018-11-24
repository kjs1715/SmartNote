package com.littleboss.smartnote;


import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.littleboss.smartnote.Utils.DateUtils;

class ListData {
    String title;
    Date createDate, modifyDate;
    List<Tag> tagList;
    ListData(String title, Date createDate, Date modifyDate, String tagListString) {
        this.title = title;
        this.createDate = createDate;
        this.modifyDate = modifyDate;
        this.tagList=Tag.getTagList(tagListString);

    }
    String tagListString()
    {
        StringBuilder stringBuilder=new StringBuilder("");
        for(Iterator<Tag> iterator = tagList.iterator();iterator.hasNext();)
        {
            stringBuilder.append(iterator.next().name);
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }
}

class ImageInfo {
    final String loc;
    final int width;
    final int height;
    private static final String image_regex = "([^\\+]+\\.[a-z0-9]+)\\+([0-9]+)\\+([0-9]+)";
    private static final Pattern p = Pattern.compile(image_regex);

    private ImageInfo(String loc, int width, int height) {
        this.loc = loc;
        this.width = width;
        this.height = height;
    }

    public static ImageInfo parse_image_string(String image_string) {
        Matcher m = p.matcher(image_string);
        if(!m.find()) {
            return null;
        }
        return new ImageInfo(
                m.group(1),
                Integer.parseInt(m.group(2)),
                Integer.parseInt(m.group(3))
        );
    }
}

public class MainActivity extends AppCompatActivity {

    private static final int NOSELECT_STATE = -1;// 表示未选中任何CheckBox
    LinkedList<ListData> notesList;
    private List<Map<String, String>> listitem;
    private ListView listView;
    Handler handler;
    Runnable listGenerate;
    NoteDatabase noteDatabase;

    private Button bt_cancel;
    private TextView tv_sum;
    private LinearLayout linearLayout;

    private Button longclick_menu;
    private LinkedList<ListData> list_selected = new LinkedList();// 需要删除的数据
    private boolean isMultiSelect = false;// 是否处于多选状态
    FloatingActionButton fab;
    MyAdapter adapter;

    private final int WRITE_EXTERNAL_STORAGE_ID = 0;
    private final int RECORD_AUDIO_ID = 1;
    private final int CAMERA_ID = 2;

    /**
     * 删除笔记功能
     * */
    private void deleteNotesSelected() {
        try {
            noteDatabase.deleteNotesTitleList(list_selected);
        } catch (NoteNotExistException e) {
            e.printStackTrace();
        }
        hideLinearLayout();
        readListandFlush();
        isMultiSelect = false;
    }

    Pattern r = Pattern.compile("<([a-zA-Z]+)>([^<>]*)</[a-zA-Z]+>");

    final private String temps_dir = "lb_temps_dir";
    final private String get_temps_dir() {
        File temps_file = new File(this.getExternalFilesDir(null).toString() + File.separator + temps_dir);
        if(!temps_file.exists()) temps_file.mkdir();
        return this.getExternalFilesDir(null).toString() + File.separator + temps_dir;
    }

    /* 递归删除代码 */
    private void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }

    /* 清除用于分享的，临时创建的文件 */
    private void remove_temps() throws IOException {
        File temps_dir = new File(get_temps_dir());
        for(File file: temps_dir.listFiles()) {
            if (!file.isDirectory()) {
                file.delete();
            } else {
                delete(temps_dir);
            }
        }
    }

    /* 用于从<audio>的content中parse出replace */
    final private String replace_regex = "src=.+content=(.+)";
    private String parse_replace(String content) {
        Pattern pattern = Pattern.compile(replace_regex);
        Matcher matcher = pattern.matcher(content);
        try {
            matcher.find();
            String replace = matcher.group(1);
            return replace;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private BaseFont bfChinese = null;
    private Font FontChinese = null;

    private void shareNotesSelected() {
        //share notes
        if(list_selected.size() == 0) {
            return;
        }

        /* 通报用户 */
        Toast.makeText(getApplicationContext(), "converting notes to pdf succeed", Toast.LENGTH_LONG);

        //[1] to pdf files
        ArrayList<File> pdf_files = new ArrayList();

        //[1.1] prepare directory for zipping if necessary:
        // 期望的文件格局：
        // a) temps_dir '/' xxx.pdf
        // b) temps_dir '/' dateTime '/' {xxx.pdf, yyy.pdf, ...}
        String dateTime = "";
        if(list_selected.size() > 1) {
            dateTime = DateUtils.Date2String(new Date());
            new File(get_temps_dir() + File.separator + dateTime).mkdir();
        }

        /*
         * 在current_prepare_dir下，
         * 获得所有pdf文件
         */
        // a)
        String current_prepare_dir = "";
        if(list_selected.size() == 1) {
            current_prepare_dir = get_temps_dir();
        } else {
            current_prepare_dir = get_temps_dir() + File.separator + dateTime;
        }
        // b)
        for (ListData ld : list_selected) {
            try {
                /*
                 * 对笔记标题的特殊处理：
                 * 如果标题含有'/'，即String ld.title中含有'/'，
                 * 则需要将其替换
                 * 暂定为字符'_'
                 * */
                String pdf_title = ld.title;
                pdf_title.replace('/','_');

                /*
                * 创建pdf文件对象
                * */
                File pdf = new File(
                        current_prepare_dir + File.separator
                                + pdf_title + ".pdf"
                );

                /* Document创建 */
                Document document = new Document(PageSize.A4, 500, 150, 50, 50);
                document.setMargins(20, 20, 40, 40);
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdf));
                document.open();

                /*
                * contents为各个控件提供的content_string，
                * 即软件内部通信用的标准化字符串
                * 此处并上标题<title></title>
                * 由于此字符串是标签串，可以通过正则表达式进行解析
                * */
                String contents = "<title>" + ld.title + "</title>" + NoteDatabase.getInstance().getNotesByTitle(ld.title);

                /*
                * 通过正则式进行解析[tag, thing]元组
                * */
                Matcher m = r.matcher(contents);
                document.newPage();
                while(m.find()) {
                    String tag = m.group(1);
                    String thing = m.group(2);
                    switch (tag) {
                        case "text": {
                            if(FontChinese!=null && bfChinese!=null) {
                                document.add(new Paragraph(thing, FontChinese));
                            } else {
                                document.add(new Paragraph(thing));
                            }
                            break;
                        }
                        case "audio": {
                            String replace = parse_replace(thing);
                            if(FontChinese!=null && bfChinese!=null) {
                                document.add(new Paragraph(replace, FontChinese));
                            } else {
                                document.add(new Paragraph(replace));
                            }
                            break;
                        }
                        case "title": {
                            Paragraph title = null;
                            if(FontChinese!=null && bfChinese!=null) {
                                title = new Paragraph(thing, FontChinese);
                            }
                            else {
                                title = new Paragraph(thing);
                            }
                            title.setFont(new Font(Font.FontFamily.COURIER, 24, Font.BOLD));
                            title.setAlignment(Element.ALIGN_CENTER);
                            document.add(title);
                            break;
                        }
                        case "image": {
                            ImageInfo image_info = ImageInfo.parse_image_string(thing);
                            Image image = Image.getInstance(image_info.loc);

                            // resize
                            float scaler_by_w = (
                                    (document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin())
                                            / image.getWidth()
                            ) * 100;

                            float scaler_by_h = (
                                    (document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin())
                                            / image.getHeight()
                            ) * 100;

                            float scaler_if_size_exceeds = Math.min(scaler_by_h, scaler_by_w);
                            if(scaler_if_size_exceeds > 1) {
                                image.scalePercent(scaler_if_size_exceeds);
                            }

                            document.add(image);
                            break;
                        }
                        case "video": {
                            // 从content string获取视频thumbnail，保存为文件thumbnailName
                            ImageInfo image_info = ImageInfo.parse_image_string(thing);
                            Bitmap thumbnail = LBVideoView.getVideoThumbnailNoPlayer(image_info.loc);

                            // 通过thumbnailName文件获取Image对象
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            thumbnail.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            Image image = Image.getInstance(stream.toByteArray());

                            // resize
                            float scaler_by_w = (
                                    (document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin())
                                            / image.getWidth()
                                    ) * 100;

                            float scaler_by_h = (
                                    (document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin())
                                            / image.getHeight()
                                ) * 100;

                            float scaler_if_size_exceeds = Math.min(scaler_by_h, scaler_by_w);
                            if(scaler_if_size_exceeds > 1) {
                                image.scalePercent(scaler_if_size_exceeds);
                            }

                            document.add(image);

                            break;
                        }
                    }
                }

                writer.setPageEmpty(true);
                document.close();

                pdf_files.add(pdf);
            }
            catch (Exception e) {
                /* 跳过创建pdf失败的笔记 */
                e.printStackTrace();
            }
        }

        File temps_dir = new File(get_temps_dir());
        boolean exists = temps_dir.exists();
        String[] files = temps_dir.list();

        if(pdf_files.size() == 0) {
            return;
        }
        else {
            for(File shared:pdf_files)
//            File shared = pdf_files.get(0);
            {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);

                Uri uri = FileProvider.getUriForFile(this, "com.littleboss.smartnote.fileprovider", shared);

                sendIntent.putExtra(Intent.EXTRA_STREAM, uri); //sendIntent.putExtra(Intent.EXTRA_TEXT, "???");
                sendIntent.setType("application/pdf");
                sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(sendIntent, getTitle()));
            }
            return;
        }
    }

    private void hiddenDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("选择操作");
        String[] items = {"删除", "分享"};
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        deleteNotesSelected();
                        break;
                    case 1:
                        shareNotesSelected();
                        break;
                    default:
                        break;
                }
            }
        });

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        try {
            remove_temps();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NoteEditActivity.class);
                intent.putExtra("id", "");
                intent.putExtra("newCreatedNote", true);
                startActivity(intent);
            }
        });

        isMultiSelect=false;

        setTitle("会议速记助手");

        noteDatabase=NoteDatabase.getInstance();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_ID
            );
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_ID
            );
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.CAMERA},
                    CAMERA_ID
            );
        }


        handler=new Handler();

        final Runnable runnableUi=new  Runnable(){
            @Override
            public void run() {

                listView = findViewById(R.id.mainlist);
                listView.setClickable(true);
                adapter = new MyAdapter(MainActivity.this, notesList, NOSELECT_STATE);
                listView.setAdapter(adapter);
            }
        };

        listGenerate=new Runnable(){
            @Override
            public void run () {
                listitem = new ArrayList<>();
                int len = notesList.size();
                for (int i = 0; i < len; i++) {
                    HashMap<String, String> showitem = new HashMap<>();
                    showitem.put("title", notesList.get(i).title);
                    listitem.add(showitem);
                }
                handler.post(runnableUi);
            }
        };

        new Thread(new Runnable(){
            @Override
            public void run()
            {
                notesList=noteDatabase.getNotesTitleList();
                if(notesList!=null)
                    new Thread(listGenerate).start();
            }
        }).start();

        bt_cancel = (Button) findViewById(R.id.bt_cancel);
//        bt_delete = (Button) findViewById(R.id.bt_delete);
        longclick_menu = findViewById(R.id.bt_delete);
        tv_sum = (TextView) findViewById(R.id.tv_sum);
        linearLayout = (LinearLayout) findViewById(R.id.hidden_layout);

        bt_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLinearLayout();
                isMultiSelect = false;
            }
        });

        longclick_menu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hiddenDialog();
            }
        });

        try {
            bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            FontChinese = new Font(bfChinese, 12, Font.NORMAL);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sortNotesList(int type) {
        switch(type) {
            case 0:                         // by time
                Collections.sort(notesList, new Comparator<ListData>() {
                    @Override
                    public int compare(ListData o1, ListData o2) {
                        return Compare(0, o1, o2);
                    }
                });
                break ;
            case 1:
                Collections.sort(notesList, new Comparator<ListData>() {
                    @Override
                    public int compare(ListData o1, ListData o2) {
                        return Compare(1, o1, o2);
                    }
                });
                break ;
            case 2:
                Collections.sort(notesList, new Comparator<ListData>() {
                    @Override
                    public int compare(ListData o1, ListData o2) {
                        return Compare(2, o1, o2);
                    }
                });
                break;
            default :
                break;
        }
        flushList();
    }

    public AlertDialog sortDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] dialogItems = { "按创建时间排序","按修改时间排序", "按名称排序" };
        builder.setItems(dialogItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sortNotesList(which);
            }
        });
        return builder.show();
    }

    public static AlertDialog enterNoteDialog(int position, List<ListData> notesList, Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final String[] dialogItems = { "编辑","预览","修改标签"};
        builder.setItems(dialogItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0 || which==1)
                {
                    Intent intent = new Intent(activity, NoteEditActivity.class);
                    intent.putExtra("id", (notesList.get(position)).title);
                    intent.putExtra("newCreatedNote", false);
                    intent.putExtra("justsee", which);
                    activity.startActivity(intent);
                }
                else
                {
                    Intent intent = new Intent(activity, TagEditActivity.class);
                    intent.putExtra("id", (notesList.get(position)).title);
                    activity.startActivity(intent);
                }
            }
        });
        return builder.show();
    }

    protected void showLinearLayout()
    {
        linearLayout.setVisibility(View.VISIBLE);
        tv_sum.setText("共选择了" + list_selected.size() + "项");
        adapter.showAllItems();
        adapter.notifyDataSetChanged();
        fab.setVisibility(View.GONE);
    }

    protected void hideLinearLayout()
    {
        adapter.hideAllItems();
        adapter.notifyDataSetChanged();
        linearLayout.setVisibility(View.GONE);
        fab.setVisibility(View.VISIBLE);

    }

    protected void readListandFlush()
    {
        new Thread(new Runnable(){
            @Override
            public void run()
            {
                notesList=noteDatabase.getNotesTitleList();
                if(notesList!=null)
                    new Thread(listGenerate).start();
            }
        }).start();
    }

    protected void flushList()
    {
        new Thread(new Runnable(){
            @Override
            public void run()
            {
                if(notesList!=null)
                    new Thread(listGenerate).start();
            }
        }).start();
    }

    @Override
    protected void onResume()
    {
        readListandFlush();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mainactivity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
            if(isMultiSelect)
            {
                Toast.makeText(MainActivity.this.getApplicationContext(),"请先退出多选模式",Toast.LENGTH_SHORT).show();
            }
            else
            {
                if (id == R.id.searchitem) {
                    Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                    startActivity(intent);
                }
                else if(id == R.id.sortitem) {
                    sortDialog();
                }
                else if(id == R.id.tagselect) {
                    Intent intent = new Intent(MainActivity.this, TagSelectActivity.class);
                    startActivity(intent);
                }
            }
        return super.onOptionsItemSelected(item);
    }

    class MyAdapter extends BaseAdapter{
        public HashMap<Integer, Integer> isCheckBoxVisible;// 用来记录是否显示checkBox
        public HashMap<Integer, Boolean> isChecked;// 用来记录是否被选中
        private LayoutInflater inflater;
        private List<ListData> list;

        public MyAdapter(Context context, List<ListData> list, int position) {
            inflater = LayoutInflater.from(context);
            this.list = notesList;
            isCheckBoxVisible = new HashMap<Integer, Integer>();
            isChecked = new HashMap<Integer, Boolean>();
            // 如果处于多选状态，则显示CheckBox，否则不显示
            if (isMultiSelect) {
                for (int i = 0; i < list.size(); i++) {
                    isCheckBoxVisible.put(i, CheckBox.VISIBLE);
                    isChecked.put(i, false);
                }
            } else {
                for (int i = 0; i < list.size(); i++) {
                    isCheckBoxVisible.put(i, CheckBox.GONE);
                    isChecked.put(i, false);
                }
            }

            // 如果长按Item，则设置长按的Item中的CheckBox为选中状态
            if (isMultiSelect && position >= 0) {
                isChecked.put(position, true);
            }
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class onMyLongClick implements OnLongClickListener {

            private int position;
            private List<ListData> list;

            // 获取数据，与长按Item的position
            public onMyLongClick(int position, List<ListData> list) {
                this.position = position;
                this.list = list;
            }

            // 在长按监听时候，切记将监听事件返回ture
            @Override
            public boolean onLongClick(View v) {
                if(isMultiSelect)
                {
                    return false;
                }
                else {
                    isMultiSelect = true;
                    list_selected.clear();
                    list_selected.add(notesList.get(position));
                    adapter.setOneHot(position);
                    adapter.showAllItems();
                    adapter.notifyDataSetChanged();
                    showLinearLayout();
                    return true;
                }
            }
        }

        class onMyClick implements OnClickListener {

            private int position;
            private List<ListData> list;

            // 获取数据，与长按Item的position
            public onMyClick(int position, List<ListData> list) {
                this.position = position;
                this.list = list;
            }

            @Override
            public void onClick(View view) {
                if(isMultiSelect)
                {
                    if(adapter.isChecked(position))
                    {
                        list_selected.remove(adapter.getNotesTitle(position));
                    }
                    else
                    {
                        list_selected.add(adapter.getNotesTitle(position));
                    }
                    adapter.switchIsChecked(position);
                    adapter.notifyDataSetChanged();
                    tv_sum.setText("共选择了" + list_selected.size() + "项");
                }
                else
                {
                    enterNoteDialog(position,notesList,MainActivity.this);
                }
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;

            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_main, null);
                viewHolder.tv_Name    = convertView.findViewById(R.id.notetitle);
                viewHolder.cb         = convertView.findViewById(R.id.item_check);
                viewHolder.createDate = convertView.findViewById(R.id.createDate);
                viewHolder.modifyDate = convertView.findViewById(R.id.modifyDate);
                viewHolder.tags       = convertView.findViewById(R.id.tagTextView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final String str = list.get(position).title;
            viewHolder.tv_Name.setText(str);

            // 根据position设置CheckBox是否可见，是否选中
            viewHolder.cb.setChecked(isChecked.get(position));
            viewHolder.cb.setVisibility(isCheckBoxVisible.get(position));

            String modified = DateUtils.display(list.get(position).modifyDate);
            String created = DateUtils.display(list.get(position).createDate);
            String tags = list.get(position).tagListString();
            viewHolder.modifyDate.setText("上次修改: " + modified);
            viewHolder.createDate.setText("创建时间: " + created);
            viewHolder.tags.setText(tags);

            convertView.setOnLongClickListener(new onMyLongClick(position,list));
            convertView.setOnClickListener(new onMyClick(position,list));

            return convertView;
        }

        public void hideAllItems()
        {
            int len=list.size();
            for(int i=0;i<len;i++)
            {
                isCheckBoxVisible.put(i,View.GONE);
            }
        }

        public void showAllItems()
        {
            int len=list.size();
            for(int i=0;i<len;i++)
            {
                isCheckBoxVisible.put(i,View.VISIBLE);
            }
        }

        public void setOneHot(int position)
        {
            int len=list.size();
            for(int i=0;i<len;i++)
            {
                if(i==position)
                    isChecked.put(i,true);
                else
                    isChecked.put(i,false);
            }
        }

        public void switchIsChecked(int position)
        {
            isChecked.put(position,!isChecked.get(position));
        }

        public boolean isChecked(int position)
        {
            return isChecked.get(position);
        }

        public ListData getNotesTitle(int index)
        {
            return list.get(index);
        }

        class ViewHolder {
            public TextView tv_Name;
            public CheckBox cb;
            public TextView createDate;
            public TextView modifyDate;
            public TextView tags;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            System.exit(0);

        }
    }

    public int Compare(int type, ListData o1, ListData o2) {
        if(type == 0) {
            if(o1.createDate.before(o2.createDate)) {
                return 1;
            } else if (o2.createDate.before(o1.createDate)) {
                return -1;
            } else {
                return 0;
            }
        } else if(type == 1) {
            if(o1.modifyDate.before(o2.modifyDate)) {
                return 1;
            } else if (o2.modifyDate.before(o1.modifyDate)) {
                return -1;
            } else {
                return 0;
            }
        } else {
            return o1.title.compareTo(o2.title);
        }
    }

    public void isMultiselected() {
        this.isMultiSelect = true;
    }
}
