package com.littleboss.smartnote;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class LBImageActivity extends AppCompatActivity {

    private Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lbimage);
        initView();
    }

    private void initView() {
        String filepath = getIntent().getStringExtra("filepath");
        try {
            this.image = BitmapFactory.decodeFile(filepath);
        } catch (Exception e) {
            Log.i("error initView : ", e.toString());
        }
        if(image!=null)
            ((ImageView)findViewById(R.id.bigimage)).setImageBitmap(image);
    }
}
