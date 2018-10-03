package com.littleboss.smartnote;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NoteEditActivity.class);
                intent.putExtra("id", "");
                intent.putExtra("newCreatedNote", true);
                startActivity(intent);
            }
        });


        setTitle("会议速记助手");

        handler=new Handler();

        final Runnable runnableUi=new  Runnable(){
            @Override
            public void run() {
                final SimpleAdapter myAdapter = new SimpleAdapter(MainActivity.super.getApplicationContext(), listitem,
                        R.layout.item_main, new String[]{"title"},new int[]{R.id.notetitle}){
                };

                listView = findViewById(R.id.mainlist);
                listView.setClickable(true);
                listView.setAdapter(myAdapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent = new Intent(MainActivity.this, NoteEditActivity.class);
                        HashMap hashMap=(HashMap) listView.getAdapter().getItem(i);
                        intent.putExtra("id", (String)(hashMap.get("title")));
                        intent.putExtra("newCreatedNote", false);
                        startActivity(intent);
                    }
                });

                listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                        return false;
                    }
                });
            }
        };

        listGenerate=new Thread(new Runnable() {
            @Override
            public void run() {
                listitem = new ArrayList<>();
                int len=notesList.size();
                for (int i = 0; i < len; i++) {
                    HashMap<String, String> showitem = new HashMap<>();
                    showitem.put("title", notesList.get(i));
                    listitem.add(showitem);
                }

                handler.post(runnableUi);
            }
        });

        new Thread(new Runnable(){
            @Override
            public void run()
            {
                notesList=DataAccess.getNotesTitleList();
                if(notesList!=null&&notesList.size()>0)
                    listGenerate.start();
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.searchitem) {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
