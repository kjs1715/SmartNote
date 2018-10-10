package com.littleboss.smartnote;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AudioTestActivity extends AppCompatActivity {

    private Button startButton, stopButton;
    private Activity thisActivity;
    private AudioFetcher audioFetcher;
    private NoteDatabase noteDatabase;
    private TextView audioLocationView;
    private TextView permissionInfoView;
    private static final int WRITE_EXTERNAL_STORAGE_ID = 0;

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_ID: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    System.exit(0);
                }
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_test);

        thisActivity = this;
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        audioLocationView = findViewById(R.id.audioLocationView);
        permissionInfoView = findViewById(R.id.permissionInfoView);
        audioFetcher = new AudioFetcher();
        noteDatabase = NoteDatabase.getInstance();

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                audioFetcher.startRecording();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioFetcher.stopRecording();
                try {
                    audioLocationView.setText(noteDatabase.getLatestAudioLocation());
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // 权限在app启动时请求一次，目前如果没有给权限会发生录音崩溃的情况
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_ID
            );
        }
    }
}
