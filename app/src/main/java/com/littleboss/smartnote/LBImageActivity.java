package com.littleboss.smartnote;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class LBImageActivity extends AppCompatActivity {

    private ImageView imageView;
    private Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lbimage);
        initView();
    }

    private void initView() {
        imageView = findViewById(R.id.bigimage);
        String filepath = getIntent().getStringExtra("filepath");//获取视频路径
        try {
            this.image = BitmapFactory.decodeFile(filepath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(image!=null)
            imageView.setImageBitmap(image);
    }
}
