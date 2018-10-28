package com.littleboss.smartnote;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * 需要注意的是我实验了.mp4,3gp和avi3种格式的视频，在5.1的真机和模拟器上avi格式都是只有声音没有影像，其他两种格式
 * 播放正常。
 */

public class LBVideoActivity extends AppCompatActivity {

    private VideoView video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lbvideo);
        initView();
    }

    //初始化控件，并且为进度条和图像控件添加监听
    private void initView() {
        video = findViewById(R.id.video);
        String path = getIntent().getStringExtra("filepath");//获取视频路径
        Uri uri = Uri.parse(path);//将路径转换成uri
        video.setVideoURI(uri);//为视频播放器设置视频路径
        video.setMediaController(new MediaController(LBVideoActivity.this));//显示控制栏
        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                video.start();//开始播放视频
            }
        });
    }
}