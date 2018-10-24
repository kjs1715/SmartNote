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
import android.widget.LinearLayout;
import android.widget.TextView;

public class AudioTestActivity extends AppCompatActivity {

    private Button startButton, stopButton;
    private LinearLayout audioLayout;
    private Activity thisContext;
    private TextView audioCount;
    public static final int WRITE_EXTERNAL_STORAGE_ID = 0;
    public static final int RECORD_AUDIO_ID = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_test);

        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        audioLayout = findViewById(R.id.audioLayout);
        audioCount = findViewById(R.id.audioCount);
        thisContext = this;
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //audioLayout.addView(new LBAudioView(thisContext));
                //audioCount.setText(String.valueOf(audioLayout.getChildCount()));
                AudioFetcher.startRecording();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioFetcher.stopRecording();
                NoteDatabase noteDatabaseInstance = NoteDatabase.getInstance();
                String latestAudioLocation = "";
                try {
                    latestAudioLocation = noteDatabaseInstance.getLatestAudioLocation();
                }
                catch (NoAudiosYetException e) {
                    e.printStackTrace();
                }

                audioLayout.addView(
                        new LBAudioView(
                                thisContext,
                                latestAudioLocation
                        )
                );
            }
        });

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

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_ID: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    System.exit(0);
                }
            }
            case RECORD_AUDIO_ID: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    System.exit(0);
                }
            }
        }
    }
}
